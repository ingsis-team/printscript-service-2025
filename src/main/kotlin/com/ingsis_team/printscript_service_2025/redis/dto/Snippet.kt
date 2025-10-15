package com.ingsis_team.printscript_service_2025.redis.dto

import java.util.*

data class Snippet(val userId: String, val id: String, val content: String, val correlationID: UUID) {
    override fun toString(): String {
        return "Snippet{ userId:$userId, id:$id, content:$content, correlationId:$correlationID"
    }
}
