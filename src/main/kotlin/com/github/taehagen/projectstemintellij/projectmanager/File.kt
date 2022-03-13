package com.github.taehagen.projectstemintellij.projectmanager

class File(var id: Int, var name: String, var content: String, var preferred: Boolean, var version: Int) {
    var stagingContent: String? = null
        get() {
            if (field == null)
                return content
            return field
        }

    val dirty: Boolean
        get() {
            return stagingContent?.trim() != content.trim()
        }
}