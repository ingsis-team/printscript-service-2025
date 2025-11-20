package com.ingsis_team.printscript_service_2025.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {
    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()

        // Allow frontend origin
        config.allowedOrigins = listOf(
            "http://localhost",
            "http://localhost:80",
            "http://localhost:5173",
            "http://localhost:3000",
            "https://localhost:5173",
            "https://localhost:3000",
        )

        // Allow all methods
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")

        // Allow all headers
        config.allowedHeaders = listOf("*")

        // Allow credentials
        config.allowCredentials = true

        // Expose headers
        config.exposedHeaders = listOf(
            "Authorization",
            "Content-Type",
            "X-Correlation-Id",
            "Correlation-id",
        )

        // Max age for preflight requests (1 hour)
        config.maxAge = 3600L

        // Apply configuration to all endpoints
        source.registerCorsConfiguration("/**", config)

        return CorsFilter(source)
    }
}

