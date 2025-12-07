package com.ingsisteam.printscriptservice2025.service

import com.ingsisteam.printscriptservice2025.exception.DatabaseException
import com.ingsisteam.printscriptservice2025.model.dto.LinterRulesFileDTO
import com.ingsisteam.printscriptservice2025.model.repository.LinterRulesRepository
import com.ingsisteam.printscriptservice2025.model.rules.LinterRules
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class LinterRulesService(
    @Autowired private var linterRulesRepository: LinterRulesRepository,
) {
    private val logger = LoggerFactory.getLogger(LinterRulesService::class.java)

    fun getLinterRulesByUserId(
        userId: String,
        correlationId: UUID,
    ): LinterRules {
        logger.info("=== LinterRulesService.getLinterRulesByUserId ===")
        logger.info("Parameters - userId: $userId, correlationId: $correlationId")
        val rules = findOrCreateByUser(userId)
        logger.info("Retrieved rules from database:")
        logger.info("  - userId: ${rules.userId}")
        logger.info("  - identifierFormat: '${rules.identifierFormat}'")
        logger.info("  - enablePrintOnly: ${rules.enablePrintOnly}")
        logger.info("  - enableInputOnly: ${rules.enableInputOnly}")
        logger.info("=== End getLinterRulesByUserId ===")
        return rules
    }

    fun updateLinterRules(
        linterRules: LinterRulesFileDTO,
        userId: String,
    ): LinterRulesFileDTO {
        try {
            logger.info("Updating linter rules for userId: $userId")
            var rules = findOrCreateByUser(userId)
            logger.debug("Current linter rules: identifierFormat=${rules.identifierFormat}, enablePrintOnly=${rules.enablePrintOnly}")
            rules.identifierFormat = linterRules.identifier_format
            rules.enableInputOnly = linterRules.enableInputOnly
            rules.enablePrintOnly = linterRules.enablePrintOnly

            val savedRules = linterRulesRepository.save(rules)
            logger.info("Linter rules updated successfully for userId: $userId")

            return savedRules.userId?.let {
                LinterRulesFileDTO(
                    it,
                    savedRules.identifierFormat,
                    savedRules.enableInputOnly,
                    savedRules.enablePrintOnly,
                )
            } ?: throw DatabaseException("Saved linter rules have null userId")
        } catch (e: DatabaseException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error updating linter rules for userId: $userId", e)
            throw DatabaseException("Could not update linter rules for user: $userId", e)
        }
    }

    private fun findOrCreateByUser(userId: String): LinterRules {
        try {
            logger.info("Searching for existing linter rules for userId: $userId")
            val rules = linterRulesRepository.findByUserId(userId).orElse(null)
            if (rules == null) {
                logger.info("No existing rules found for userId: $userId, creating default rules")
                return createUserById(userId)
            }
            logger.info("Found existing linter rules for userId: $userId")
            logger.info("Existing rules - identifierFormat: '${rules.identifierFormat}', enablePrintOnly: ${rules.enablePrintOnly}, enableInputOnly: ${rules.enableInputOnly}")
            return rules
        } catch (e: Exception) {
            logger.error("Error finding linter rules for userId: $userId", e)
            throw DatabaseException("Error accessing linter rules for user: $userId", e)
        }
    }

    private fun createUserById(userId: String): LinterRules {
        try {
            logger.info("Creating default linter rules for userId: $userId")
            val format =
                LinterRules(
                    userId = userId,
                    identifierFormat = "camelcase",
                    enablePrintOnly = false,
                    enableInputOnly = false,
                )
            return linterRulesRepository.save(format)
        } catch (e: Exception) {
            logger.error("Error creating linter rules for userId: $userId", e)
            throw DatabaseException("Could not create default linter rules for user: $userId", e)
        }
    }
}
