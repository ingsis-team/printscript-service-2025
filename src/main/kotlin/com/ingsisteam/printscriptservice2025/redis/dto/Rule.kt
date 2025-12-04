package com.ingsisteam.printscriptservice2025.redis.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Rule(
    val name: String,
    val value: Any,
)
