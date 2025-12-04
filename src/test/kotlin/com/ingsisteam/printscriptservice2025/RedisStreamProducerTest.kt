package com.ingsisteam.printscriptservice2025

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveStreamOperations
import reactor.core.publisher.Mono
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedisStreamProducerTest {

    private lateinit var redisTemplate: ReactiveRedisTemplate<String, String>
    private lateinit var streamOperations: ReactiveStreamOperations<String, String, Any>
    private lateinit var producer: RedisStreamProducer

    @BeforeEach
    fun setUp() {
        redisTemplate = mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, String>
        streamOperations = mock(ReactiveStreamOperations::class.java) as ReactiveStreamOperations<String, String, Any>
        `when`(redisTemplate.opsForStream<String, Any>()).thenReturn(streamOperations)
        producer = object : RedisStreamProducer("testStream", redisTemplate) {}
    }

    @Test
    fun `emit should return Mono of RecordId`() {
        val testValue = "TestValue"
        val record = StreamRecords.newRecord().ofObject(testValue).withStreamKey("testStream")
        val recordId = RecordId.of("123-0")
        `when`(streamOperations.add(record)).thenReturn(Mono.just(recordId))

        val result = producer.emit(testValue)

        assertTrue(result is Mono<RecordId>, "emit should return Mono<RecordId>")
        assertEquals(recordId, result.block())
    }
}
