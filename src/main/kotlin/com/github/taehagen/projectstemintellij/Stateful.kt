package com.github.taehagen.projectstemintellij

import com.intellij.ui.layout.ComponentPredicate

typealias StateListener = () -> Unit

class UnsubscribeToken {
    val unsubFrom = ArrayList<() -> Unit>()
    fun unsub() {
        unsubFrom.forEach { it() }
        unsubFrom.clear()
    }
}

open class Stateful {
    private var stateChangeListeners = ArrayList<StateListener>()

    protected fun stateChanged() {
        for (l in stateChangeListeners)
            l()
    }

    fun addStateChangeListener(l: StateListener, unsubToken: UnsubscribeToken) {
        stateChangeListeners.add(l)
        unsubToken.unsubFrom.add {
            stateChangeListeners.remove(l)
        }
    }

    fun getPredicate(data: () -> Boolean, unsubToken: UnsubscribeToken): ComponentPredicate {
        return object : ComponentPredicate() {
            override fun invoke() = data()

            override fun addListener(listener: (Boolean) -> Unit) {
                addStateChangeListener({ listener(data()) }, unsubToken)
            }
        }
    }
}