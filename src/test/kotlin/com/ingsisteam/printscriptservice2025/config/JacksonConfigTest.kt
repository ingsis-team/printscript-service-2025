package com.ingsisteam.printscriptservice2025.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

class JacksonConfigTest {

    data class SampleDto(val firstName: String, val lastName: String)

    @Test
    fun `jacksonObjectMapper should configure snake_case and features`() {
        val config = JacksonConfig()
        val builder = Jackson2ObjectMapperBuilder()
        val mapper: ObjectMapper = config.jacksonObjectMapper(builder)

        // Naming strategy
        assertEquals(PropertyNamingStrategies.SNAKE_CASE, mapper.propertyNamingStrategy)

        // Features
        assertTrue(mapper.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES))
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES))

        // Serialize camelCase -> snake_case
        val json = mapper.writeValueAsString(SampleDto("John", "Doe"))
        assertTrue(json.contains("first_name"))
        assertTrue(json.contains("last_name"))

        // Deserialize with different case in properties and unknown property should be ignored
        val inputJson = """{"FIRST_NAME":"Jane","Last_Name":"Roe","unknown_field":123}"""
        val back = mapper.readValue(inputJson, SampleDto::class.java)
        assertEquals("Jane", back.firstName)
        assertEquals("Roe", back.lastName)
    }
}
