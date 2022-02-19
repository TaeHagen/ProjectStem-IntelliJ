package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.UnsubscribeToken
import com.github.taehagen.projectstemintellij.projectmanager.AuthState
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow

class ManagerWindow(val project: Project, val toolWindow: ToolWindow) {
    val unsubscribeToken = UnsubscribeToken()

    var page: Page? = null
        set (value) {
            if (field == value)
                return
            field?.hide()
            field = value
            field?.show()
        }

    init {
        AuthState.addStateChangeListener({
            ApplicationManager.getApplication().invokeLater() {
                updatePage()
            }
        }, unsubscribeToken)
        updatePage()
    }

    fun updatePage() {
        page?.destroy() // we always recreate pages
        if (AuthState.user == null || !AuthState.loginInProgress) {
            page = LoginPage(project, toolWindow)
            return
        }
        page = MainPage(project, toolWindow)
    }
}