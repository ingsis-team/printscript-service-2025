package com.ingsis_team.printscript_service_2025.redis.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Rule(
    val id: String,
    @JsonProperty("name")
    @JsonAlias("rule_name")
    val name: String,
    @JsonProperty("isActive")
    @JsonAlias("is_active")
    val isActive: Boolean,
    val value: Any?,
)
