package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import javax.swing.JPanel

abstract class Page(val state: UiState) {
    private var contentFactory = ContentFactory.SERVICE.getInstance()
    private var content: Content? = null

    abstract fun getContent(): JPanel

    fun show() {
        if (content == null) {
            content = contentFactory.createContent(getContent(), "", false)
        }
        content?.let { state.toolWindow.contentManager.addContent(it) }
    }

    fun hide() {
        content?.let { state.toolWindow.contentManager.removeContent(it, true) }
        content = null
    }
}