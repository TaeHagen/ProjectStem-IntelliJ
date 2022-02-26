package com.github.taehagen.projectstemintellij.projectmanager

import com.github.taehagen.projectstemintellij.projectmanager.ui.UiState
import com.github.taehagen.projectstemintellij.runOnIoThread
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project

class ProjectState(val project: Project) {
    val authState = AuthState()
    val uiState = UiState(this)
    val projectManager = ProjectManager(project)

    fun save(success: () -> Unit) {
        FileDocumentManager.getInstance().saveAllDocuments()
        runOnIoThread {
            val status = projectManager.saveFiles()
            return@runOnIoThread {
                if (status)
                    success()
                else
                    NotificationGroupManager.getInstance().getNotificationGroup("Status")
                        .createNotification("Error saving... don't close IDE", NotificationType.ERROR)
                        .notify(projectManager.project)
            }
        }
    }
}