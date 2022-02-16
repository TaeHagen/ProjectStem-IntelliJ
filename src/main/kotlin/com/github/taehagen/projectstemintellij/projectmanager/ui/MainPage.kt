package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.projectmanager.AuthState
import com.github.taehagen.projectstemintellij.projectmanager.Course
import com.github.taehagen.projectstemintellij.projectmanager.Item
import com.github.taehagen.projectstemintellij.projectmanager.Module
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.dsl.gridLayout.VerticalAlign
import com.intellij.ui.treeStructure.Tree
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel


class CourseTreeNode(text: String, val data: Any) : DefaultMutableTreeNode(text) {}

class MainPage(state: ManagerState) : Page(state.toolWindow) {
    fun updateItems(selected: CourseTreeNode) {
        val data = selected.data
        if (data is Course) {
            for (module in data.modules) {
                val node = CourseTreeNode(module.name, module)
                updateItems(node)
                selected.add(node)
            }
        }
        if (data is Module) {
            for (item in data.items) {
                val node = CourseTreeNode("${"    ".repeat(item.indent)}${item.title}", item)
                updateItems(node)
                selected.add(node)
            }
        }
    }

    override fun getContent(): JPanel {
        val user = AuthState.user!!
        return panel {
            row("Name: ") {
                label(user.name)
                button("Logout") {
                    AuthState.user = null
                }.horizontalAlign(HorizontalAlign.RIGHT)
            }
            for (course in user.courses) {
                row {
                    val node = CourseTreeNode(course.name, course)
                    val tree = Tree(node)
                    val pane = JBScrollPane(tree)
                    pane.border = null
                    cell(pane).horizontalAlign(HorizontalAlign.FILL).verticalAlign(VerticalAlign.FILL)
                    val updateAndReload = { node: CourseTreeNode, update: () -> Unit ->
                        Thread {
                            update()
                            ApplicationManager.getApplication().invokeLater() {
                                updateItems(node)
                                (tree.model as DefaultTreeModel).reload(node)
                            }
                        }.start()
                    }
                    val selectItem = { selected: CourseTreeNode ->
                        val data = selected.data
                        if (data is Course && data.modules.size == 0)
                            updateAndReload(selected) { data.getModules() }
                        if (data is Module && data.items.size == 0)
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
                                if (data is Item)
                                    UiState.projectManager.selectedItem = data
                            }
                        }
                    }
                    tree.addMouseListener(ml)
                    updateItems(node)
                }.resizableRow()
            }
        }
    }

}