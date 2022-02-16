package com.github.taehagen.projectstemintellij.projectmanager

class User(val token: String, val name: String) {
    val courses = ArrayList<Course>()
    fun getCourses() {
        courses.clear()
        Remote.getCourses(token, courses)
    }

    fun fetchUser() {
        getCourses()
        if (courses.size == 1) {
            courses[0].fetchWorking()
        }
    }
}