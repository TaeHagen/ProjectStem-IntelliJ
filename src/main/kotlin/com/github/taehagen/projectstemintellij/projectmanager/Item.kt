package com.github.taehagen.projectstemintellij.projectmanager

class Item(val id: Int, val title: String, val type: String, val url: String?, val indent: Int, val completed: Boolean, val contentId: Int, val module: Module) {

    var description: String = "Loading"
    val files = ArrayList<File>()

    fun getDetails(refresh: Boolean = false) {
        if (description != "Loading" && !refresh)
            return
        if (type != "Assignment") {
            description = "<a href=\"${url}\" target=\"_blank\">View content</a>"
            return
        }
        if (url == null) return
        PageParser.parseAssignment(url, AuthState.user!!.token, this)
    }

    /**
     * Get next item
     * Blocking.
     */
    fun next(): Item? {
        return module.items.getOrNull(module.items.indexOf(this) + 1) ?: module.next()?.getItems()?.getOrNull(0)
    }

    /**
     * Get prev item
     * Blocking.
     */
    fun prev(): Item? {
        return module.items.getOrNull(module.items.indexOf(this) - 1) ?: module.prev()?.getItems()?.getOrNull((module.prev()?.items?.size ?: 0) - 1)
    }
}