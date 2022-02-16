package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.intellij.ui.dsl.builder.panel
import javax.swing.JPanel

class Loading(state: ManagerState) : Page(state.toolWindow) {
    override fun getContent(): JPanel {
        return panel {
            row {
                label("loading")
            }
        }
    }

}