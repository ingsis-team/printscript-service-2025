package com.ingsisteam.printscriptservice2025.redis.dto

import java.util.*

data class ExecutionDataDTO(val correlationId: UUID, val snippetId: String, val language: String, val version: String, val input: String) {
    override fun toString(): String {
        return "ExecutionDataDto{ correlationId:$correlationId, snippetId:$snippetId, language:$language, version:$version, input:$input"
    }
}
