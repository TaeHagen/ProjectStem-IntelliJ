package com.github.taehagen.projectstemintellij.projectmanager

import com.intellij.openapi.project.Project

object AppState {
    val projectStates = HashMap<Project, ProjectState>()

    fun getStateForProject(project: Project): ProjectState {
        if (!projectStates.contains(project)) {
            projectStates[project] = ProjectState(project)
        }
        return projectStates[project]!!
    }
}