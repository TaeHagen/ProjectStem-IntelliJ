package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.DesktopApi
import com.github.taehagen.projectstemintellij.projectmanager.*
import com.github.taehagen.projectstemintellij.runOnIoThread
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.dsl.gridLayout.VerticalAlign
import com.intellij.ui.layout.ComponentPredicate
import java.awt.Color
import java.awt.Desktop
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.HyperlinkEvent


class ItemViewPage(val projectState: ProjectState, val toolWindow: ToolWindow) : Page(toolWindow) {

    val panel = JPanel()
    lateinit var item: Item

    fun processHtml(data: String): String {
        return """<html>
            <style>
            body {
                font-family: Helvetica, sans-serif;
            }
            </style>
            ${data}
            </html>
        """.trimIndent()
    }

    fun canDisplay(item: Item = this.item): Boolean {
        return item.type == "Page" ||
                item.type == "Assignment" ||
                item.type == "Quiz"
    }

    fun next() {
        projectState.save {
            runOnIoThread {
                val next = item.next()
                return@runOnIoThread {
                    projectState.projectManager.selectedItem = next
                }
            }
        }
    }
    fun prev() {
        projectState.save {
            runOnIoThread {
                var next = item.prev()
                while (next != null && !canDisplay(next))
                    next = next.prev()
                return@runOnIoThread {
                    projectState.projectManager.selectedItem = next
                }
            }
        }
    }

    fun updateContent() {
        panel.removeAll()
        if (projectState.authState.user == null) {
            panel.add(JLabel("Log on in the Project Stem panel (to the left)!"))
            return
        }
        val item = projectState.projectManager.selectedItem
        if (item == null) {
            panel.add(JLabel("Select something in the Project Stem panel (to the left)!"))
            return
        }
        this.item = item
        if (!canDisplay()) {
            next()
        }
        panel.border = EmptyBorder(3, 5, 5, 5)
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        val label = JEditorPane("text/html", processHtml(item.description))
        label.isEditable = false
        label.border = null
        label.background = null
        label.isOpaque = false
        label.addHyperlinkListener {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(it.eventType)) {
                DesktopApi.browse(it.url.toURI())
            }
        }
        label.background = Color(0, 0, 0, 0)
        val scroll = JBScrollPane(label)
        scroll.border = BorderFactory.createEmptyBorder()
        val header = JPanel()
        header.layout = BoxLayout(header, BoxLayout.X_AXIS)
        val prev = JButton("Previous")
        prev.isEnabled = false
        header.add(prev)
        val title = JLabel("<html><b>${item.title}</b></html>", SwingConstants.CENTER)
        title.maximumSize = Dimension(Integer.MAX_VALUE, title.preferredSize.height)
        header.add(title)
        val next = JButton("Next")
        next.isEnabled = false
        next.addActionListener {
            next.isEnabled = false
            prev.isEnabled = false
            next()
        }
        prev.addActionListener {
            prev.isEnabled = false
            next.isEnabled = false
            prev()
        }
        header.add(next)
        panel.add(header)
        panel.add(scroll)
        runOnIoThread {
            item.getDetails()
            return@runOnIoThread {
                if (projectState.projectManager.selectedItem == item) {
                    // we've not selected something else
                    label.text = processHtml(item.description)
                    projectState.projectManager.openFiles()
                    prev.isEnabled = true
                    next.isEnabled = true
                }
            }
        }
        panel.updateUI()
    }

    init {
        projectState.uiState.openUsefulPanes() // kick them to the other panel if they start here and are logged out
        projectState.projectManager.addStateChangeListener({
            updateContent()
        }, unsubscribeToken)
        updateContent()
    }

    override fun getContent(): JPanel {
        return panel
    }

}