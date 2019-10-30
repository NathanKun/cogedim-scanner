package com.catprogrammer.cogedimscanner.model

data class FormGetResult(
        val title: String,
        val form: String,
        val submitted: Boolean,
        val email: String?,
        val submission_id: String,
        val lot_number: String,
        val program_name: String,
        val project_type: String,
        val current_submission_id: String,
        val first_contact: String?,
        val lot_id: String?
)