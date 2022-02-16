package com.github.taehagen.projectstemintellij.projectmanager

class Course(val id: Int, val name: String) {
    val modules = ArrayList<Module>()
    fun getModules() {
        modules.clear()
        Remote.getModules(AuthState.user!!.token, this, modules)
    }

    /**
     * Fetch the item that is currently being worked on
     */
    fun fetchWorking(): Item? {
        if (modules.size == 0)
            getModules()
        return modules.find { it.state != "completed" }?.fetchWorking()
    }
}