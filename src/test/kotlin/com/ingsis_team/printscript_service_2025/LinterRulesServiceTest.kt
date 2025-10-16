package com.ingsis_team.printscript_service_2025

import com.ingsis_team.printscript_service_2025.model.repository.LinterRulesRepository
import com.ingsis_team.printscript_service_2025.service.LinterRulesService

class LinterRulesServiceTest {
    private lateinit var linterRulesRepository: LinterRulesRepository
    private lateinit var linterRulesService: LinterRulesService
/*
    @Test
    fun `getLinterRulesByUserId should return existing rules`() {
        val userId = "user123"
        val correlationId = UUID.randomUUID()
        val existingRules =
            LinterRules(
                userId = userId,
                identifierFormat = "snake_case",
                enablePrintOnly = true,
                enableInputOnly = false,
            )
        linterRulesRepository.save(existingRules)

        val result = linterRulesService.getLinterRulesByUserId(userId, correlationId)

        assertEquals(existingRules, result)
    }

    @Test
    fun `getLinterRulesByUserId should create new rules if not found`() {
        val userId = "newUser"
        val correlationId = UUID.randomUUID()

        val result = linterRulesService.getLinterRulesByUserId(userId, correlationId)

        assertEquals(userId, result.userId)
        assertEquals("camelcase", result.identifierFormat)
        assertEquals(false, result.enablePrintOnly)
        assertEquals(false, result.enableInputOnly)
    }

    @Test
    fun `updateLinterRules should update and return updated rules`() {
        val userId = "userToUpdate"
        val existingRules =
            LinterRules(
                userId = userId,
                identifierFormat = "camelcase",
                enablePrintOnly = false,
                enableInputOnly = false,
            )
        linterRulesRepository.save(existingRules)

        val updatedDto =
            LinterRulesFileDTO(
                userId = userId,
                identifier_format = "snake_case",
                enablePrintOnly = true,
                enableInputOnly = true,
            )

        val result = linterRulesService.updateLinterRules(updatedDto, userId)

        assertEquals(updatedDto, result)
        val updatedRules = linterRulesRepository.findByUserId(userId).orElseThrow()
        assertEquals("snake_case", updatedRules.identifierFormat)
        assertEquals(true, updatedRules.enablePrintOnly)
        assertEquals(true, updatedRules.enableInputOnly)
    }

    @Test
    fun `updateLinterRules should return default DTO on exception`() {
        val userId = "exceptionUser"
        val invalidDto =
            LinterRulesFileDTO(
                userId = userId,
                identifier_format = "",
                enablePrintOnly = false,
                enableInputOnly = false,
            )

        val result = linterRulesService.updateLinterRules(invalidDto, userId)

        assertEquals(userId, result.userId)
        assertEquals("camelcase", result.identifier_format)
        assertEquals(false, result.enablePrintOnly)
        assertEquals(false, result.enableInputOnly)
    }

 */
}
