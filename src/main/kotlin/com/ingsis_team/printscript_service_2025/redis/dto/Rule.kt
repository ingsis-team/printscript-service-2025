package com.ingsis_team.printscript_service_2025.redis.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Rule(
    val name: String,
    val value: Any,
)
