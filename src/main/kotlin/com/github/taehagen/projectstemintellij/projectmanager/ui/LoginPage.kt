package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.projectmanager.AuthState
import com.github.taehagen.projectstemintellij.projectmanager.Remote
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.JPanel

class LoginPage(val state: ManagerState) : Page(state.toolWindow) {
    lateinit var panel: DialogPanel
    override fun getContent(): JPanel {
        val model = Model()
        panel = panel {
            row("Auth token:") {
                textField().bindText(model::token)
                button("Login") {
                    panel.apply()
                    state.loading = true
                    Thread {
                        val data = Remote.loginUser(model.token)
                        AuthState.user = data
                        data?.fetchUser()
                        if (data != null && data.courses.size == 1) {
                            val working = data.courses[0].fetchWorking()
                            if (working != null) {
                                UiState.projectManager.selectedItem = working
                                ApplicationManager.getApplication().invokeLater() {
                                    ToolWindowManager.getInstance(state.project).getToolWindow("Course")?.show(null)
                                }
                            }
                        }
                        ApplicationManager.getApplication().invokeLater() {
                            if (data == null)
                                state.loginError = "Bad token"
                            state.loading = false
                        }
                    }.start()
                }
            }
            row("Error: ") {
                label(state.loginError)
            }.visible(state.loginError != "")
        }
        return panel
    }

}

internal data class Model(
    var token: String = ""
)