package com.ingsis_team.printscript_service_2025.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import com.ingsis_team.printscript_service_2025.interfaces.IRedisService
import com.ingsis_team.printscript_service_2025.redis.dto.Snippet
import java.io.ByteArrayInputStream

@Service("defaultRedisService")
class DefaultRedisService
    @Autowired
    constructor(
        private val snippetService: PrintScriptService,
    ) : IRedisService {
        private val logger = LoggerFactory.getLogger(DefaultRedisService::class.java)

        override fun formatSnippet(snippet: Snippet): Snippet {
            logger.info("Los datos del snippet son: $snippet")

            val formattedOutput =
                snippetService.format(
                    snippet.id,
                    ByteArrayInputStream(snippet.content.toByteArray()),
                    "1.1",
                    snippet.userId,
                    snippet.correlationID,
                )

            println("Estoy formateando un snippet")

            // Crea un nuevo objeto Snippet con el contenido formateado
            val outputSnippet =
                Snippet(
                    snippet.id,
                    formattedOutput.string,
                    snippet.userId,
                    snippet.correlationID,
                )
            println(formattedOutput.string)

            // Actualiza el bucket con el contenido formateado
            return outputSnippet
        }

        override fun lintSnippet(snippet: Snippet): Snippet {
            logger.info("Estoy linteando un snippet")

            val lintResults =
                snippetService.lint(
                    ByteArrayInputStream(snippet.content.toByteArray()),
                    "1.1",
                    snippet.userId,
                    snippet.correlationID,
                )

            // Extrae las reglas rotas como texto
            val brokenRules =
                lintResults.joinToString("\n") { scaOutput ->
                    "Rule: ${scaOutput.ruleBroken}, Line: ${scaOutput.lineNumber}, Description: ${scaOutput.description}"
                }

            // Crea un nuevo Snippet con las reglas rotas
            val outputSnippet = Snippet(snippet.id, brokenRules, snippet.userId, snippet.correlationID)
            return outputSnippet
        }
    }
