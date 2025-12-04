package com.ingsisteam.printscriptservice2025.redis.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class Snippet(
    @JsonProperty("userId")
    @JsonAlias("user_id")
    val userId: String,

    @JsonProperty("id")
    @JsonAlias("snippetId", "snippet_id")
    val id: String,

    @JsonProperty("content")
    val content: String,

    @JsonProperty("correlationID")
    @JsonAlias("correlation_id", "correlationId")
    val correlationID: UUID,
) {
    override fun toString(): String {
        return "Snippet{ userId:$userId, id:$id, content:$content, correlationId:$correlationID"
    }
}
