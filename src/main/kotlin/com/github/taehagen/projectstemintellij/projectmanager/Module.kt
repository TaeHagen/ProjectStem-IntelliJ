package com.github.taehagen.projectstemintellij.projectmanager

class Module(val id: Int, val name: String, val state: String, val course: Course) {
    val items = ArrayList<Item>()
    fun getItems() {
        items.clear()
        Remote.getItems(AuthState.user!!.token, this, items)
    }
    fun fetchWorking(): Item? {
        if (items.size == 0)
            getItems()
        return items.find { !it.completed }
    }

}