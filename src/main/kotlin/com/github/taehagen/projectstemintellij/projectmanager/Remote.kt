package com.github.taehagen.projectstemintellij.projectmanager

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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
                items.add(
                    Item(
                        model.getInt("id"),
                        model.getString("title"),
                        model.getString("type"),
                        if (model.has("html_url")) model.getString("html_url") else null,
                        model.getInt("indent"),
                        if (model.has("completion_requirement")) model.getJSONObject("completion_requirement")
                            .getBoolean("completed") else true,
                        if (model.has("content_id")) model.getInt("content_id") else -1,
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

    fun parseSubmission(data: JSONObject, sub: Submission) {
        sub.id = data.getInt("id")
        sub.grade = data.getInt("grade")
        sub.status = data.getString("status")
        val result = data.getJSONObject("response").getJSONObject("result")
        sub.error = if (result.getJSONArray("errors").length() > 0) result.getJSONArray("errors").getJSONObject(0).getString("error") else null
        val arr = result.getJSONArray("datasets")
        sub.results.clear()
        (0 until arr.length()).forEach { idx ->
            val dataset = arr.getJSONObject(idx)
            val results = ArrayList<GraderExample>()
            val resultsJson = dataset.getJSONArray("results")
            (0 until resultsJson.length()).forEach { results_idx ->
                val resultJson = resultsJson.getJSONObject(results_idx)
                results.add(GraderExample(resultJson.getString("expected"), resultJson.getString("found")))
            }
            sub.results.add(GraderResult(dataset.getString("description"), dataset.getString("debug_hint"), dataset.getBoolean("passed"), results))
        }
    }

    fun updateFiles(token: String, item: Item) {
        val obj = JSONObject()
        obj.put("lti_user_id", item.lti_user_id)
        obj.put("lti_course_id", item.lti_course_id)
        obj.put("problem_id", item.problem_id)
        val files = JSONArray()
        obj.put("files", files)
        for (file in item.files) {
            val fileobj = JSONObject()
            fileobj.put("id", file.id)
            fileobj.put("name", file.name)
            fileobj.put("content", file.stagingContent)
            fileobj.put("modified", file.content != file.stagingContent)
            fileobj.put("disable_history", false)
            files.put(fileobj)
        }
        val req = Request.Builder()
            .url("https://coderunner.projectstem.org/api/v1/problem_files/update_all")
            .header("accept", "application/json")
            .post(obj.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
            .build();
        val res = client.newCall(req).execute()
        if (res.code != 200) {
            return
        }
        val json = JSONObject(res.body!!.string())
        val arr = json.getJSONArray("files")
        (0 until arr.length()).forEach { idx ->
            val file = arr.getJSONObject(idx)
            val inputFile = item.files.find { it.name == file.getString("name") } ?: return@forEach // compare against name because id changes from -1 to an id when creating a file
            inputFile.id = file.getInt("id")
            inputFile.content = file.getString("content")
        }
    }
}