package com.ingsis_team.printscript_service_2025
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.stream.StreamReceiver
import com.ingsis_team.printscript_service_2025.redis.config.RedisConfiguration

class RedisConfigTest {
    @Test
    fun `redisConnectionFactory should return a LettuceConnectionFactory`() {
        val hostName = "localhost"
        val port = 6379
        val redisConfig = RedisConfiguration(hostName, port)

        val connectionFactory = redisConfig.redisConnectionFactory()

        assertNotNull(connectionFactory, "LettuceConnectionFactory should not be null")
        assert(connectionFactory is LettuceConnectionFactory) { "Connection factory should be an instance of LettuceConnectionFactory" }
    }

    @Test
    fun `streamReceiver should return a StreamReceiver`() {
        val hostName = "localhost"
        val port = 6379
        val redisConfig = RedisConfiguration(hostName, port)

        val connectionFactory: ReactiveRedisConnectionFactory = redisConfig.redisConnectionFactory()
        val streamReceiver = redisConfig.streamReceiver(connectionFactory)

        assertNotNull(streamReceiver, "StreamReceiver should not be null")
        assert(streamReceiver is StreamReceiver<*, *>) { "StreamReceiver should be an instance of StreamReceiver" }
    }
}
