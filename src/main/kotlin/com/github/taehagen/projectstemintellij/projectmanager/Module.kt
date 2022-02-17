package com.github.taehagen.projectstemintellij.projectmanager

class Module(val id: Int, val name: String, val state: String, val course: Course) {
    val items = ArrayList<Item>()
    fun getItems(refresh: Boolean = false): ArrayList<Item> {
        if (items.size != 0 && !refresh)
            return items
        items.clear()
        Remote.getItems(AuthState.user!!.token, this, items)
        return items
    }

    fun fetchWorking(): Item? {
        if (items.size == 0)
            getItems()
        return items.find { !it.completed }
    }

    fun next(): Module? {
        return course.modules.getOrNull(course.modules.indexOf(this)+1)
    }

    fun prev(): Module? {
        return course.modules.getOrNull(course.modules.indexOf(this)-1)
    }

}