package com.ingsis_team.printscript_service_2025

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import com.ingsis_team.printscript_service_2025.redis.consumer.SnippetFormatterConsumer
import com.ingsis_team.printscript_service_2025.redis.dto.Snippet
import com.ingsis_team.printscript_service_2025.service.DefaultRedisService
import java.util.UUID

class RedisSnippetFormatterConsumerTest {
    @Suppress("UNCHECKED_CAST")
    @Test
    fun `test onMessage processes record`() {
        val redisMock = Mockito.mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, String>
        val serviceMock = Mockito.mock(DefaultRedisService::class.java)
        val snippet = Snippet("exampleId", "exampleContent", "exampleUserId", UUID.randomUUID())
        val record = ObjectRecord.create("test-stream", snippet)
        val consumer = SnippetFormatterConsumer(redisMock, "test-stream", "test-group", serviceMock)

        consumer.onMessage(record)
        Mockito.verify(serviceMock).formatSnippet(snippet)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `onMessage should process record and call formatSnippet`() {
        // Arrange
        val redisMock = Mockito.mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, String>
        val serviceMock = Mockito.mock(DefaultRedisService::class.java)
        val snippet = Snippet("exampleId", "exampleContent", "exampleUserId", UUID.randomUUID())
        val record = ObjectRecord.create("test-stream", snippet)
        val consumer = SnippetFormatterConsumer(redisMock, "test-stream", "test-group", serviceMock)

        // Act
        consumer.onMessage(record)

        // Assert
        Mockito.verify(serviceMock).formatSnippet(snippet)
    }
/*
    @Test
    @Suppress("UNCHECKED_CAST")
    fun `test options builds correct configuration`() {
        // Mock the necessary dependencies
        val redisMock = Mockito.mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, String>
        val serviceMock = Mockito.mock(printscript.service.DefaultRedisService::class.java)

        // Instantiate the class to test
        val consumer = SnippetFormatterConsumer(redisMock, "test-stream", "test-group", serviceMock)

        // Invoke options and verify it returns a valid configuration
        val options = consumer.options()
        assert(options.pollTimeout == Duration.ofMillis(100))
    }

 */
}
