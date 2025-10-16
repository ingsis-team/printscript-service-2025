package com.ingsis_team.printscript_service_2025.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import com.ingsis_team.printscript_service_2025.model.dto.FormatterRulesFileDTO
import com.ingsis_team.printscript_service_2025.model.repository.FormatterRulesRepository
import com.ingsis_team.printscript_service_2025.model.rules.FormatterRules
import java.util.*

@Service
class FormatterRulesService(
    @Autowired private var formatterRulesRepository: FormatterRulesRepository,
) {
    private val logger = LoggerFactory.getLogger(FormatterRulesService::class.java)

    fun getFormatterRulesByUserId(
        userId: String,
        correlationId: UUID,
    ): FormatterRules {
        logger.info("userId: $userId")
        return findOrCreateByUser(userId)
    }

    fun updateFormatterRules(
        formatterRules: FormatterRulesFileDTO,
        userId: String,
    ): FormatterRulesFileDTO {
        try {
            logger.info("userId: $userId")
            val rules = findOrCreateByUser(userId)
            logger.info("old rules: $rules")
            rules.spaceBeforeColon = formatterRules.spaceBeforeColon
            rules.spaceAfterColon = formatterRules.spaceAfterColon
            rules.spaceAroundEquals = formatterRules.spaceAroundEquals
            rules.lineBreak = formatterRules.lineBreak
            rules.lineBreakPrintln = formatterRules.lineBreakPrintln
            rules.conditionalIndentation = formatterRules.conditionalIndentation

            formatterRulesRepository.save(rules)
            logger.info("$rules")
            return FormatterRulesFileDTO(
                userId,
                rules.spaceBeforeColon,
                rules.spaceAfterColon,
                rules.spaceAroundEquals,
                rules.lineBreak,
                rules.lineBreakPrintln,
                rules.conditionalIndentation,
            )
        } catch (e: Exception) {
            throw RuntimeException("Could not save rules")
        }
    }

    private fun findOrCreateByUser(userId: String): FormatterRules {
        val rules = formatterRulesRepository.findByUserId(userId).orElse(null)
        logger.info("rules: $rules")
        if (rules == null) {
            logger.info("User not found")
            return createUserById(userId)
        }
        return rules
    }

    private fun createUserById(userId: String): FormatterRules {
        val format =
            FormatterRules(
                userId = userId,
                spaceBeforeColon = false,
                spaceAfterColon = false,
                spaceAroundEquals = false,
                lineBreak = 0,
                lineBreakPrintln = 0,
                conditionalIndentation = 0,
            )
        return formatterRulesRepository.save(format)
    }
}
