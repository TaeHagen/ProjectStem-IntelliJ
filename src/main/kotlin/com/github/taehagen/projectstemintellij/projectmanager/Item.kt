package com.github.taehagen.projectstemintellij.projectmanager

import com.github.taehagen.projectstemintellij.Stateful
import org.json.JSONObject

class Item(val user: User, val id: Int,
           val title: String,
           val type: String,
           val url: String?,
           val indent: Int,
           val completed: Boolean,
           val contentId: Int,
           val module: Module): Stateful() {

    var description: String = "Loading"
    val files = ArrayList<File>()
    var problem_id: Int = -1
    var lti_course_id: String = ""
    var lti_user_id: String = ""
    var ltiparams: JSONObject = JSONObject()
    var submission: Submission? = null

    fun hasDetails(): Boolean {
        return description != "Loading"
    }

    fun getDetails(refresh: Boolean = false) {
        if (hasDetails() && !refresh)
            return
        if (type != "Assignment") {
            description = "<a href=\"${url}\" target=\"_blank\">View content</a>"
            return
        }
        if (url == null) return
        PageParser.parseAssignment(url, user.token, this)
        stateChanged()
    }

    fun updateFiles(): Boolean {
        if (!files.any { it.dirty }) {
            return true // no files dirty
        }
        Remote.updateFiles(this)
        return !files.any { it.dirty } // if files are still dirty, we failed.
    }

    fun submit(): Boolean {
        return Remote.createSubmission(this) != null

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