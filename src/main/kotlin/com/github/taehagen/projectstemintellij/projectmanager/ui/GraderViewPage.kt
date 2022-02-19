package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.UnsubscribeToken
import com.github.taehagen.projectstemintellij.projectmanager.Item
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
import java.awt.GridLayout
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants


class GraderViewPage(val project: Project, val toolWindow: ToolWindow) : Page(toolWindow) {
    val submissionPanel = JPanel()
    val submissionInner = JPanel()
    val panel = panel {
        row {
            cell(submissionInner)
        }
        row {
            val scroll = JBScrollPane(submissionPanel)
            scroll.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            scroll.border = null
            cell(scroll).horizontalAlign(HorizontalAlign.FILL).verticalAlign(VerticalAlign.FILL)
        }.resizableRow()
    }

    private val refreshStatements = arrayOf("Checking code...",
        "Still checking...",
        "This is taking a while...",
        "Wow even I'm surprised",
        "Their grader really does suck",
        "They should really be ashamed")
    var refreshCounter = 0

    init {
        UiState.projectManager.addStateChangeListener({
            updateSubmissions()
        }, unsubscribeToken)
        updateSubmissions()
    }

    override fun getContent(): JPanel {
        return panel
    }

    fun refreshFrames() {
        panel.invalidate()
        panel.validate()
        panel.repaint()
    }

    private val itemSubscribeListener = UnsubscribeToken()
    var lastItem: Item? = null

    fun updateSubmissions() {
        submissionPanel.removeAll()
        submissionInner.removeAll()
        val item = UiState.projectManager.selectedItem ?: return
        if (lastItem != item) {
            lastItem = item
            itemSubscribeListener.unsub()
            item.addStateChangeListener({
                updateSubmissions()
            }, itemSubscribeListener)
        }
        if (!item.hasDetails())
            return // come back later
        submissionPanel.layout = GridLayout(0, 1)
        val submission = item.submission
        val submitBtn = JButton("Check Code")
        submitBtn.addActionListener {
            if (submission != null && submission.status == "submitting")
                return@addActionListener
            submitBtn.isEnabled = false
            submitBtn.text = refreshStatements[0]
            refreshCounter = 0
            submissionPanel.removeAll()
            FileDocumentManager.getInstance().saveAllDocuments()
            UiState.runOnIoThread {
                UiState.projectManager.saveFiles()
                item.submit()
                return@runOnIoThread {
                    updateSubmissions()
                }
            }
        }
        val saveBtn = JButton("Save")
        saveBtn.addActionListener {
            saveBtn.isEnabled = false
            UiState.save {
                saveBtn.isEnabled = true
                NotificationGroupManager.getInstance().getNotificationGroup("Status")
                    .createNotification("Project saved!", NotificationType.INFORMATION)
                    .notify(project)
            }
        }
        if (submission == null) {
            submissionInner.add(submitBtn)
            submissionInner.add(saveBtn)
            refreshFrames()
            return
        }
        submitBtn.isEnabled = !submission.isSubmitting()
        val submit = JButton("Submit")
        submit.isEnabled = !submission.isSubmitted() && !submission.isSubmitting()
        submit.addActionListener {
            submit.isEnabled = false
            UiState.runOnIoThread {
                val res = submission.grade()
                return@runOnIoThread {
                    submit.isEnabled = !submission.isSubmitted()
                    if (res)
                        NotificationGroupManager.getInstance().getNotificationGroup("Status")
                            .createNotification("Your score of ${submission.grade} was submitted!", NotificationType.INFORMATION)
                            .notify(project)
                }
            }
        }
        submissionInner.add(JLabel("Score ${submission.grade}%"))
        submissionInner.add(submitBtn)
        submissionInner.add(saveBtn)
        submissionInner.add(submit)
        if (submission.error != null) {
            submissionPanel.add(panel {
                group("Error") {
                    row {
                        label(submission.error!!)
                    }
                }
            })
            refreshFrames()
            return
        }

        if (submission.status == "submitting") {
            submitBtn.text = refreshStatements[refreshCounter.coerceAtMost(refreshStatements.size - 1)]
            Thread {
                Thread.sleep(5000)
                if (item.submission != null && item.submission?.status == "submitting") {
                    item.submission?.checkSubmission()
                    ApplicationManager.getApplication().invokeLater {
                        refreshCounter++
                        updateSubmissions()
                    }
                }
            }.start()
            refreshFrames()
            return
        }
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        for ((index, result) in submission.results.withIndex()) {
            val resultItem = JPanel()
            resultItem.layout = BoxLayout(resultItem, BoxLayout.Y_AXIS)
            val label = JLabel("Test ${index+1} - ${if (result.passed) "Pass" else "Fail"}")
            resultItem.add(label)
            var showing = false
            // i am so done with this JSL ui. HTML is so much better and well documented.
            val details = JLabel("""<html>
                    <div>Description</div>
                    <div>${result.description}</div>
                    <div>Message</div>
                    <div>${if (result.passed) "Correct!" else result.debug_hint}</div>
                    ${result.examples.withIndex().joinToString("") {
                    """<div style="padding-left: 10px">
                        <div>Test ${it.index+1}</div>
                        <div>Expected</div>
                        <div>${it.value.expected}</div>
                        <div>Found</div>
                        <div>${it.value.found}</div>
                    </div>""".trimIndent()
                    }}
                </html>""".trimMargin())
            label.addMouseListener(object : MouseListener {
                override fun mouseClicked(p0: MouseEvent?) {
                    if (showing) {
                        resultItem.remove(details)
                    } else {
                        resultItem.add(details)
                    }
                    showing = !showing
                    refreshFrames()
                }
                override fun mousePressed(p0: MouseEvent?) {}
                override fun mouseReleased(p0: MouseEvent?) {}
                override fun mouseEntered(p0: MouseEvent?) {}
                override fun mouseExited(p0: MouseEvent?) {}
            })
            panel.add(resultItem)
        }
        submissionPanel.add(panel)
        refreshFrames()
    }
}