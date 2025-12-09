package com.ingsisteam.printscriptservice2025.model.dto
import java.util.*

data class SnippetDTO(
    val correlationId: UUID,
    val snippetId: String,
    val language: String,
    val version: String,
    val input: String,
    val userId: String,
)
