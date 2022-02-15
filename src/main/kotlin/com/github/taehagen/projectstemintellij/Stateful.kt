package com.github.taehagen.projectstemintellij

typealias StateListener = () -> Unit

open class Stateful {
    private var stateChangeListeners = ArrayList<StateListener>()

    protected fun stateChanged() {
        for (l in stateChangeListeners)
            l()
    }

    fun addStateChangeListener(l: StateListener) {
        stateChangeListeners.add(l)
    }
}