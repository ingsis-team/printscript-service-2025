package com.ingsis_team.printscript_service_2025

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import com.ingsis_team.printscript_service_2025.redis.consumer.SnippetFormatMessage

class RedisSnippetFormatMessageTest {
    @Test
    fun `test SnippetFormatMessage properties and equality`() {
        val userId = "12345"
        val snippetId = "67890"
        val snippet = "Sample Snippet"

        // Crear instancia
        val message = SnippetFormatMessage(userId, snippetId, snippet)

        // Verificar propiedades
        assertEquals(userId, message.userId)
        assertEquals(snippetId, message.snippetId)
        assertEquals(snippet, message.snippet)

        // Verificar igualdad
        val sameMessage = SnippetFormatMessage(userId, snippetId, snippet)
        assertEquals(message, sameMessage)

        // Verificar hashCode
        assertEquals(message.hashCode(), sameMessage.hashCode())

        // Verificar toString
        val expectedString = "SnippetFormatMessage(userId=12345, snippetId=67890, snippet=Sample Snippet)"
        assertEquals(expectedString, message.toString())
    }
}
