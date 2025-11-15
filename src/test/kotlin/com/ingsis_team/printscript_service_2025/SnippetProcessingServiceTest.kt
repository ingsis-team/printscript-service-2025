package com.ingsis_team.printscript_service_2025

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.web.context.WebApplicationContext
import com.ingsis_team.printscript_service_2025.service.PrintScriptService
import com.ingsis_team.printscript_service_2025.service.SnippetProcessingService

class SnippetProcessingServiceTest {
    private lateinit var applicationContext: WebApplicationContext
    private lateinit var snippetProcessingService: SnippetProcessingService

    @BeforeEach
    fun setUp() {
        applicationContext = mock(WebApplicationContext::class.java)
        snippetProcessingService = SnippetProcessingService(applicationContext)
    }

    @Test
    fun `selectService should return PrintScriptService for language printscript`() {
        val printScriptService = mock(PrintScriptService::class.java)
        `when`(applicationContext.getBean(PrintScriptService::class.java)).thenReturn(printScriptService)

        val result = snippetProcessingService.selectService("printscript")

        assertEquals(printScriptService, result)
        verify(applicationContext).getBean(PrintScriptService::class.java)
    }

    @Test
    fun `selectService should throw exception for unsupported language`() {
        val unsupportedLanguage = "python"

        val exception =
            assertThrows(com.ingsis_team.printscript_service_2025.exception.UnsupportedLanguageException::class.java) {
                snippetProcessingService.selectService(unsupportedLanguage)
            }

        assertEquals("Unsupported language: $unsupportedLanguage. Only 'printscript' is supported.", exception.message)
        verifyNoInteractions(applicationContext)
    }
}
