package com.ingsisteam.printscriptservice2025.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class LinterFileDTO(
    @JsonProperty("identifier_format") // Forzar snake_case para esta propiedad
    val identifier_format: String,
    val enablePrintOnly: Boolean,
    val enableInputOnly: Boolean,
)
