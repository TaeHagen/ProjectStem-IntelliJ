package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.projectmanager.Remote
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.swing.JPanel

class LoginPage(state: UiState) : Page(state) {
    lateinit var panel: DialogPanel
    override fun getContent(): JPanel {
        val model = Model()
        panel = panel {
            row("Auth token:") {
                textField().bindText(model::token)
                button("Login") {
                    panel.apply()
                    state.loading = true
                    runBlocking {
                        launch(Dispatchers.IO) {
                            val data = Remote.loginUser(model.token)
                            ApplicationManager.getApplication().invokeLater() {
                                state.user = data
                                if (data == null)
                                    state.loginError = "Bad token"
                                state.loading = false
                            }
                        }
                    }
                }
            }
            row("Error: ") {
                label(state.loginError)
            }.enabled(state.loginError != "")
        }
        return panel
    }

}

internal data class Model(
    var token: String = ""
)