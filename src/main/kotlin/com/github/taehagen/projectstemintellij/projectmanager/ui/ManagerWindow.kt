package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.UnsubscribeToken
import com.github.taehagen.projectstemintellij.projectmanager.AuthState
import com.github.taehagen.projectstemintellij.projectmanager.ProjectState
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow

class ManagerWindow(val projectState: ProjectState, val toolWindow: ToolWindow) {
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
        projectState.authState.addStateChangeListener({
            ApplicationManager.getApplication().invokeLater() {
                updatePage()
            }
        }, unsubscribeToken)
        updatePage()
    }

    lateinit var loginPage: LoginPage
    lateinit var mainPage: MainPage

    fun updatePage() {
        if (!this::loginPage.isInitialized) {
            loginPage = LoginPage(projectState, toolWindow)
            mainPage = MainPage(projectState, toolWindow)
        }
        if (projectState.authState.user == null || !projectState.authState.loginInProgress) {
            page = loginPage
            return
        }
        page = mainPage
    }
}