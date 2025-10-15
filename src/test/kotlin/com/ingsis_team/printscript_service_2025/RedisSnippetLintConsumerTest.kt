package com.ingsis_team.printscript_service_2025

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.data.redis.core.ReactiveRedisTemplate
import com.ingsis_team.printscript_service_2025.redis.consumer.SnippetLintConsumer
import com.ingsis_team.printscript_service_2025.service.DefaultRedisService
import java.time.Duration

class RedisSnippetLintConsumerTest {
    /*
    @Suppress("UNCHECKED_CAST")
    @Test
    fun `onMessage processes record successfully`() {
        // Crear mocks básicos
        val redisMock = Mockito.mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, String>
        val serviceMock = Mockito.mock(DefaultRedisService::class.java)

        // Crear datos de prueba
        val snippet = printscript.redis.dto.Snippet("examepleUserId", "exampleId", "exampleContent", java.util.UUID.randomUUID())
        val record = ObjectRecord.create("test-stream", snippet)

        // Instanciar la clase directamente
        val consumer = SnippetLintConsumer(redisMock, "test-stream", "test-group", serviceMock)

        // Ejecutar el método `onMessage`
        consumer.onMessage(record)

        // Verificar que el servicio fue invocado correctamente
        Mockito.verify(serviceMock).lintSnippet(snippet)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `onMessage handles exceptions gracefully`() {
        // Crear mocks básicos
        val redisMock = Mockito.mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, String>
        val serviceMock = Mockito.mock(printscript.service.DefaultRedisService::class.java)


        // Crear datos de prueba
        val snippet = printscript.redis.dto.Snippet("exampleUserId", "exampleId", "exampleContent", java.util.UUID.randomUUID())
        val record = ObjectRecord.create("test-stream", snippet)

        // Instanciar la clase directamente
        val consumer = SnippetLintConsumer(redisMock, "test-stream", "test-group", serviceMock)

        // Ejecutar el método `onMessage`
        consumer.onMessage(record)

        // Verificar que el servicio fue invocado incluso cuando ocurrió una excepción
        Mockito.verify(serviceMock).lintSnippet(eq(snippet))
    }

     */

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `options returns correct configuration`() {
        // Crear mocks básicos
        val redisMock = Mockito.mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, String>
        val serviceMock = Mockito.mock(DefaultRedisService::class.java)

        // Instanciar la clase directamente
        val consumer = SnippetLintConsumer(redisMock, "test-stream", "test-group", serviceMock)

        // Ejecutar el método `options`
        val options = consumer.options()

        // Verificar que las configuraciones sean correctas
        assert(options.pollTimeout == Duration.ofMillis(100))
    }
}
