package com.ingsis_team.printscript_service_2025.model.dto

data class FormatterRulesFileDTO(
    val userId: String,
    val spaceBeforeColon: Boolean,
    val spaceAfterColon: Boolean,
    val spaceAroundEquals: Boolean,
    val lineBreak: Int,
    val lineBreakPrintln: Int,
    val conditionalIndentation: Int,
)
