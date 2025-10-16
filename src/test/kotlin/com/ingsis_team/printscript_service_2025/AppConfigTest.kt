package com.ingsis_team.printscript_service_2025

import formatter.FormatterPS
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import com.ingsis_team.printscript_service_2025.AppConfig

class AppConfigTest {
    private val appConfig = AppConfig()

    @Test
    fun `restTemplate bean should be created`() {
        val restTemplate = appConfig.restTemplate()
        assertNotNull(restTemplate, "RestTemplate bean should not be null")
    }

    @Test
    fun `formatter bean should be created`() {
        val formatter = appConfig.formatter()
        assertNotNull(formatter, "FormatterPS bean should not be null")
    }

    @Test
    fun `tokenMapper bean should be created`() {
        val tokenMapper = appConfig.tokenMapper()
        assertNotNull(tokenMapper, "TokenMapper bean should not be null")
    }

    @Test
    fun `parser bean should be created`() {
        val parser = appConfig.parser()
        assertNotNull(parser, "Parser bean should not be null")
    }

    @Test
    fun `linter bean should be created`() {
        val linter = appConfig.linter()
        assertNotNull(linter, "Linter bean should not be null")
    }

    @Test
    fun `formatter bean should load valid rules path`() {
        val formatter = appConfig.formatter()
        val rulesPathField = FormatterPS::class.java.getDeclaredField("rulesPath")
        rulesPathField.isAccessible = true
        val rulesPath = rulesPathField.get(formatter) as String

        assertNotNull(rulesPath, "Rules path should not be null")
        println("Rules path: $rulesPath")
    }
}
