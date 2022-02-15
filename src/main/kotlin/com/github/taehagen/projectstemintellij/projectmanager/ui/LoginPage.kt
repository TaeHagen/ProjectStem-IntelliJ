package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.projectmanager.Remote
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
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
                    state.user = Remote.loginUser(model.token)
                }
            }
        }
        return panel
    }

}

internal data class Model(
    var token: String = ""
)