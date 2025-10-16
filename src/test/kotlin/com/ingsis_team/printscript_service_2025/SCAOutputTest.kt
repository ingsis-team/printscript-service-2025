package com.ingsis_team.printscript_service_2025

import com.ingsis_team.printscript_service_2025.model.Output
import com.ingsis_team.printscript_service_2025.model.SCAOutput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SCAOutputTest {
    @Test
    fun `should return a list with ruleBroken when getBrokenRules is called`() {
        // Creando una instancia de SCAOutput con valores de prueba
        val scaOutput =
            SCAOutput(
                lineNumber = 10,
                ruleBroken = "Rule 1",
                description = "This is a broken rule",
            )

        // Llamando al método getBrokenRules y verificando el contenido
        val brokenRules = scaOutput.getBrokenRules()

        // Verificando que la lista devuelta contiene exactamente "Rule 1"
        assertEquals(1, brokenRules.size, "List should contain exactly one broken rule")
        assertTrue(brokenRules.contains("Rule 1"), "Broken rule should be 'Rule 1'")
    }

    @Test
    fun `should create SCAOutput correctly with given values`() {
        // Creando una instancia de SCAOutput con valores de prueba
        val scaOutput =
            SCAOutput(
                lineNumber = 5,
                ruleBroken = "Rule 2",
                description = "Another broken rule",
            )

        // Verificando los valores asignados a los atributos
        assertEquals(5, scaOutput.lineNumber)
        assertEquals("Rule 2", scaOutput.ruleBroken)
        assertEquals("Another broken rule", scaOutput.description)
    }

    @Test
    fun `Output test`() {
        val output = Output("Hello")
        assertEquals("Hello", output.string)
    }
}
