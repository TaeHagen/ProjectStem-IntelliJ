package com.github.taehagen.projectstemintellij.projectmanager.ui

import com.github.taehagen.projectstemintellij.Stateful
import com.github.taehagen.projectstemintellij.projectmanager.ProjectManager
import com.intellij.openapi.project.Project

object UiState : Stateful() {
    lateinit var projectManager: ProjectManager;

    fun init(project: Project) {
        if (this::projectManager.isInitialized)
            return
        projectManager = ProjectManager(project)
        projectManager.addStateChangeListener { stateChanged() }
    }
}