package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.Stateful
import com.github.taehagen.projectstemintellij.projectmanager.ProjectManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow


class ManagerState(val project: Project, val toolWindow: ToolWindow) : Stateful() {
    var loading = false
        set(value) {
            field = value
            stateChanged()
        }

    var loginError: String = "";
}