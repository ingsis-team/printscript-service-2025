package com.ingsis_team.printscript_service_2025.service

import ast.ASTNode
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import formatOperations.AssignationFormatter
import formatOperations.BinaryFormatter
import formatOperations.BlockFormatter
import formatOperations.ConditionalFormatter
import formatOperations.DeclarationFormatter
import formatOperations.FormattingOperation
import formatOperations.LiteralFormatter
import formatOperations.PrintFormatter
import formatter.FormatterPS
import lexer.Lexer
import lexer.TokenMapper
import linter.Linter
import linter.LinterVersion
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import parser.Parser
import com.ingsis_team.printscript_service_2025.interfaces.LanguageService
import com.ingsis_team.printscript_service_2025.model.Output
import com.ingsis_team.printscript_service_2025.model.SCAOutput
import com.ingsis_team.printscript_service_2025.model.dto.FormatterFileDTO
import com.ingsis_team.printscript_service_2025.model.dto.LinterFileDTO
import com.ingsis_team.printscript_service_2025.model.dto.ValidationResult
import com.ingsis_team.printscript_service_2025.exception.*
import reactor.core.publisher.Mono
import rules.RulesReader
import token.Token
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.*

@Service
class PrintScriptService
    @Autowired
    constructor(
        private val tokenMapper: TokenMapper,
        private val parser: Parser,
        private val linter: Linter,
        private val formatter: FormatterPS,
        private val formatterService: FormatterRulesService,
        private val linterRulesService: LinterRulesService,
        @Value("\${asset.url}") private val assetUrl: String,
    ) : LanguageService {
        private val logger = LoggerFactory.getLogger(PrintScriptService::class.java)

        companion object {
            fun objectMapper(): ObjectMapper {
                val mapper = ObjectMapper()
                // Serializar por defecto en snake_case para compatibilidad con front/cliente
                mapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
                // Aceptar propiedades independientemente de mayúsculas/minúsculas
                mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                // Ignorar campos desconocidos para ser tolerante con cambios en JSON
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                return mapper
            }
        }

        val assetServiceApi = WebClient.builder().baseUrl("http://$assetUrl/v1/asset").build()

        override fun runScript(
            input: InputStream,
            version: String,
        ): Output {
            try {
                logger.info("Running script with version: $version")
                val lexer = Lexer(tokenMapper)
                val tokens = lexer.execute(input.bufferedReader().readText())
                val script = parser.execute(tokens)
                logger.debug("Script execution completed")
                return Output(script.toString())
            } catch (e: IllegalArgumentException) {
                logger.error("Invalid version or syntax in script execution: ${e.message}", e)
                throw ValidationException("Invalid version or syntax: ${e.message}", e)
            } catch (e: Exception) {
                logger.error("Error executing script: ${e.message}", e)
                throw ScriptExecutionException("Failed to execute script: ${e.message}", e)
            }
        }

        override fun test(
            input: String,
            output: List<String>,
            snippet: String,
            envVars: String,
        ): String {
            try {
                logger.info("Starting test execution")
                logger.debug("Expected outputs: ${output.size}")
                val inputStream = ByteArrayInputStream(snippet.toByteArray())
                val executionOutput = runScript(inputStream, "1.1")
                val executionResult = executionOutput.string.split("\n")
                for (i in output.indices) {
                    if (executionResult[i] != output[i]) {
                        logger.warn("Test failed at line $i. Expected: ${output[i]}, Got: ${executionResult[i]}")
                        return "failure"
                    }
                }
                logger.info("Test execution successful")
                return "success"
            } catch (e: ScriptExecutionException) {
                throw e
            } catch (e: Exception) {
                logger.error("Error during test execution: ${e.message}", e)
                throw ScriptExecutionException("Test execution failed: ${e.message}", e)
            }
        }

        override fun validate(
            input: String,
            version: String,
        ): ValidationResult {
            try {
                logger.info("Validating code with version: $version")
                val lexer = Lexer(tokenMapper)
                val tokens = lexer.execute(input)
                val script = parser.execute(tokens)
                val linterVersion =
                    LinterVersion.fromString(version)
                        ?: return ValidationResult(
                            isValid = false,
                            rule = "Unsupported version: $version",
                            line = 0,
                            column = 0
                        )
                val linter = Linter(linterVersion)

                val results = linter.check(script)

                val scaOutputs: MutableList<SCAOutput> =
                    results.getBrokenRules().map { brokenRule ->
                        SCAOutput(
                            lineNumber = brokenRule.errorPosition.row,
                            ruleBroken = brokenRule.ruleDescription,
                            description = "Broken rule at line ${brokenRule.errorPosition.row}, column ${brokenRule.errorPosition.column}",
                        )
                    }.toMutableList()

                if (scaOutputs.isEmpty()) {
                    logger.info("Validation successful - no broken rules found")
                    return ValidationResult(isValid = true, rule = "", line = 0, column = 0)
                } else {
                    val firstBrokenRule = scaOutputs.first()
                    logger.warn("Validation failed - found ${scaOutputs.size} broken rules. First: ${firstBrokenRule.ruleBroken} at line ${firstBrokenRule.lineNumber}")

                    // Intentar extraer el número de columna del mensaje de descripción
                    val columnNumber = try {
                        firstBrokenRule.description.split(", column ").getOrNull(1)?.toIntOrNull() ?: 0
                    } catch (e: Exception) {
                        0
                    }

                    return ValidationResult(
                        isValid = false,
                        rule = firstBrokenRule.ruleBroken,
                        line = firstBrokenRule.lineNumber,
                        column = columnNumber,
                    )
                }
            } catch (e: Exception) {
                // En lugar de lanzar una excepción, devolver un ValidationResult con el error de sintaxis
                logger.warn("Syntax or parsing error during validation: ${e.message}")
                return ValidationResult(
                    isValid = false,
                    rule = e.message ?: "Syntax error",
                    line = 0,
                    column = 0
                )
            }
        }

        override fun lint(
            input: InputStream,
            version: String,
            userId: String,
            correlationId: UUID,
        ): MutableList<SCAOutput> {
            logger.info("Starting linting for userId: $userId, correlationId: $correlationId")
            val sanitizedUserId = sanitizeUserId(userId)
            val defaultPath = "./$sanitizedUserId-linterRules.json"

            try {
                val lintRules = linterRulesService.getLinterRulesByUserId(userId, correlationId)
                logger.debug("Loaded linter rules for user: $userId")
                val linterDto =
                    LinterFileDTO(
                        lintRules.identifierFormat,
                        lintRules.enablePrintOnly,
                        lintRules.enableInputOnly,
                    )

                val rulesFile = File(defaultPath)
                objectMapper().writeValue(rulesFile, linterDto)

                val code = input.bufferedReader().use { it.readText() }

                val tokenMapper = TokenMapper(version)
                val lexer = Lexer(tokenMapper)
                val tokens: List<Token> = lexer.execute(code)

                val parser = Parser()
                val trees: List<ASTNode> = parser.execute(tokens)

                // Instantiate the linter and apply the rules
                val linterVersion =
                    LinterVersion.fromString(version)
                        ?: throw ValidationException("Unsupported linter version: $version")
                val linter = Linter(linterVersion)
                val results = linter.check(trees)

                // Convertir los BrokenRules a SCAOutput
                val scaOutputs: MutableList<SCAOutput> =
                    results.getBrokenRules().map { brokenRule ->
                        SCAOutput(
                            lineNumber = brokenRule.errorPosition.row,
                            ruleBroken = brokenRule.ruleDescription,
                            description = "Broken rule at line ${brokenRule.errorPosition.row}, column ${brokenRule.errorPosition.column}",
                        )
                    }.toMutableList()

                logger.info("Linting completed. Found ${scaOutputs.size} issues")
                return scaOutputs
            } catch (e: ValidationException) {
                throw e
            } catch (e: DatabaseException) {
                throw e
            } catch (e: Exception) {
                logger.error("Error during linting: ${e.message}", e)
                throw LintingException("Failed to lint code: ${e.message}", e)
            } finally {
                // Delete the rules file
                val rulesFile = File(defaultPath)
                if (rulesFile.exists()) {
                    rulesFile.delete()
                    logger.debug("Temporary linter rules file deleted: $defaultPath")
                }
            }
        }

        override fun format(
            snippetId: String,
            input: InputStream,
            version: String,
            userId: String,
            correlationId: UUID,
        ): Output {
            logger.info("Starting formatting for snippetId: $snippetId, userId: $userId, correlationId: $correlationId")
            val sanitizedUserId = sanitizeUserId(userId)

            val defaultPath = "./$sanitizedUserId-formatterRules.json"
            try {
                // Get format rules from the service
                val formatterRules = formatterService.getFormatterRulesByUserId(userId, correlationId)
                logger.debug("Loaded formatter rules for user: $userId")
                val formatterDto =
                    FormatterFileDTO(
                        formatterRules.spaceBeforeColon,
                        formatterRules.spaceAfterColon,
                        formatterRules.spaceAroundEquals,
                        formatterRules.lineBreak,
                        formatterRules.lineBreakPrintln,
                        formatterRules.conditionalIndentation,
                    )

                // Escribir las reglas en un archivo JSON temporal
                val rulesFile = File(defaultPath)
                // Usar el ObjectMapper configurado en companion object para respetar snake_case y opciones
                objectMapper().writeValue(rulesFile, formatterDto)

                // Configurar las reglas, lexer, parser y operaciones
                val rulesReader =
                    RulesReader(
                        mapOf(
                            "spaceBeforeColon" to Boolean::class,
                            "spaceAfterColon" to Boolean::class,
                            "spaceAroundEquals" to Boolean::class,
                            "lineBreak" to Int::class,
                            "lineBreakPrintln" to Int::class,
                            "conditionalIndentation" to Int::class,
                        ),
                    )
                val lexer = Lexer(TokenMapper(version))
                val parser = Parser()

                // Declarar las funciones para palabras clave y tipos de datos
                val getAllowedDeclarationKeywords = { Declversion: String ->
                    when (Declversion) {
                        "1.0" -> listOf("let")
                        "1.1" -> listOf("let", "const")
                        else -> throw ValidationException("Unsupported version: $Declversion")
                    }
                }
                val getAllowedDataTypes = { Allowedversion: String ->
                    when (Allowedversion) {
                        "1.0" -> listOf("number", "string")
                        "1.1" -> listOf("number", "string", "boolean")
                        else -> throw ValidationException("Unsupported version: $Allowedversion")
                    }
                }

                // Configurar las operaciones de formato
                var formattingOperations: List<FormattingOperation> =
                    listOf(
                        AssignationFormatter(),
                        BinaryFormatter(),
                        BlockFormatter(),
                        LiteralFormatter(),
                        PrintFormatter(),
                        DeclarationFormatter(getAllowedDeclarationKeywords(version), getAllowedDataTypes(version)),
                    )
                if (version == "1.1") {
                    formattingOperations += ConditionalFormatter()
                }

                // Create FormatterPS instance
                val formatter = FormatterPS(rulesReader, defaultPath, formattingOperations, lexer, parser)

                // Read the code from the InputStream
                val code = input.bufferedReader().use { it.readText() }

                // Execute the formatter and get the formatted code
                val formattedCode = formatter.format(code)

                // Update the formatted content in the bucket (optional - does not fail if service is not available)
                try {
                    updateOnBucket(snippetId, formattedCode)
                } catch (e: Exception) {
                    // Log the error but don't fail the formatting
                    logger.warn("Could not update bucket, but formatting was successful: ${e.message}")
                }

                // Return the formatted result
                return Output(formattedCode)
            } catch (e: ValidationException) {
                throw e
            } catch (e: ExternalServiceException) {
                throw e
            } catch (e: Exception) {
                logger.error("Error during formatting: ${e.message}", e)
                throw FormattingException("Failed to format code: ${e.message}", e)
            } finally {
                // Clean up the temporary file
                val rulesFile = File(defaultPath)
                if (rulesFile.exists()) {
                    rulesFile.delete()
                    logger.debug("Temporary formatter rules file deleted: $defaultPath")
                }
            }
        }

        fun sanitizeUserId(userId: String): String {
            return userId.replace(Regex("[^a-zA-Z0-9.-]"), "_")
        }

        fun updateOnBucket(
            key: String,
            content: String,
        ) {
            logger.info("Updating bucket for snippetId: $key")
            try {
                // Delete the existing content in the bucket
                val deleteResponseStatus =
                    assetServiceApi
                        .delete()
                        .uri("/snippets/{key}", key)
                        .exchangeToMono { clientResponse -> Mono.just(clientResponse.statusCode()) }
                        .block()

                // Validate if deletion was successful
                // Allow 404 (NOT_FOUND) since the snippet may not exist yet
                if (deleteResponseStatus != HttpStatus.NO_CONTENT && deleteResponseStatus != HttpStatus.NOT_FOUND) {
                    logger.error("Failed to delete snippet from bucket. Status: $deleteResponseStatus")
                    throw RuntimeException("Error al eliminar el snippet: Código de respuesta $deleteResponseStatus")
                }
                logger.debug("Snippet deleted from bucket (or did not exist). Status: $deleteResponseStatus")

                // Upload the new content to the bucket
                val putResponseStatus =
                    assetServiceApi
                        .put()
                        .uri("/snippets/{key}", key)
                        .bodyValue(content)
                        .exchangeToMono { clientResponse -> Mono.just(clientResponse.statusCode()) }
                        .block()

                // Validate if upload was successful
                // PUT returns 200 (OK) for updates or 201 (CREATED) for new assets
                if (putResponseStatus != HttpStatus.OK && putResponseStatus != HttpStatus.CREATED) {
                    logger.error("Failed to update snippet in bucket. Status: $putResponseStatus")
                    throw RuntimeException("Error al actualizar el snippet: Código de respuesta $putResponseStatus")
                }
                logger.info("Snippet successfully updated in bucket. Status: $putResponseStatus")
            } catch (e: Exception) {
                logger.error("Error updating bucket for snippetId $key: ${e.message}", e)
                throw ExternalServiceException("Error communicating with asset service: ${e.message}", e)
            }
        }
    }
