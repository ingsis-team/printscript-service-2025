package com.ingsis_team.printscript_service_2025.redis.dto

import java.util.*

data class ChangeRulesDTO(val userId: String, val rules: List<Rule>, val snippets: List<ExecutionDataDTO>, val correlationId: UUID) {
    override fun toString(): String {
        return "ChangeRulesDto { userId: $userId, rules:$rules, snippets:$snippets, correlationId:$correlationId}"
    }
}
