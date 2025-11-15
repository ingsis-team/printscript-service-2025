package com.ingsis_team.printscript_service_2025.service

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import com.ingsis_team.printscript_service_2025.interfaces.LanguageService
import com.ingsis_team.printscript_service_2025.exception.UnsupportedLanguageException

@Service
class SnippetProcessingService(private val applicationContext: ApplicationContext) {
    private val logger = LoggerFactory.getLogger(SnippetProcessingService::class.java)

    fun selectService(language: String): LanguageService {
        logger.debug("Selecting service for language: $language")
        return when (language) {
            "printscript" -> applicationContext.getBean(PrintScriptService::class.java)
            else -> {
                logger.error("Unsupported language requested: $language")
                throw UnsupportedLanguageException("Unsupported language: $language. Only 'printscript' is supported.")
            }
        }
    }
}
