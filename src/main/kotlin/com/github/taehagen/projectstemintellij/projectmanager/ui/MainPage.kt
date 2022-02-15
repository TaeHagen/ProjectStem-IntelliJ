package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.intellij.ui.dsl.builder.panel
import javax.swing.JPanel

class MainPage(state: UiState) : Page(state) {
    override fun getContent(): JPanel {
        return panel {
            row("Name: ") {
                label(state.user!!.name)
            }
        }
    }

}