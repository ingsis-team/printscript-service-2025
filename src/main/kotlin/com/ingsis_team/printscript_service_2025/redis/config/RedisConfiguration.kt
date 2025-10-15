package com.ingsis_team.printscript_service_2025.redis.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.stream.StreamReceiver
import com.ingsis_team.printscript_service_2025.redis.consumer.SnippetFormatMessage
import java.time.Duration

@Configuration
class RedisConfiguration(
    @Value("\${redis.host}") private val hostName: String,
    @Value("\${redis.port}") private val port: Int,
) {
    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        return LettuceConnectionFactory(RedisStandaloneConfiguration(hostName, port))
    }

    @Bean
    fun streamReceiver(
        connectionFactory: ReactiveRedisConnectionFactory,
    ): StreamReceiver<String, ObjectRecord<String, SnippetFormatMessage>> {
        val options =
            StreamReceiver.StreamReceiverOptions.builder()
                .pollTimeout(Duration.ofMillis(10000))
                .targetType(SnippetFormatMessage::class.java)
                .build()
        return StreamReceiver.create(connectionFactory, options)
    }
}
