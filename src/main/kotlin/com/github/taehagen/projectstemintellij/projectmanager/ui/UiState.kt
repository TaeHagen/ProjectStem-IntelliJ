package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.Stateful
import com.github.taehagen.projectstemintellij.projectmanager.AuthState
import com.github.taehagen.projectstemintellij.projectmanager.ProjectManager
import com.github.taehagen.projectstemintellij.projectmanager.ProjectState
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

class UiState(val projectState: ProjectState) : Stateful() {

    fun openUsefulPanes() {
        if (projectState.authState.loginInProgress || projectState.authState.user == null) {
            ToolWindowManager.getInstance(projectState.projectManager.project).getToolWindow("Project Stem")?.show(null)
        } else {
            ToolWindowManager.getInstance(projectState.projectManager.project).getToolWindow("Course")?.show(null)
        }
        ToolWindowManager.getInstance(projectState.projectManager.project).getToolWindow("Grader")?.show(null)
    }
}