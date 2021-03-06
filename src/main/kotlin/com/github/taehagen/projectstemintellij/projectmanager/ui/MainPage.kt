package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.projectmanager.*
import com.github.taehagen.projectstemintellij.runOnIoThread
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.dsl.gridLayout.VerticalAlign
import com.intellij.ui.treeStructure.Tree
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel


class CourseTreeNode(text: String, val data: Any) : DefaultMutableTreeNode(text) {}

class MainPage(val projectState: ProjectState, val toolWindow: ToolWindow) : Page(toolWindow) {
    fun updateItems(selected: CourseTreeNode) {
        val data = selected.data
        if (data is Course) {
            if (selected.childCount == data.modules.size)
                return
            selected.removeAllChildren()
            for (module in data.modules) {
                val node = CourseTreeNode(module.name, module)
                updateItems(node)
                selected.add(node)
            }
        }
        if (data is Module) {
            if (selected.childCount == data.items.size)
                return
            selected.removeAllChildren()
            for (item in data.items) {
                val node = CourseTreeNode("${"    ".repeat(item.indent)}${item.title}", item)
                updateItems(node)
                selected.add(node)
            }
        }
    }

    override fun getContent(): JPanel {
        val user = projectState.authState.user!!
        val panel = panel {
            row("Name: ") {
                label(user.name)
                button("Logout") {
                    projectState.authState.user = null
                    projectState.projectManager.selectedItem = null
                }.horizontalAlign(HorizontalAlign.RIGHT)
            }
            for (course in user.courses) {
                row {
                    val node = CourseTreeNode(course.name, course)
                    val tree = Tree(node)
                    val pane = JBScrollPane(tree)
                    pane.border = BorderFactory.createEmptyBorder()
                    cell(pane).horizontalAlign(HorizontalAlign.FILL).verticalAlign(VerticalAlign.FILL)
                    val updateAndReload = { nde: CourseTreeNode, update: () -> Unit ->
                        Thread {
                            update()
                            ApplicationManager.getApplication().invokeLater() {
                                updateItems(nde)
                                (tree.model as DefaultTreeModel).reload(nde)
                            }
                        }.start()
                    }
                    val selectItem = { selected: CourseTreeNode ->
                        val data = selected.data
                        if (data is Course)
                            updateAndReload(selected) { data.getModules() }
                        if (data is Module)
                            updateAndReload(selected) { data.getItems() }
                    }
                    tree.addTreeSelectionListener {
                        if (tree.lastSelectedPathComponent == null)
                            return@addTreeSelectionListener
                        selectItem(tree.lastSelectedPathComponent as CourseTreeNode)
                    }
                    val ml: MouseListener = object : MouseAdapter() {
                        override fun mousePressed(e: MouseEvent) {
                            if (e.clickCount == 2) {
                                if (tree.lastSelectedPathComponent == null)
                                    return
                                val data = (tree.lastSelectedPathComponent as CourseTreeNode).data
                                if (data is Item) {

                                    // save first!
                                    FileDocumentManager.getInstance().saveAllDocuments()
                                    runOnIoThread {
                                        val status = projectState.projectManager.saveFiles()
                                        return@runOnIoThread {
                                            if (status) {
                                                projectState.projectManager.selectedItem = data
                                                ToolWindowManager.getInstance(projectState.project).getToolWindow("Course")
                                                    ?.show(null)
                                            } else
                                                NotificationGroupManager.getInstance().getNotificationGroup("Status")
                                                    .createNotification("Error saving... don't close IDE", NotificationType.ERROR)
                                                    .notify(projectState.project)
                                        }
                                    }

                                }
                            }
                        }
                    }
                    tree.addMouseListener(ml)
                    updateItems(node)
                }.resizableRow()
            }
        }
        panel.border = EmptyBorder(1, 5, 5, 5)
        return panel
    }

}