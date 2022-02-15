package com.github.taehagen.projectstemintellij.projectmanager

import com.intellij.openapi.diagnostic.Logger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
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

    fun getCourses(token: String, courses: ArrayList<Courses>): Boolean {
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
            courses.add(Courses(course.getString("id"), course.getString("shortName")))
        }
        return true
    }
}