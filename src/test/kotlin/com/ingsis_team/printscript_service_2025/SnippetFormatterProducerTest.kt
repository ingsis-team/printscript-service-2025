package com.ingsis_team.printscript_service_2025

import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.springframework.data.redis.core.ReactiveRedisTemplate

class SnippetFormatterProducerTest {

    private lateinit var redisTemplate: ReactiveRedisTemplate<String, String>
    private lateinit var producer: SnippetFormatterProducer

    @BeforeEach
    fun setUp() {
        redisTemplate = mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, String>
        producer = SnippetFormatterProducer("testStream", redisTemplate)
    }

}