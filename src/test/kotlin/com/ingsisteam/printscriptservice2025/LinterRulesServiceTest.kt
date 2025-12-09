package com.ingsisteam.printscriptservice2025

import com.ingsisteam.printscriptservice2025.exception.DatabaseException
import com.ingsisteam.printscriptservice2025.model.dto.LinterRulesFileDTO
import com.ingsisteam.printscriptservice2025.model.repository.LinterRulesRepository
import com.ingsisteam.printscriptservice2025.model.rules.LinterRules
import com.ingsisteam.printscriptservice2025.service.LinterRulesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.*

class LinterRulesServiceTest {

    private lateinit var linterRulesRepository: LinterRulesRepository
    private lateinit var linterRulesService: LinterRulesService

    @BeforeEach
    fun setUp() {
        linterRulesRepository = mock(LinterRulesRepository::class.java)
        linterRulesService = LinterRulesService(linterRulesRepository)
    }

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
        `when`(linterRulesRepository.findByUserId(userId)).thenReturn(Optional.of(existingRules))

        val result = linterRulesService.getLinterRulesByUserId(userId, correlationId)

        assertEquals(existingRules, result)
        verify(linterRulesRepository, never()).save(any()) // Ensure save is not called if rules exist
    }

    @Test
    fun `getLinterRulesByUserId should create default rules if none exist`() {
        val userId = "newUser"
        val correlationId = UUID.randomUUID()
        `when`(linterRulesRepository.findByUserId(userId)).thenReturn(Optional.empty())

        val defaultRules =
            LinterRules(
                userId = userId,
                identifierFormat = "camelcase",
                enablePrintOnly = false,
                enableInputOnly = false,
            )
        `when`(linterRulesRepository.save(any(LinterRules::class.java))).thenReturn(defaultRules)

        val result = linterRulesService.getLinterRulesByUserId(userId, correlationId)

        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(defaultRules.identifierFormat, result.identifierFormat)
        verify(linterRulesRepository).findByUserId(userId)
        verify(linterRulesRepository).save(any(LinterRules::class.java)) // Verify save is called
    }

    @Test
    fun `getLinterRulesByUserId should throw DatabaseException when findByUserId fails`() {
        val userId = "userError"
        val correlationId = UUID.randomUUID()
        `when`(linterRulesRepository.findByUserId(userId)).thenThrow(RuntimeException("DB error"))

        assertThrows(DatabaseException::class.java) {
            linterRulesService.getLinterRulesByUserId(userId, correlationId)
        }
        verify(linterRulesRepository, never()).save(any())
    }

    @Test
    fun `getLinterRulesByUserId should throw DatabaseException when create default rules save fails`() {
        val userId = "userCreateError"
        val correlationId = UUID.randomUUID()
        `when`(linterRulesRepository.findByUserId(userId)).thenReturn(Optional.empty()) // Ensure create is called
        `when`(linterRulesRepository.save(any(LinterRules::class.java))).thenThrow(RuntimeException("DB save error"))

        assertThrows(DatabaseException::class.java) {
            linterRulesService.getLinterRulesByUserId(userId, correlationId)
        }
        verify(linterRulesRepository).save(any(LinterRules::class.java))
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
        `when`(linterRulesRepository.findByUserId(userId)).thenReturn(Optional.of(existingRules))
        `when`(linterRulesRepository.save(any(LinterRules::class.java))).thenReturn(existingRules) // Mock save to return the same instance

        val updatedDto =
            LinterRulesFileDTO(
                userId = userId,
                identifier_format = "snake_case",
                enablePrintOnly = true,
                enableInputOnly = true,
            )

        val result = linterRulesService.updateLinterRules(updatedDto, userId)

        assertEquals(updatedDto.identifier_format, result.identifier_format)
        assertEquals(updatedDto.enablePrintOnly, result.enablePrintOnly)
        assertEquals(updatedDto.enableInputOnly, result.enableInputOnly)

        verify(linterRulesRepository).save(existingRules)
        assertEquals("snake_case", existingRules.identifierFormat) // Verify internal state of existingRules
        assertEquals(true, existingRules.enablePrintOnly)
        assertEquals(true, existingRules.enableInputOnly)
    }

    @Test
    fun `updateLinterRules should throw DatabaseException when save fails`() {
        val userId = "userUpdateError"
        val existingRules = LinterRules(userId = userId)
        `when`(linterRulesRepository.findByUserId(userId)).thenReturn(Optional.of(existingRules))
        `when`(linterRulesRepository.save(any(LinterRules::class.java))).thenThrow(RuntimeException("DB update error"))

        val updatedDto =
            LinterRulesFileDTO(
                userId = userId,
                identifier_format = "snake_case",
                enablePrintOnly = true,
                enableInputOnly = true,
            )

        assertThrows(DatabaseException::class.java) {
            linterRulesService.updateLinterRules(updatedDto, userId)
        }
        verify(linterRulesRepository).save(any(LinterRules::class.java))
    }
}
