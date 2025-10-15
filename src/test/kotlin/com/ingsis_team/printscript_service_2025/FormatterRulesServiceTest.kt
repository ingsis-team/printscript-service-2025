package com.ingsis_team.printscript_service_2025
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import com.ingsis_team.printscript_service_2025.model.dto.FormatterRulesFileDTO
import com.ingsis_team.printscript_service_2025.model.repository.FormatterRulesRepository
import com.ingsis_team.printscript_service_2025.model.rules.FormatterRules
import com.ingsis_team.printscript_service_2025.service.FormatterRulesService
import java.util.*
import kotlin.test.assertEquals

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
            conditionalIndentation = 4
        )
        `when`(formatterRulesRepository.findByUserId(userId)).thenReturn(Optional.of(existingRules))

        val result = formatterRulesService.getFormatterRulesByUserId(userId, correlationId)

        assertEquals(existingRules, result)
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
            conditionalIndentation = 0
        )
        `when`(formatterRulesRepository.findByUserId(userId)).thenReturn(Optional.of(existingRules))

        val updatedDto = FormatterRulesFileDTO(
            userId = userId,
            spaceBeforeColon = true,
            spaceAfterColon = true,
            spaceAroundEquals = true,
            lineBreak = 1,
            lineBreakPrintln = 2,
            conditionalIndentation = 4
        )

        val result = formatterRulesService.updateFormatterRules(updatedDto, userId)

        assertEquals(updatedDto, result)
        verify(formatterRulesRepository).save(existingRules)
        assertEquals(true, existingRules.spaceBeforeColon)
        assertEquals(true, existingRules.spaceAfterColon)
        assertEquals(true, existingRules.spaceAroundEquals)
        assertEquals(1, existingRules.lineBreak)
        assertEquals(2, existingRules.lineBreakPrintln)
        assertEquals(4, existingRules.conditionalIndentation)
    }
}