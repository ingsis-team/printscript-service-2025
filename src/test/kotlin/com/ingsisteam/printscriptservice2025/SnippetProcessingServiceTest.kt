package com.ingsisteam.printscriptservice2025

import com.ingsisteam.printscriptservice2025.exception.UnsupportedLanguageException
import com.ingsisteam.printscriptservice2025.service.PrintScriptService
import com.ingsisteam.printscriptservice2025.service.SnippetProcessingService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.web.context.WebApplicationContext

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
            assertThrows(UnsupportedLanguageException::class.java) {
                snippetProcessingService.selectService(unsupportedLanguage)
            }

        assertEquals("Unsupported language: $unsupportedLanguage. Only 'printscript' is supported.", exception.message)
        verifyNoInteractions(applicationContext)
    }

    @Test
    fun `selectService should throw exception for empty language`() {
        val emptyLanguage = ""

        val exception =
            assertThrows(UnsupportedLanguageException::class.java) {
                snippetProcessingService.selectService(emptyLanguage)
            }

        assertEquals("Unsupported language: $emptyLanguage. Only 'printscript' is supported.", exception.message)
        verifyNoInteractions(applicationContext)
    }
}
