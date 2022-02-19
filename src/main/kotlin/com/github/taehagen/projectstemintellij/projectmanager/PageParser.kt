package com.github.taehagen.projectstemintellij.projectmanager

import org.json.JSONObject
import org.jsoup.Jsoup

object PageParser {
    fun parseAssignment(url: String, token: String, item: Item) {
        val doc = Jsoup.connect(url).cookie("_normandy_session", token).get()
        val form = doc.getElementById("tool_form")
        if (form == null) {
            println("no form!")
            return
        }
        val toolurl = form.attr("action")
        val tool = Jsoup.connect(toolurl)
        for (elem in form.children()) {
            if (elem.hasAttr("name"))
                tool.data(elem.attr("name"), elem.attr("value"))
        }
        val document = tool.post()
        val runner = document.getElementById("coderunner")
        if (runner == null) {
            println("no form!")
            return
        }
        val data = runner.dataset()["props"]
        val json = JSONObject(data)
        item.description = json.getJSONObject("settings").getString("instructions")
        item.problem_id = json.getJSONObject("settings").getInt("problem_id")
        item.ltiparams = json.getJSONObject("settings").getJSONObject("lti_params")
        item.lti_course_id = item.ltiparams.getString("lti_course_id")
        item.lti_user_id = item.ltiparams.getString("lti_user_id")
        item.files.clear()
        val files = json.getJSONArray("files")
        val currentFile = json.getJSONObject("meta").getJSONObject("current_file").getInt("id")
        (0 until files.length()).forEach {
            val file = files.getJSONObject(it)
            item.files.add(File(file.getInt("id"), file.getString("name"), file.getString("content"), file.getInt("id") == currentFile, if (file.has("version_number")) file.getInt("version_number") else 1))
        }
        if (json.isNull("submission")) {
            item.submission = null
        } else {
            val sub = Submission(item)
            Remote.parseSubmission(json, sub)
            item.submission = sub
        }
    }
}