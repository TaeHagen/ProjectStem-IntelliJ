package com.github.taehagen.projectstemintellij.projectmanager

import com.github.taehagen.projectstemintellij.Stateful
import com.intellij.openapi.project.Project

class ProjectManager(val project: Project) : Stateful() {
    var selectedItem: Item? = null
        set(value) {
            field = value
            println(value?.title)
            stateChanged()
        }
}