package com.ingsisteam.printscriptservice2025.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.filter.CorsFilter

class CorsConfigTest {

    @Test
    fun `corsFilter should register configuration with expected values`() {
        val corsConfig = CorsConfig()
        val filter: CorsFilter = corsConfig.corsFilter()

        // Access the protected configSource inside CorsFilter via reflection
        val field = CorsFilter::class.java.getDeclaredField("configSource")
        field.isAccessible = true
        val configSource = field.get(filter) as CorsConfigurationSource

        val request = MockHttpServletRequest()
        request.requestURI = "/any/path"

        val config: CorsConfiguration? = configSource.getCorsConfiguration(request)
        requireNotNull(config) { "CorsConfiguration should not be null" }

        // Origins
        val origins = config.allowedOrigins ?: emptyList()
        assertTrue(origins.contains("http://localhost"))
        assertTrue(origins.contains("http://localhost:5173"))
        assertTrue(origins.contains("https://snippet-prueba.duckdns.org"))

        // Methods
        val methods = config.allowedMethods ?: emptyList()
        assertTrue(methods.containsAll(listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")))

        // Headers
        val headers = config.allowedHeaders ?: emptyList()
        assertEquals(listOf("*"), headers)

        // Credentials
        assertTrue(config.allowCredentials == true)

        // Exposed headers
        val exposed = config.exposedHeaders ?: emptyList()
        assertTrue(exposed.containsAll(listOf("Authorization", "Content-Type", "X-Correlation-Id", "Correlation-id")))

        // Max age
        assertEquals(3600L, config.maxAge ?: -1L)
    }
}
