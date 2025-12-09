package com.ingsisteam.printscriptservice2025
import com.ingsisteam.printscriptservice2025.exception.DatabaseException
import com.ingsisteam.printscriptservice2025.model.dto.FormatterRulesFileDTO
import com.ingsisteam.printscriptservice2025.model.repository.FormatterRulesRepository
import com.ingsisteam.printscriptservice2025.model.rules.FormatterRules
import com.ingsisteam.printscriptservice2025.service.FormatterRulesService
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
import java.util.Optional
import java.util.UUID

class FormatterRulesServiceTest {

    private lateinit var formatterRulesRepository: FormatterRulesRepository
    private lateinit var formatterRulesService: FormatterRulesService

    @BeforeEach
    fun setUp() {
        formatterRulesRepository = mock(FormatterRulesRepository::class.java)
        formatterRulesService = FormatterRulesService(formatterRulesRepository)
    }

    @Test
    fun `getFormatterRulesByUserId should return existing rules`() {
        val userId = "user123"
        val correlationId = UUID.randomUUID()
        val existingRules = FormatterRules(
            userId = userId,
            spaceBeforeColon = true,
            spaceAfterColon = true,
            spaceAroundEquals = true,
            lineBreak = 1,
            lineBreakPrintln = 2,
            conditionalIndentation = 4,
        )
        `when`(formatterRulesRepository.findByUserId(userId)).thenReturn(Optional.of(existingRules))

        val result = formatterRulesService.getFormatterRulesByUserId(userId, correlationId)

        assertEquals(existingRules, result)
        verify(formatterRulesRepository, never()).save(any())
    }

    @Test
    fun `getFormatterRulesByUserId should create default rules if none exist`() {
        val userId = "user456"
        val correlationId = UUID.randomUUID()
        `when`(formatterRulesRepository.findByUserId(userId)).thenReturn(Optional.empty())

        val defaultRules = FormatterRules(
            userId = userId,
            spaceBeforeColon = false,
            spaceAfterColon = false,
            spaceAroundEquals = false,
            lineBreak = 1,
            lineBreakPrintln = 0,
            conditionalIndentation = 0,
        )
        `when`(formatterRulesRepository.save(any(FormatterRules::class.java))).thenReturn(defaultRules)

        val result = formatterRulesService.getFormatterRulesByUserId(userId, correlationId)

        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(defaultRules.spaceBeforeColon, result.spaceBeforeColon)
        verify(formatterRulesRepository).findByUserId(userId)
        verify(formatterRulesRepository).save(any(FormatterRules::class.java))
    }

    @Test
    fun `getFormatterRulesByUserId should throw DatabaseException when findByUserId fails`() {
        val userId = "user789"
        val correlationId = UUID.randomUUID()
        `when`(formatterRulesRepository.findByUserId(userId)).thenThrow(RuntimeException("DB error"))

        assertThrows(DatabaseException::class.java) {
            formatterRulesService.getFormatterRulesByUserId(userId, correlationId)
        }
        verify(formatterRulesRepository, never()).save(any())
    }

    @Test
    fun `updateFormatterRules should update and return updated rules`() {
        val userId = "userToUpdate"
        val existingRules = FormatterRules(
            userId = userId,
            spaceBeforeColon = false,
            spaceAfterColon = false,
            spaceAroundEquals = false,
            lineBreak = 0,
            lineBreakPrintln = 0,
            conditionalIndentation = 0,
        )
        `when`(formatterRulesRepository.findByUserId(userId)).thenReturn(Optional.of(existingRules))
        `when`(formatterRulesRepository.save(any(FormatterRules::class.java))).thenReturn(existingRules) // Mock save to return the same instance

        val updatedDto = FormatterRulesFileDTO(
            spaceBeforeColon = true,
            spaceAfterColon = true,
            spaceAroundEquals = true,
            lineBreak = 1,
            lineBreakPrintln = 2,
            conditionalIndentation = 4,
        )

        val result = formatterRulesService.updateFormatterRules(updatedDto, userId)

        assertEquals(updatedDto.spaceBeforeColon, result.spaceBeforeColon)
        assertEquals(updatedDto.spaceAfterColon, result.spaceAfterColon)
        assertEquals(updatedDto.spaceAroundEquals, result.spaceAroundEquals)
        assertEquals(updatedDto.lineBreak, result.lineBreak)
        assertEquals(updatedDto.lineBreakPrintln, result.lineBreakPrintln)
        assertEquals(updatedDto.conditionalIndentation, result.conditionalIndentation)

        verify(formatterRulesRepository).save(existingRules)
        assertEquals(true, existingRules.spaceBeforeColon) // Verify internal state of existingRules
        assertEquals(true, existingRules.spaceAfterColon)
        assertEquals(true, existingRules.spaceAroundEquals)
        assertEquals(1, existingRules.lineBreak)
        assertEquals(2, existingRules.lineBreakPrintln)
        assertEquals(4, existingRules.conditionalIndentation)
    }

    @Test
    fun `updateFormatterRules should throw DatabaseException when save fails`() {
        val userId = "userError"
        val existingRules = FormatterRules(userId = userId)
        `when`(formatterRulesRepository.findByUserId(userId)).thenReturn(Optional.of(existingRules))
        `when`(formatterRulesRepository.save(any(FormatterRules::class.java))).thenThrow(RuntimeException("DB save error"))

        val updatedDto = FormatterRulesFileDTO(
            spaceBeforeColon = true,
            spaceAfterColon = true,
            spaceAroundEquals = true,
            lineBreak = 1,
            lineBreakPrintln = 2,
            conditionalIndentation = 4,
        )

        assertThrows(DatabaseException::class.java) {
            formatterRulesService.updateFormatterRules(updatedDto, userId)
        }
        verify(formatterRulesRepository).save(any(FormatterRules::class.java))
    }

    @Test
    fun `createRulesForUser should throw DatabaseException when save fails`() {
        val userId = "userCreateError"
        `when`(formatterRulesRepository.findByUserId(userId)).thenReturn(Optional.empty()) // Ensure create is called
        `when`(formatterRulesRepository.save(any(FormatterRules::class.java))).thenThrow(RuntimeException("DB create error"))

        assertThrows(DatabaseException::class.java) {
            formatterRulesService.getFormatterRulesByUserId(userId, UUID.randomUUID())
        }
        verify(formatterRulesRepository).save(any(FormatterRules::class.java))
    }
}
