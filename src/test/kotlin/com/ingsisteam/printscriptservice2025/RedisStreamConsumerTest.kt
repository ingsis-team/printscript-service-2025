package com.ingsisteam.printscriptservice2025

import com.ingsisteam.printscriptservice2025.redis.consumer.RedisStreamConsumer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.connection.ReactiveRedisConnection
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroup
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import kotlin.test.assertEquals

class RedisStreamConsumerTest {

    private lateinit var redisTemplate: ReactiveRedisTemplate<String, String>
    private lateinit var connectionFactory: ReactiveRedisConnectionFactory
    private lateinit var connection: ReactiveRedisConnection

    @BeforeEach
    fun setUp() {
        redisTemplate = mock()
        connectionFactory = mock()
        connection = mock()
        whenever(redisTemplate.connectionFactory).thenReturn(connectionFactory)
        whenever(connectionFactory.reactiveConnection).thenReturn(connection)
    }

    // Concrete test class
    class TestRedisStreamConsumer(
        redis: ReactiveRedisTemplate<String, String>,
    ) : RedisStreamConsumer<String>("test-stream", "test-group", redis) {
        var lastProcessedRecord: String? = null

        public override fun onMessage(record: ObjectRecord<String, String>) {
            lastProcessedRecord = record.value
        }

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, String>> {
            return StreamReceiver.StreamReceiverOptions.builder()
                .pollTimeout(Duration.ofMillis(100))
                .targetType(String::class.java)
                .build()
        }
    }

    @Test
    fun `subscription should create consumer group if it does not exist`() {
        val streamOps: org.springframework.data.redis.core.ReactiveStreamOperations<String, Any, Any> = mock()
        whenever(redisTemplate.opsForStream<Any, Any>()).thenReturn(streamOps)
        whenever(streamOps.groups(any())).thenReturn(Flux.empty())
        whenever(streamOps.createGroup(any<String>(), any<String>())).thenReturn(Mono.just("OK"))
        whenever(connectionFactory.getReactiveConnection()).thenReturn(connection)

        val consumer = TestRedisStreamConsumer(redisTemplate)

        runBlocking {
            consumer.subscription()
        }

        verify(streamOps).createGroup(eq("test-stream"), eq("test-group"))
    }

    @Test
    fun `subscription should not create consumer group if it exists`() {
        val streamOps: org.springframework.data.redis.core.ReactiveStreamOperations<String, Any, Any> = mock()
        val xInfoGroup = mock<XInfoGroup>()
        whenever(xInfoGroup.groupName()).thenReturn("test-group")
        whenever(redisTemplate.opsForStream<Any, Any>()).thenReturn(streamOps)
        whenever(streamOps.groups(any())).thenReturn(Flux.just(xInfoGroup))

        val consumer = TestRedisStreamConsumer(redisTemplate)

        runBlocking {
            consumer.subscription()
        }

        verify(streamOps, never()).createGroup(any<String>(), any<String>())
    }

    @Test
    fun `subscription should create consumer group on exception`() {
        val streamOps: org.springframework.data.redis.core.ReactiveStreamOperations<String, Any, Any> = mock()
        whenever(redisTemplate.opsForStream<Any, Any>()).thenReturn(streamOps)
        whenever(streamOps.groups(any())).thenThrow(RuntimeException("Redis error"))
        whenever(streamOps.createGroup(any<String>(), any<String>())).thenReturn(Mono.just("OK"))

        val consumer = TestRedisStreamConsumer(redisTemplate)

        runBlocking {
            consumer.subscription()
        }

        verify(streamOps).createGroup(eq("test-stream"), eq("test-group"))
    }

    @Test
    fun `test onMessage execution`() {
        val consumer = TestRedisStreamConsumer(redisTemplate)
        val dummyRecord = ObjectRecord.create("test-stream", "test-message")
        consumer.onMessage(dummyRecord)
        assertEquals("test-message", consumer.lastProcessedRecord)
    }
}
