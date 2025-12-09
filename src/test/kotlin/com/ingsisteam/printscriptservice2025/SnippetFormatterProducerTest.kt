package com.ingsisteam.printscriptservice2025

import com.ingsisteam.printscriptservice2025.redis.dto.Snippet
import com.ingsisteam.printscriptservice2025.redis.producer.SnippetFormatterProducer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveStreamOperations
import reactor.core.publisher.Mono
import java.util.UUID

class SnippetFormatterProducerTest {

    private lateinit var redisTemplate: ReactiveRedisTemplate<String, String>
    private lateinit var streamOps: ReactiveStreamOperations<String, String, com.ingsisteam.printscriptservice2025.redis.dto.Snippet>
    private lateinit var producer: SnippetFormatterProducer

    @BeforeEach
    fun setUp() {
        @Suppress("UNCHECKED_CAST")
        val template = Mockito.mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, String>

        @Suppress("UNCHECKED_CAST")
        val ops = Mockito.mock(ReactiveStreamOperations::class.java) as ReactiveStreamOperations<String, String, com.ingsisteam.printscriptservice2025.redis.dto.Snippet>
        whenever(template.opsForStream<String, com.ingsisteam.printscriptservice2025.redis.dto.Snippet>()).thenReturn(ops)
        redisTemplate = template
        streamOps = ops
        producer = SnippetFormatterProducer("testStream", redisTemplate)
    }

    @Test
    fun `publishEvent should emit snippet to stream`() {
        val snippet = Snippet("user", "id1", "content", UUID.randomUUID())
        whenever(streamOps.add(any<org.springframework.data.redis.connection.stream.Record<String, com.ingsisteam.printscriptservice2025.redis.dto.Snippet>>()))
            .thenReturn(Mono.just(RecordId.of("1-0")))

        runBlocking {
            producer.publishEvent(snippet)
        }
    }
}
