package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.projectmanager.AuthState
import com.github.taehagen.projectstemintellij.projectmanager.ProjectState
import com.github.taehagen.projectstemintellij.projectmanager.Remote
import com.github.taehagen.projectstemintellij.runOnIoThread
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBTextField
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class LoginPage(val projectState: ProjectState, val toolWindow: ToolWindow) : Page(toolWindow) {
    override fun getContent(): JPanel {
        val panel = JPanel()
        panel.layout = GridBagLayout()
        val const = GridBagConstraints()
        const.fill = GridBagConstraints.HORIZONTAL
        const.gridx = 0
        const.gridy = 0
        panel.add(JLabel("Auth token:"), const)
        val tokenField = JBTextField()
        const.gridx = 1
        const.weightx = 1.0
        panel.add(tokenField, const)
        const.weightx = 0.0
        val errorLabel = JLabel()
        val button = JButton("Login")
        button.isEnabled = projectState.authState.loginInProgress
        button.addActionListener {
            button.isEnabled = false
            projectState.authState.loginInProgress = false
            runOnIoThread {
                val data = Remote.loginUser(tokenField.text)
                if (data != null) {
                    projectState.authState.user = data
                    data.fetchUser()
                    if (data.courses.size == 1) {
                        val working = data.courses[0].fetchWorking()
                        if (working != null) {
                            projectState.projectManager.selectedItem = working
                            ApplicationManager.getApplication().invokeLater() {
                                ToolWindowManager.getInstance(projectState.project).getToolWindow("Course")?.show(null)
                            }
                        }
                    }
                }
                {
                    if (data == null)
                        errorLabel.text = "Bad token"
                    button.isEnabled = true
                    projectState.authState.loginInProgress = true
                }
            }
        }
        const.gridx = 2
        panel.add(button, const)
        const.gridx = 0
        const.gridy = 1
        const.gridwidth = 2
        panel.add(errorLabel, const)
        const.gridy = 2
        const.weighty = 1.0
        panel.add(JPanel(), const)
        return panel
    }

}