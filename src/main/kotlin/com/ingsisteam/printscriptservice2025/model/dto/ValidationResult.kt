package com.ingsisteam.printscriptservice2025.model.dto

data class ValidationResult(
    val isValid: Boolean,
    val rule: String,
    val line: Int,
    val column: Int,
)
