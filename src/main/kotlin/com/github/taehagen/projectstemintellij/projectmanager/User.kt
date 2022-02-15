package com.github.taehagen.projectstemintellij.projectmanager

class User(val token: String, val name: String) {
    val courses = ArrayList<Courses>()
    fun getCourses() {
        courses.clear()

    }
}