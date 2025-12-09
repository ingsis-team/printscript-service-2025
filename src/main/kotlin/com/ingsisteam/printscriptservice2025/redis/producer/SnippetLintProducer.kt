package com.ingsisteam.printscriptservice2025.redis.producer

import com.ingsisteam.printscriptservice2025.redis.dto.Snippet
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component

@Component
class SnippetLintProducer
@Autowired
constructor(
    @Value("\${stream.key.lint}") streamKey: String,
    redis: ReactiveRedisTemplate<String, String>,
) : RedisStreamProducer(streamKey, redis) {
    private val logger = LoggerFactory.getLogger(SnippetLintProducer::class.java)

    suspend fun publishEvent(snippet: Snippet) {
        logger.info("Publishing lint event for snippetId: ${snippet.id}, userId: ${snippet.userId}, correlationId: ${snippet.correlationID}")
        emit(snippet).awaitSingle()
        logger.debug("Lint event published successfully to stream: $streamKey")
    }
}
