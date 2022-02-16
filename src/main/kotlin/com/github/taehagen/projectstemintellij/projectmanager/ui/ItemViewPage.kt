package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.projectmanager.AuthState
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.dsl.builder.panel
import javax.swing.JPanel

class ItemViewPage(val project: Project, val toolWindow: ToolWindow) : Page(toolWindow) {

    override fun getContent(): JPanel {
        return panel { }
    }

}