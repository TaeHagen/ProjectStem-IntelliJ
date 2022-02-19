package com.github.taehagen.projectstemintellij.projectmanager

import com.github.taehagen.projectstemintellij.Stateful

object AuthState : Stateful() {
    var user: User? = null
        set(value) {
            field = value
            stateChanged()
        }

    var loginInProgress: Boolean = true
        set(value) {
            field = value
            stateChanged()
        }
}