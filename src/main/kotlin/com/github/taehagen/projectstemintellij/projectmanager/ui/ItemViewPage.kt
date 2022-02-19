package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.DesktopApi
import com.github.taehagen.projectstemintellij.projectmanager.AuthState
import com.github.taehagen.projectstemintellij.projectmanager.Item
import com.github.taehagen.projectstemintellij.projectmanager.Submission
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
import java.awt.GridLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.event.HyperlinkEvent


class ItemViewPage(val project: Project, val toolWindow: ToolWindow) : Page(toolWindow) {

    val panel = JPanel()
    lateinit var item: Item

    fun processHtml(data: String): String {
        return """<html>
            <style>
            .section-body {
                color:white;
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
        UiState.save {
            UiState.runOnIoThread {
                val next = item.next()
                return@runOnIoThread {
                    UiState.projectManager.selectedItem = next
                }
            }
        }
    }
    fun prev() {
        UiState.save {
            UiState.runOnIoThread {
                var next = item.prev()
                while (next != null && !canDisplay(next))
                    next = next.prev()
                return@runOnIoThread {
                    UiState.projectManager.selectedItem = next
                }
            }
        }
    }

    fun updateContent() {
        panel.removeAll()
        val item = UiState.projectManager.selectedItem
        if (item == null) {
            panel.add(JLabel("Select something!"))
            return
        }
        this.item = item
        if (!canDisplay()) {
            next()
        }
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
        val scroll = JBScrollPane(label)
        scroll.border = null
        val header = JPanel()
        header.layout = BoxLayout(header, BoxLayout.X_AXIS)
        val prev = JButton("Previous")
        prev.isEnabled = false
        header.add(prev)
        header.add(JLabel(item.title))
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
        UiState.runOnIoThread {
            item.getDetails()
            return@runOnIoThread {
                if (UiState.projectManager.selectedItem == item) {
                    // we've not selected something else
                    label.text = processHtml(item.description)
                    UiState.projectManager.openFiles()
                    prev.isEnabled = true
                    next.isEnabled = true
                }
            }
        }
        panel.updateUI()
    }

    init {
        UiState.projectManager.addStateChangeListener({
            updateContent()
        }, unsubscribeToken)
        updateContent()
    }

    override fun getContent(): JPanel {
        return panel
    }

}