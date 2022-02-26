package com.github.taehagen.projectstemintellij

import com.intellij.openapi.application.ApplicationManager

fun runOnIoThread(task: () -> () -> Unit) {
    Thread {
        val ret = task()
        ApplicationManager.getApplication().invokeLater {
            ret()
        }
    }.start()
}