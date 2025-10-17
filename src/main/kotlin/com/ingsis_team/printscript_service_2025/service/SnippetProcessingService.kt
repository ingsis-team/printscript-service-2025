package com.ingsis_team.printscript_service_2025.service

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import com.ingsis_team.printscript_service_2025.interfaces.LanguageService

@Service
class SnippetProcessingService(private val applicationContext: ApplicationContext) {

    fun selectService(language: String): LanguageService {
        return when (language) {
            "printscript" -> applicationContext.getBean(PrintScriptService::class.java)
            else -> throw IllegalArgumentException("Unsupported language $language")
        }
    }
}
