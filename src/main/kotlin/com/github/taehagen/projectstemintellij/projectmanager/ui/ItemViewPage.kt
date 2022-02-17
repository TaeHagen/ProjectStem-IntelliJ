package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.DesktopApi
import com.github.taehagen.projectstemintellij.projectmanager.AuthState
import com.github.taehagen.projectstemintellij.projectmanager.Item
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
        UiState.runOnIoThread {
            val next = item.next()
            return@runOnIoThread {
                UiState.projectManager.selectedItem = next
            }
        }
    }
    fun prev() {
        UiState.runOnIoThread {
            var next = item.prev()
            while (next != null && !canDisplay(next))
                next = next.prev()
            return@runOnIoThread {
                UiState.projectManager.selectedItem = next
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
        panel.layout = GridLayout(0, 1)
        val label = JEditorPane("text/html", processHtml(item.description))
        val scroll = JBScrollPane(label)
        scroll.border = null
        panel.add(panel {
            row {
                button("Previous") {
                    prev()
                }
                label(item.title).horizontalAlign(HorizontalAlign.CENTER)
                button("Next") {
                    next()
                }.horizontalAlign(HorizontalAlign.RIGHT)
            }
            row {
                cell(scroll).horizontalAlign(HorizontalAlign.FILL).verticalAlign(VerticalAlign.FILL)
            }.resizableRow()
        })
        label.isEditable = false
        label.border = null
        label.background = null
        label.isOpaque = false
        label.addHyperlinkListener {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(it.eventType)) {
                DesktopApi.browse(it.url.toURI())
            }
        }
        UiState.runOnIoThread {
            item.getDetails()
            return@runOnIoThread {
                label.text = processHtml(item.description)
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