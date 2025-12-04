package com.ingsisteam.printscriptservice2025.service

import com.ingsisteam.printscriptservice2025.model.Output
import com.ingsisteam.printscriptservice2025.model.SCAOutput
import com.ingsisteam.printscriptservice2025.redis.dto.Snippet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.InputStream
import java.util.UUID

class DefaultRedisServiceTest {

    private lateinit var snippetService: PrintScriptService
    private lateinit var redisService: DefaultRedisService

    @BeforeEach
    fun setUp() {
        snippetService = mock()
        redisService = DefaultRedisService(snippetService)
    }

    @Test
    fun `formatSnippet should call snippetService and return formatted snippet`() {
        val snippet = Snippet("user1", "id1", "let x=1", UUID.randomUUID())
        val formattedContent = "let x = 1;"
        val output = Output(formattedContent)

        whenever(snippetService.format(any(), any(), any(), any(), any())).thenReturn(output)

        val result = redisService.formatSnippet(snippet)

        assertEquals(formattedContent, result.content)
        assertEquals(snippet.id, result.id)
        assertEquals(snippet.userId, result.userId)
        assertEquals(snippet.correlationID, result.correlationID)
        verify(snippetService).format(any(), any<InputStream>(), any(), any(), any())
    }

    @Test
    fun `lintSnippet should call snippetService and return linted snippet`() {
        val snippet = Snippet("user1", "id1", "let x=1", UUID.randomUUID())
        val lintResults = mutableListOf(
            SCAOutput(1, "rule1", "desc1"),
            SCAOutput(2, "rule2", "desc2"),
        )
        val expectedContent = "Rule: rule1, Line: 1, Description: desc1\nRule: rule2, Line: 2, Description: desc2"

        whenever(snippetService.lint(any(), any(), any(), any())).thenReturn(lintResults)

        val result = redisService.lintSnippet(snippet)

        assertEquals(expectedContent, result.content)
        assertEquals(snippet.id, result.id)
        assertEquals(snippet.userId, result.userId)
        assertEquals(snippet.correlationID, result.correlationID)
        verify(snippetService).lint(any<InputStream>(), any(), any(), any())
    }

    @Test
    fun `testSnippet should return success when snippet executes correctly`() {
        val snippet = Snippet("user1", "id1", "let x=1", UUID.randomUUID())
        whenever(snippetService.runScript(any(), any())).thenReturn(Output(""))

        val result = redisService.testSnippet(snippet)

        assertEquals("success - Snippet executed without errors", result.content)
        verify(snippetService).runScript(any<InputStream>(), any())
    }

    @Test
    fun `testSnippet should return failure when snippet execution fails`() {
        val snippet = Snippet("user1", "id1", "let x=1", UUID.randomUUID())
        val errorMessage = "Syntax error"
        whenever(snippetService.runScript(any(), any())).thenThrow(RuntimeException(errorMessage))

        val result = redisService.testSnippet(snippet)

        assertTrue(result.content.startsWith("failure - Error:"))
        assertTrue(result.content.contains(errorMessage))
        verify(snippetService).runScript(any<InputStream>(), any())
    }
}
