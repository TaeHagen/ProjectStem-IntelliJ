package com.github.taehagen.projectstemintellij.projectmanager

data class GraderExample (
    val expected: String,
    val found: String
)

data class GraderResult (
    val description: String,
    val debug_hint: String,
    val passed: Boolean,
    val examples: ArrayList<GraderExample> = ArrayList()
)

// grade is number 0-100
class Submission(val item: Item) {
    var id: Int = 0
    var status: String = ""
    var error: String? = null
    var results = ArrayList<GraderResult>()
    var grade: Int = 0
    fun checkSubmission() {
        if (!isSubmitting())
            return
        Remote.checkSubmission(this)
    }

    fun isSubmitted(): Boolean {
        return status == "graded"
    }

    fun isSubmitting(): Boolean {
        return status == "submitting"
    }

    fun grade(): Boolean {
        if (isSubmitted())
            return true
        return Remote.gradeSubmission(this)
    }
}