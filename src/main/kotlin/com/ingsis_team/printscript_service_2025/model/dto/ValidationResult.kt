package com.ingsis_team.printscript_service_2025.model.dto

data class ValidationResult(
    val isValid: Boolean,
    val rule: String,
    val line: Int,
    val column: Int,
)
