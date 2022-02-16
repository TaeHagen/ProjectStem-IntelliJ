package com.github.taehagen.projectstemintellij.projectmanager

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject


object Remote {
    var client = OkHttpClient()

    fun loginUser(token: String): User? {
        val req = Request.Builder()
            .url("https://courses.projectstem.org/profile")
            .header("cookie", "_legacy_normandy_session=$token")
            .header("accept", "application/json")
            .build();
        val res = client.newCall(req).execute()
        if (res.code != 200) {
            return null
        }
        val json = JSONObject(res.body!!.string())
        return User(token, json.getString("name"))
    }

    fun getCourses(token: String, courses: ArrayList<Course>): Boolean {
        val req = Request.Builder()
            .url("https://courses.projectstem.org/api/v1/dashboard/dashboard_cards")
            .header("cookie", "_normandy_session=$token")
            .header("accept", "application/json")
            .build();
        val res = client.newCall(req).execute()
        if (res.code != 200) {
            return false
        }
        val json = JSONArray(res.body!!.string())
        (0 until json.length()).forEach {
            val course = json.getJSONObject(it)
            courses.add(Course(course.getInt("id"), course.getString("shortName")))
        }
        return true
    }

    fun getModules(token: String, course: Course, modules: ArrayList<Module>): Boolean {
        val req = Request.Builder()
            .url("https://courses.projectstem.org/api/v1/courses/${course.id}/modules?per_page=50")
            .header("cookie", "_normandy_session=$token")
            .header("accept", "application/json")
            .build();
        val res = client.newCall(req).execute()
        if (res.code != 200) {
            return false
        }
        val json = JSONArray(res.body!!.string())
        (0 until json.length()).forEach {
            val model = json.getJSONObject(it)
            modules.add(
                Module(
                    model.getInt("id"),
                    model.getString("name"),
                    model.getString("state"),
                    course
                )
            )
        }
        return true
    }

    fun getItems(token: String, module: Module, items: ArrayList<Item>): Boolean {
        var page = 1
        while (true) {
            val req = Request.Builder()
                .url("https://courses.projectstem.org/api/v1/courses/${module.course.id}/modules/${module.id}/items?per_page=50&page=${page}")
                .header("cookie", "_normandy_session=$token")
                .header("accept", "application/json")
                .build();
            val res = client.newCall(req).execute()
            if (res.code != 200) {
                return false
            }
            val json = JSONArray(res.body!!.string())
            (0 until json.length()).forEach {
                val model = json.getJSONObject(it)
                print(model.toString())
                items.add(
                    Item(
                        model.getInt("id"),
                        model.getString("title"),
                        model.getString("type"),
                        if (model.has("html_url")) model.getString("html_url") else null,
                        model.getInt("indent"),
                        if (model.has("completion_requirement")) model.getJSONObject("completion_requirement")
                            .getBoolean("completed") else true,
                        module
                    )
                )
            }
            if (json.length() != 50)
                break
            page++
        }
        return true
    }
}