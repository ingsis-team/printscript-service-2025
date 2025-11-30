package com.ingsis_team.printscript_service_2025.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import com.ingsis_team.printscript_service_2025.model.dto.FormatterRulesFileDTO
import com.ingsis_team.printscript_service_2025.model.repository.FormatterRulesRepository
import com.ingsis_team.printscript_service_2025.model.rules.FormatterRules
import com.ingsis_team.printscript_service_2025.exception.DatabaseException
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
        logger.info("Getting formatter rules for userId: $userId, correlationId: $correlationId")
        val rules = findOrCreateRulesForUser(userId)
        logger.debug("Formatter rules retrieved for userId: $userId")
        return rules
    }

    fun updateFormatterRules(
        formatterRules: FormatterRulesFileDTO,
        userId: String,
    ): FormatterRulesFileDTO {
        try {
            logger.info("Updating formatter rules for userId: $userId")
            val rules = findOrCreateRulesForUser(userId)
            logger.debug("Current formatter rules: ${rules.toString()}")

            rules.spaceBeforeColon = formatterRules.spaceBeforeColon
            rules.spaceAfterColon = formatterRules.spaceAfterColon
            rules.spaceAroundEquals = formatterRules.spaceAroundEquals
            rules.lineBreak = formatterRules.lineBreak
            rules.lineBreakPrintln = formatterRules.lineBreakPrintln
            rules.conditionalIndentation = formatterRules.conditionalIndentation

            formatterRulesRepository.save(rules)
            logger.info("Formatter rules updated successfully for userId: $userId")

            return FormatterRulesFileDTO(
                rules.spaceBeforeColon,
                rules.spaceAfterColon,
                rules.spaceAroundEquals,
                rules.lineBreak,
                rules.lineBreakPrintln,
                rules.conditionalIndentation,
            )
        } catch (e: Exception) {
            logger.error("Failed to save formatter rules for userId: $userId", e)
            throw DatabaseException("Could not save formatter rules for user: $userId", e)
        }
    }

    private fun findOrCreateRulesForUser(userId: String): FormatterRules {
        try {
            val rules = formatterRulesRepository.findByUserId(userId).orElse(null)
            if (rules == null) {
                logger.info("Formatter rules not found for userId: $userId, creating default rules")
                return createRulesForUser(userId)
            }
            logger.debug("Found existing formatter rules for userId: $userId")
            return rules
        } catch (e: Exception) {
            logger.error("Error finding formatter rules for userId: $userId", e)
            throw DatabaseException("Error accessing formatter rules for user: $userId", e)
        }
    }

    private fun createRulesForUser(userId: String): FormatterRules {
        try {
            logger.info("Creating default formatter rules for userId: $userId")
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
        } catch (e: Exception) {
            logger.error("Error creating formatter rules for userId: $userId", e)
            throw DatabaseException("Could not create default formatter rules for user: $userId", e)
        }
    }
}
