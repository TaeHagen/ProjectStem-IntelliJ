package com.github.taehagen.projectstemintellij.services

import com.intellij.openapi.project.Project
import com.github.taehagen.projectstemintellij.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
