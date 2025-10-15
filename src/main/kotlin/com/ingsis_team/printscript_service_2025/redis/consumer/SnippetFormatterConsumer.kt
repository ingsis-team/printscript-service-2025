package com.ingsis_team.printscript_service_2025.redis.consumer

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import com.ingsis_team.printscript_service_2025.redis.dto.Snippet
import com.ingsis_team.printscript_service_2025.service.DefaultRedisService
import java.time.Duration

@Component
class SnippetFormatterConsumer
    @Autowired
    constructor(
        redis: ReactiveRedisTemplate<String, String>,
        @Value("\${stream.key.format}") streamKey: String,
        @Value("\${groups.product}") groupId: String,
        private val service: DefaultRedisService,
    ) : RedisStreamConsumer<Snippet>(streamKey, groupId, redis) {
        private val logger = LoggerFactory.getLogger(SnippetFormatterConsumer::class.java)

        public override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, Snippet>> {
            return StreamReceiver.StreamReceiverOptions.builder()
                .pollTimeout(Duration.ofMillis(100)) // Set poll rate
                .targetType(Snippet::class.java) // Set type to de-serialize record
                .build()
        }

        public override fun onMessage(record: ObjectRecord<String, Snippet>) {
            try {
                Thread.sleep(100 * 10)
                logger.info("Id: ${record.id}, Value: ${record.value}, Stream: ${record.stream}, Group: $groupId")
                service.formatSnippet(record.value)
            } catch (e: Exception) {
                logger.info("Error al procesar el snippet: ${e.message}")
                // Continue processing the next message
            }
        }
    }
