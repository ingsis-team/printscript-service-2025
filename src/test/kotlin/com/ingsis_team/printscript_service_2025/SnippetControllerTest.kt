package com.ingsis_team.printscript_service_2025

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.http.HttpClient

class SnippetControllerTest {
    private val client = HttpClient.newHttpClient()
    private val baseUrl = "http://localhost:8080" // Cambia al puerto donde corre tu servidor.

    private val objectMapper = jacksonObjectMapper()
}
