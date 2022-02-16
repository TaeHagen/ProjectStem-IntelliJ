package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class ItemViewFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        UiState.init(project)
        ItemViewWindow(project, toolWindow)
    }
}