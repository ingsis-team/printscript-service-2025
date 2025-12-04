package com.ingsisteam.printscriptservice2025

import com.ingsisteam.printscriptservice2025.redis.dto.Snippet
import com.ingsisteam.printscriptservice2025.redis.producer.SnippetLintProducer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.connection.stream.Record
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveStreamOperations
import reactor.core.publisher.Mono
import java.util.UUID

class SnippetLintProducerTest {

    private lateinit var redisTemplate: ReactiveRedisTemplate<String, String>
    private lateinit var streamOps: ReactiveStreamOperations<String, String, Snippet>
    private lateinit var producer: SnippetLintProducer

    @BeforeEach
    fun setUp() {
        redisTemplate = mock()
        streamOps = mock()
        Mockito.doReturn(streamOps).`when`(redisTemplate).opsForStream<String, Snippet>()
        producer = SnippetLintProducer("testStream", redisTemplate)
    }

    @Test
    fun `publishEvent should emit snippet to stream`() {
        val snippet = Snippet("user", "id1", "content", UUID.randomUUID())
        whenever(streamOps.add(any<Record<String, Snippet>>())).thenReturn(Mono.just(RecordId.of("1-0")))

        runBlocking {
            producer.publishEvent(snippet)
        }

        verify(streamOps).add(any<Record<String, Snippet>>())
    }
}
