package com.ingsis_team.printscript_service_2025.service

import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock

class DefaultRedisServiceTest {

    private lateinit var snippetService: PrintScriptService
    private lateinit var redisService: DefaultRedisService

    @BeforeEach
    fun setUp() {
        snippetService = mock(PrintScriptService::class.java)
        redisService = DefaultRedisService(snippetService)
    }
}