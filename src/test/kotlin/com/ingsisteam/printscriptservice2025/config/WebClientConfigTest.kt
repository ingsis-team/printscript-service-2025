package com.ingsisteam.printscriptservice2025.config

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

class WebClientConfigTest {

    @AfterEach
    fun tearDown() {
        MDC.clear()
    }

    private class CapturingExchangeFunction : ExchangeFunction {
        var lastRequest: ClientRequest? = null
        override fun exchange(request: ClientRequest): Mono<ClientResponse> {
            lastRequest = request
            val response = ClientResponse.create(HttpStatus.OK).build()
            return Mono.just(response)
        }
    }

    @Test
    fun `webClientBuilder should propagate X-Request-ID from MDC`() {
        val config = WebClientConfig()
        val builder: WebClient.Builder = config.webClientBuilder()

        val capturing = CapturingExchangeFunction()
        val client = builder.exchangeFunction(capturing).build()

        // With MDC value -> header should be added
        MDC.put(WebClientConfig.REQUEST_ID_MDC_KEY, "abc-123")
        client.get().uri("http://test/hello").retrieve().toBodilessEntity().block()
        val headersWith = capturing.lastRequest!!.headers()
        assertEquals("abc-123", headersWith.getFirst(WebClientConfig.REQUEST_ID_HEADER))

        // Without MDC value -> header should be absent
        MDC.clear()
        client.get().uri("http://test/hello2").retrieve().toBodilessEntity().block()
        val headersWithout = capturing.lastRequest!!.headers()
        assertNull(headersWithout.getFirst(WebClientConfig.REQUEST_ID_HEADER))
    }
}
