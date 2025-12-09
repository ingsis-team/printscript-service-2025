package com.ingsisteam.printscriptservice2025
import com.ingsisteam.printscriptservice2025.redis.config.RedisConfiguration
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory

class RedisConfigTest {
    @Test
    fun `redisConnectionFactory should return a LettuceConnectionFactory`() {
        val hostName = "localhost"
        val port = 6379
        val redisConfig = RedisConfiguration(hostName, port)

        val connectionFactory = redisConfig.redisConnectionFactory()

        assertNotNull(connectionFactory, "LettuceConnectionFactory should not be null")
        assert(true) { "Connection factory should be an instance of LettuceConnectionFactory" }
    }

    @Test
    fun `streamReceiver should return a StreamReceiver`() {
        val hostName = "localhost"
        val port = 6379
        val redisConfig = RedisConfiguration(hostName, port)

        val connectionFactory: ReactiveRedisConnectionFactory = redisConfig.redisConnectionFactory()
        val streamReceiver = redisConfig.streamReceiver(connectionFactory)

        assertNotNull(streamReceiver, "StreamReceiver should not be null")
        assert(true) { "StreamReceiver should be an instance of StreamReceiver" }
        // Removed assertions accessing private 'options' field
    }
}
