package com.ingsis_team.printscript_service_2025.redis.producer

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import com.ingsis_team.printscript_service_2025.redis.dto.Snippet

@Component
class SnippetTestProducer
    @Autowired
    constructor(
        @Value("\${stream.key.test}") streamKey: String,
        redis: ReactiveRedisTemplate<String, String>,
    ) : RedisStreamProducer(streamKey, redis) {
        suspend fun publishEvent(snippet: Snippet) {
            emit(snippet).awaitSingle()
        }
    }

