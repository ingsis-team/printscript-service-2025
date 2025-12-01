package com.ingsis_team.printscript_service_2025.model.dto

// DTO para las reglas de formateo que se intercambian con el cliente
data class FormatterRulesFileDTO(
    val spaceBeforeColon: Boolean,
    val spaceAfterColon: Boolean,
    val spaceAroundEquals: Boolean,
    val lineBreak: Int,
    val lineBreakPrintln: Int,
    val conditionalIndentation: Int,
)
