package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow

class ItemViewWindow(val project: Project, val toolWindow: ToolWindow) {
    init {
        ItemViewPage(project, toolWindow).show()
    }
}