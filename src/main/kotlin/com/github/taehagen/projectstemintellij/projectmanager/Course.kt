package com.github.taehagen.projectstemintellij.projectmanager

class Course(val user: User, val id: Int, val name: String) {
    val modules = ArrayList<Module>()
    fun getModules(refresh: Boolean = false): ArrayList<Module> {
        if (modules.size != 0 && !refresh)
            return modules
        modules.clear()
        Remote.getModules(user, this, modules)
        return modules
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