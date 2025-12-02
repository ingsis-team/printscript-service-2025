package com.ingsis_team.printscript_service_2025.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
class JacksonConfig {
    @Bean
    fun jacksonObjectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper {
        val mapper = builder.build<ObjectMapper>()
        // Serializar por defecto en snake_case
        mapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        // Aceptar propiedades case-insensitive y ignorar desconocidas
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper
    }
}

