package com.ingsisteam.printscriptservice2025.model

data class SCAOutput(val lineNumber: Int, val ruleBroken: String, val description: String) {
    fun getBrokenRules(): List<String> {
        return listOf(ruleBroken)
    }
}
