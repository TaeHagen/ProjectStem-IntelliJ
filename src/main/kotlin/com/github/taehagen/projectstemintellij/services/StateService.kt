package com.github.taehagen.projectstemintellij.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State

@State(name = "stemData.xml")
class StateService : PersistentStateComponent<StemState> {

    var myState = StemState()

    override fun getState(): StemState {
        return myState
    }

    override fun loadState(state: StemState) {
        myState = state
    }
}

class StemState {
    var openItem = -1
    var openModule = -1
    var openCourse = -1
}