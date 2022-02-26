package com.github.taehagen.projectstemintellij.listeners

import com.github.taehagen.projectstemintellij.projectmanager.AppState
import com.github.taehagen.projectstemintellij.projectmanager.ui.UiState
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.github.taehagen.projectstemintellij.services.MyProjectService

internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {

    }
}
