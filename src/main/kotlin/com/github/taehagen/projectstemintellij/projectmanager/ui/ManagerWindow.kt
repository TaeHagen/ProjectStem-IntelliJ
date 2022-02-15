package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory

class ManagerWindow(val project: Project, val toolWindow: ToolWindow) {
    val uiState = UiState(project, toolWindow)
    var page: Page? = null
        set (value) {
            if (field == value)
                return
            field?.hide()
            field = value
            field?.show()
        }

    init {
        uiState.addStateChangeListener {
            updatePage()
        }
        updatePage()
    }

    fun updatePage() {
        if (uiState.user == null) {
            page = LoginPage(uiState)
            return
        }
        page = MainPage(uiState)
    }
}