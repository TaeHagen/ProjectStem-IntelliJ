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

class Loading(state: UiState) : Page(state) {
    override fun getContent(): JPanel {
        return panel {
            row {
                label("Loading")
            }
        }
    }

}