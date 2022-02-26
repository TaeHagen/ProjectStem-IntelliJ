package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.UnsubscribeToken
import com.github.taehagen.projectstemintellij.projectmanager.Item
import com.github.taehagen.projectstemintellij.projectmanager.ProjectState
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
import java.awt.GridLayout
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants
import javax.swing.border.EmptyBorder
import javax.swing.plaf.basic.BasicArrowButton


class GraderViewPage(val projectState: ProjectState, val toolWindow: ToolWindow) : Page(toolWindow) {
    val submissionPanel = JPanel()
    val submissionInner = JPanel()
    val panel = panel {
        row {
            submissionInner.layout = BoxLayout(submissionInner, BoxLayout.X_AXIS)
            cell(submissionInner).horizontalAlign(HorizontalAlign.FILL)
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
        panel.border = EmptyBorder(3, 5, 5, 5)
        projectState.projectManager.addStateChangeListener({
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

    private var itemSubscribeListener = UnsubscribeToken()
    var lastItem: Item? = null

    fun updateSubmissions() {
        submissionPanel.removeAll()
        submissionInner.removeAll()
        val item = projectState.projectManager.selectedItem ?: return
        if (lastItem != item) {
            lastItem = item
            if (itemSubscribeListener == null) { // i know its always false right KOTLIN??????
                itemSubscribeListener = UnsubscribeToken() // happens when this is called in init
            }
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
            runOnIoThread {
                projectState.projectManager.saveFiles()
                item.submit()
                return@runOnIoThread {
                    updateSubmissions()
                }
            }
        }
        val saveBtn = JButton("Save")
        saveBtn.addActionListener {
            saveBtn.isEnabled = false
            projectState.save {
                saveBtn.isEnabled = true
                NotificationGroupManager.getInstance().getNotificationGroup("Status")
                    .createNotification("Project saved!", NotificationType.INFORMATION)
                    .notify(projectState.project)
            }
        }
        if (submission == null) {
            submissionInner.add(submitBtn)
            submissionInner.add(Box.createHorizontalGlue())
            submissionInner.add(saveBtn)
            refreshFrames()
            return
        }
        submitBtn.isEnabled = !submission.isSubmitting()
        val submit = JButton("Submit")
        submit.isEnabled = !submission.isSubmitted() && !submission.isSubmitting()
        submit.addActionListener {
            submit.isEnabled = false
            runOnIoThread {
                val res = submission.grade()
                return@runOnIoThread {
                    submit.isEnabled = !submission.isSubmitted()
                    if (res)
                        NotificationGroupManager.getInstance().getNotificationGroup("Status")
                            .createNotification("Your score of ${submission.grade} was submitted!", NotificationType.INFORMATION)
                            .notify(projectState.project)
                }
            }
        }
        val label = JLabel("<html>Score <b>${submission.grade}%</b></html>")
        label.maximumSize = label.preferredSize
        submissionInner.add(label)
        submissionInner.add(submitBtn)
        submissionInner.add(submit)
        submissionInner.add(Box.createHorizontalGlue())
        submissionInner.add(saveBtn)
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
            resultItem.border = EmptyBorder(10, 0, 10, 0)
            val label = JLabel()
            resultItem.add(label)
            var showing = false
            val update = {
                label.text = "<html>Test ${index+1} - <b style=\"color: #${if (result.passed) "c9e1a7" else "ffc7ba"};\">${if (result.passed) "Pass" else "Fail"}</b>&nbsp;&nbsp;&nbsp;<b>${if (showing) "\u25be" else "\u25b8"}</b></html>"
            }
            update()
            // i am so done with this JSL ui. HTML is so much better and well documented.
            val details = JLabel("""<html>
                    <br>
                    <div><b>Description</b></div>
                    <div>${result.description}</div>
                    <br>
                    <div><b>Message</b></div>
                    <div>${if (result.passed) "Correct!" else result.debug_hint}</div>
                    ${if (result.examples.size > 0) "<br>" else ""}
                    ${result.examples.withIndex().joinToString("<br>") {
                    """<div style="padding-left: 10px">
                        <div><b>Test ${it.index+1}</b></div>
                        <div><b>Expected</b></div>
                        <div>${it.value.expected}</div>
                        <div><b>Found</b></div>
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
                    update()
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