package com.ingsisteam.printscriptservice2025

import com.ingsisteam.printscriptservice2025.redis.consumer.SnippetFormatterConsumer
import com.ingsisteam.printscriptservice2025.redis.dto.Snippet
import com.ingsisteam.printscriptservice2025.service.DefaultRedisService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import java.time.Duration
import java.util.UUID

class RedisSnippetFormatterConsumerTest {

    private lateinit var redisMock: ReactiveRedisTemplate<String, String>
    private lateinit var serviceMock: DefaultRedisService
    private lateinit var consumer: SnippetFormatterConsumer

    @BeforeEach
    fun setUp() {
        redisMock = mock()
        serviceMock = mock()
        consumer = SnippetFormatterConsumer(redisMock, "test-stream", "test-group", serviceMock)
    }

    @Test
    fun `onMessage should process record and call formatSnippet`() {
        // Arrange
        val snippet = Snippet("exampleId", "exampleContent", "exampleUserId", UUID.randomUUID())
        val record = ObjectRecord.create("test-stream", snippet)

        // Act
        consumer.onMessage(record)

        // Assert
        verify(serviceMock).formatSnippet(snippet)
    }

    @Test
    fun `onMessage should handle exceptions gracefully`() {
        // Arrange
        val snippet = Snippet("exampleId", "exampleContent", "exampleUserId", UUID.randomUUID())
        val record = ObjectRecord.create("test-stream", snippet)
        whenever(serviceMock.formatSnippet(any())).thenThrow(RuntimeException("Test exception"))

        // Act & Assert
        consumer.onMessage(record) // Should not throw an exception
        verify(serviceMock).formatSnippet(snippet)
    }

    @Test
    fun `options should return correct configuration`() {
        // Act
        val options = consumer.options()

        // Assert
        assertEquals(Duration.ofMillis(100), options.pollTimeout)
        assertEquals(Snippet::class.java, options.targetType)
    }
}
