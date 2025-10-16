package com.ingsis_team.printscript_service_2025

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.ReactiveRedisTemplate
import com.ingsis_team.printscript_service_2025.redis.dto.Snippet
import reactor.core.publisher.Mono

abstract class RedisStreamProducer(
    val streamKey: String,
    val redis: ReactiveRedisTemplate<String, String>,
) {
    inline fun <reified Value : Any> emit(value: Value): Mono<RecordId> {
        val record = StreamRecords.newRecord().ofObject(value).withStreamKey(streamKey)
        return redis.opsForStream<String, Value>().add(record)
    }
}

class SnippetFormatterProducer(streamKey: String, redis: ReactiveRedisTemplate<String, String>) :
    RedisStreamProducer(streamKey, redis) {
    suspend fun publishEvent(snippet: Snippet) {
        println("Publishing on stream: $streamKey")
        emit(snippet).awaitSingle()
    }
}

class SnippetLintProducer(streamKey: String, redis: ReactiveRedisTemplate<String, String>) :
    RedisStreamProducer(streamKey, redis) {
    suspend fun publishEvent(snippet: Snippet) {
        println("Publishing on stream: $streamKey")
        emit(snippet).awaitSingle()
    }
}

class RedisProducerTest {
    /*
    @Test
    fun `test RedisStreamProducer emit method`() {
        val redisTemplate =
            ReactiveRedisTemplate(
                null,
                RedisSerializationContext.newSerializationContext<String, String>()
                    .key(StringRedisSerializer())
                    .value(StringRedisSerializer())
                    .build(),
            )
        val producer = object : RedisStreamProducer("testStream", redisTemplate) {}

        val testValue = "TestValue"
        val record = StreamRecords.newRecord().ofObject(testValue).withStreamKey("testStream")
        val result: Mono<RecordId> = producer.emit(testValue)

        assertTrue(result is Mono<RecordId>, "emit should return Mono<RecordId>")
    }

    @Test
    fun `test SnippetFormatterProducer publishEvent`() =
        runBlocking {
            val redisTemplate =
                ReactiveRedisTemplate(
                    null,
                    RedisSerializationContext.newSerializationContext<String, String>()
                        .key(StringRedisSerializer())
                        .value(StringRedisSerializer())
                        .build(),
                )
            val producer = SnippetFormatterProducer("snippetFormatStream", redisTemplate)

            val snippet = Snippet("user1", "snippet1", "content", UUID.randomUUID())
            producer.publishEvent(snippet)
        }

    @Test
    fun `test SnippetLintProducer publishEvent`() =
        runBlocking {
            val redisTemplate =
                ReactiveRedisTemplate(
                    null,
                    RedisSerializationContext.newSerializationContext<String, String>()
                        .key(StringRedisSerializer())
                        .value(StringRedisSerializer())
                        .build(),
                )
            val producer = SnippetLintProducer("snippetLintStream", redisTemplate)

            val snippet = Snippet("user2", "snippet2", "code content", UUID.randomUUID())
            producer.publishEvent(snippet)
        }

     */
}
