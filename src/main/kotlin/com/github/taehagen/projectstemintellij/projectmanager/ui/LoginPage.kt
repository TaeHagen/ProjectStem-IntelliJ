package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.projectmanager.AuthState
import com.github.taehagen.projectstemintellij.projectmanager.Remote
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class LoginPage(val project: Project, val toolWindow: ToolWindow) : Page(toolWindow) {
    override fun getContent(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.add(JLabel("Auth token:"))
        val tokenField = JTextField()
        panel.add(tokenField)
        val errorLabel = JLabel()
        val button = JButton("Login")
        button.isEnabled = AuthState.loginInProgress
        button.addActionListener {
            button.isEnabled = false
            AuthState.loginInProgress = false
            UiState.runOnIoThread {
                val data = Remote.loginUser(tokenField.text)
                if (data != null) {
                    AuthState.user = data
                    data.fetchUser()
                    if (data.courses.size == 1) {
                        val working = data.courses[0].fetchWorking()
                        if (working != null) {
                            UiState.projectManager.selectedItem = working
                            ApplicationManager.getApplication().invokeLater() {
                                ToolWindowManager.getInstance(project).getToolWindow("Course")?.show(null)
                            }
                        }
                    }
                }
                {
                    if (data == null)
                        errorLabel.text = "Bad token"
                    button.isEnabled = true
                    AuthState.loginInProgress = true
                }
            }
        }
        panel.add(button)
        panel.add(errorLabel)
        return panel
    }

}