package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.Stateful
import com.github.taehagen.projectstemintellij.UnsubscribeToken
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import javax.swing.JPanel

abstract class Page(val window: ToolWindow) {
    private var contentFactory = ContentFactory.SERVICE.getInstance()
    private var content: Content? = null
    protected var unsubscribeToken = UnsubscribeToken()

    abstract fun getContent(): JPanel

    fun show() {
        if (content == null) {
            content = contentFactory.createContent(getContent(), "", false)
        }
        content?.let { window.contentManager.addContent(it) }
    }

    fun hide() {
        content?.let { window.contentManager.removeContent(it, true) }
        content = null
    }

    fun destroy() {
        hide()
        unsubscribeToken.unsub()
    }
}