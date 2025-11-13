package com.ingsis_team.printscript_service_2025

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.data.redis.core.ReactiveRedisTemplate
import com.ingsis_team.printscript_service_2025.redis.consumer.SnippetLintConsumer
import com.ingsis_team.printscript_service_2025.service.DefaultRedisService
import java.time.Duration

class RedisSnippetLintConsumerTest {
    /*
    @Suppress("UNCHECKED_CAST")
    @Test
    fun `onMessage processes record successfully`() {
        // Create basic mocks
        val redisMock = Mockito.mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, String>
        val serviceMock = Mockito.mock(DefaultRedisService::class.java)

        // Create test data
        val snippet = printscript.redis.dto.Snippet("examepleUserId", "exampleId", "exampleContent", java.util.UUID.randomUUID())
        val record = ObjectRecord.create("test-stream", snippet)

        // Instantiate the class directly
        val consumer = SnippetLintConsumer(redisMock, "test-stream", "test-group", serviceMock)

        // Execute the `onMessage` method
        consumer.onMessage(record)

        // Verify that the service was invoked correctly
        Mockito.verify(serviceMock).lintSnippet(snippet)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `onMessage handles exceptions gracefully`() {
        // Create basic mocks
        val redisMock = Mockito.mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, String>
        val serviceMock = Mockito.mock(printscript.service.DefaultRedisService::class.java)


        // Create test data
        val snippet = printscript.redis.dto.Snippet("exampleUserId", "exampleId", "exampleContent", java.util.UUID.randomUUID())
        val record = ObjectRecord.create("test-stream", snippet)

        // Instantiate the class directly
        val consumer = SnippetLintConsumer(redisMock, "test-stream", "test-group", serviceMock)

        // Execute the `onMessage` method
        consumer.onMessage(record)

        // Verify that the service was invoked even when an exception occurred
        Mockito.verify(serviceMock).lintSnippet(eq(snippet))
    }

     */

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `options returns correct configuration`() {
        // Create basic mocks
        val redisMock = Mockito.mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, String>
        val serviceMock = Mockito.mock(DefaultRedisService::class.java)

        // Instantiate the class directly
        val consumer = SnippetLintConsumer(redisMock, "test-stream", "test-group", serviceMock)

        // Execute the `options` method
        val options = consumer.options()

        // Verify that the configurations are correct
        assert(options.pollTimeout == Duration.ofMillis(100))
    }
}
