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
import javax.swing.JButton
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.event.HyperlinkEvent


class ItemViewPage(val project: Project, val toolWindow: ToolWindow) : Page(toolWindow) {

    val panel = JPanel()
    val submissionPanel = JPanel()
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
        save {
            UiState.runOnIoThread {
                val next = item.next()
                return@runOnIoThread {
                    UiState.projectManager.selectedItem = next
                }
            }
        }
    }
    fun prev() {
        save {
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

    fun save(success: () -> Unit) {
        FileDocumentManager.getInstance().saveAllDocuments()
        UiState.runOnIoThread {
            val status = UiState.projectManager.saveFiles()
            return@runOnIoThread {
                if (status)
                    success()
                else
                    NotificationGroupManager.getInstance().getNotificationGroup("Status")
                        .createNotification("Error saving... don't close IDE", NotificationType.ERROR)
                        .notify(project)
            }
        }
    }

    val submissionInner = JPanel()

    fun updateSubmissions() {
        submissionPanel.removeAll()
        submissionInner.removeAll()
        submissionPanel.layout = GridLayout(0, 1)
        val submission = item.submission
        val submitBtn = JButton("Check Code")
        submitBtn.addActionListener {
            if (submission != null && submission.status == "submitting")
                return@addActionListener
            submitBtn.text = "Checking code..."
            UiState.runOnIoThread {
                item.submit()
                return@runOnIoThread {
                    updateSubmissions()
                }
            }
        }
        if (submission == null) {
            submissionInner.add(submitBtn)
            return
        }
        if (submission.error != null) {
            submissionPanel.add(panel {
                group("Error") {
                    row {
                        label(submission.error!!)
                    }
                }
            })
            submissionInner.add(submitBtn)
            return
        }

        if (submission.status == "submitting") {
            submitBtn.text = "Checking code..."
            Thread {
                Thread.sleep(5000)
                if (item.submission != null && item.submission?.status == "submitting") {
                    item.submission?.checkSubmission()
                    ApplicationManager.getApplication().invokeLater {
                        updateSubmissions()
                    }
                }
            }.start()
            submissionInner.add(submitBtn)
            return
        }

        submissionInner.add(JLabel("Score ${submission.grade}%"))
        submissionInner.add(submitBtn)
        submissionPanel.add(panel {
            var idx = 0
            for ((index, result) in submission.results.withIndex()) {
                collapsibleGroup("Test ${index+1} - ${if (result.passed) "Pass" else "Fail"}") {
                    row {
                        comment("Description")
                    }
                    row {
                        label(result.description)
                    }
                    row {
                        comment("Message")
                    }
                    row {
                        label(if (result.passed) "Correct!" else result.debug_hint)
                    }
                    if (result.examples.size > 0) {
                        row {
                            comment("Results")
                        }
                    }
                    for (test in result.examples) {
                        group {
                            row {
                                comment("Expected")
                            }
                            row {
                                label(test.expected)
                            }
                            row {
                                comment("Found")
                            }
                            row {
                                label(test.found)
                            }
                        }
                    }
                }
                idx++
            }
        })
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
            row {
                button("Save") {
                    save {
                        NotificationGroupManager.getInstance().getNotificationGroup("Status")
                            .createNotification("Project saved!", NotificationType.INFORMATION)
                            .notify(project)
                    }
                }
                cell(submissionInner).horizontalAlign(HorizontalAlign.RIGHT)
            }
            row {
                cell(submissionPanel)
            }
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
                if (UiState.projectManager.selectedItem == item) {
                    // we've not selected something else
                    label.text = processHtml(item.description)
                    UiState.projectManager.openFiles()
                    updateSubmissions()
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