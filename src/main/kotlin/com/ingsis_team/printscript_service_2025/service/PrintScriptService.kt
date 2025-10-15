package com.ingsis_team.printscript_service_2025.service

import ast.ASTNode
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
                mapper.propertyNamingStrategy = PropertyNamingStrategies.UPPER_CAMEL_CASE
                return mapper
            }
        }

        val assetServiceApi = WebClient.builder().baseUrl("http://$assetUrl/v1/asset").build()

        override fun runScript(
            input: InputStream,
            version: String,
        ): Output {
            val lexer = Lexer(tokenMapper)
            val tokens = lexer.execute(input.bufferedReader().readText())
            val script = parser.execute(tokens)
            return Output(script.toString())
        }

        override fun test(
            input: String,
            output: List<String>,
            snippet: String,
            envVars: String,
        ): String {
            val inputStream = ByteArrayInputStream(snippet.toByteArray())
            val executionOutput = runScript(inputStream, "1.1")
            val executionResult = executionOutput.string.split("\n")
            for (i in output.indices) {
                if (executionResult[i] != output[i]) {
                    return "failure"
                }
            }
            return "success"
        }

        override fun validate(
            input: String,
            version: String,
        ): ValidationResult {
            val lexer = Lexer(tokenMapper)
            val tokens = lexer.execute(input)
            val script = parser.execute(tokens)
            val linterVersion =
                LinterVersion.fromString(version)
                    ?: throw IllegalArgumentException("Versión de linter no soportada: $version")
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

            return if (scaOutputs.isEmpty()) {
                ValidationResult(isValid = true, rule = "", line = 0, column = 0)
            } else {
                val firstBrokenRule = scaOutputs.first()
                ValidationResult(
                    isValid = false,
                    rule = firstBrokenRule.ruleBroken,
                    line = firstBrokenRule.lineNumber,
                    column = firstBrokenRule.description.split(", column ")[1].toInt(),
                )
            }
        }

        override fun lint(
            input: InputStream,
            version: String,
            userId: String,
            correlationId: UUID,
        ): MutableList<SCAOutput> {
            val sanitizedUserId = sanitizeUserId(userId)
            val defaultPath = "./$sanitizedUserId-linterRules.json"

            try {
                val lintRules = linterRulesService.getLinterRulesByUserId(userId, correlationId)
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

                // Instanciar el linter y aplicar las reglas
                val linterVersion =
                    LinterVersion.fromString(version)
                        ?: throw IllegalArgumentException("Versión de linter no soportada: $version")
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

                return scaOutputs
            } finally {
                // Eliminar el archivo de reglas
                val rulesFile = File(defaultPath)
                if (rulesFile.exists()) {
                    rulesFile.delete()
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
            val sanitizedUserId = sanitizeUserId(userId)

            val defaultPath = "./$sanitizedUserId-formatterRules.json"
            try {
                // Obtener reglas de formato del servicio
                val formatterRules = formatterService.getFormatterRulesByUserId(userId, correlationId)
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
                ObjectMapper().writeValue(rulesFile, formatterDto)

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
                        else -> throw IllegalArgumentException("Versión no soportada: $Declversion")
                    }
                }
                val getAllowedDataTypes = { Allowedversion: String ->
                    when (Allowedversion) {
                        "1.0" -> listOf("number", "string")
                        "1.1" -> listOf("number", "string", "boolean")
                        else -> throw IllegalArgumentException("Versión no soportada: $Allowedversion")
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

                // Crear instancia de FormatterPS
                val formatter = FormatterPS(rulesReader, defaultPath, formattingOperations, lexer, parser)

                // Leer el código desde el InputStream
                val code = input.bufferedReader().use { it.readText() }

                // Ejecutar el formateador y obtener el código formateado
                val formattedCode = formatter.format(code)

                // Limpiar el archivo temporal
                if (rulesFile.exists()) {
                    rulesFile.delete()
                }

                // Actualizar el contenido formateado en el bucket
                updateOnBucket(snippetId, formattedCode)

                // Retornar el resultado formateado
                return Output(formattedCode)
            } catch (e: Exception) {
                throw RuntimeException("Error al formatear el código: ${e.message}", e)
            }
        }

        fun sanitizeUserId(userId: String): String {
            return userId.replace(Regex("[^a-zA-Z0-9.-]"), "_")
        }

        fun updateOnBucket(
            key: String,
            content: String,
        ) {
            try {
                // Eliminar el contenido existente en el bucket
                val deleteResponseStatus =
                    assetServiceApi
                        .delete()
                        .uri("/snippets/{key}", key)
                        .exchangeToMono { clientResponse -> Mono.just(clientResponse.statusCode()) } // Wrap status code in Mono
                        .block()

                // Validar si la eliminación fue exitosa
                if (deleteResponseStatus != HttpStatus.NO_CONTENT) {
                    throw RuntimeException("Error al eliminar el snippet: Código de respuesta $deleteResponseStatus")
                }

                // Subir el nuevo contenido al bucket
                val postResponseStatus =
                    assetServiceApi
                        .post()
                        .uri("/snippets/{key}", key)
                        .bodyValue(content)
                        .exchangeToMono { clientResponse -> Mono.just(clientResponse.statusCode()) } // Wrap status code in Mono
                        .block()

                // Validar si la subida fue exitosa
                if (postResponseStatus != HttpStatus.CREATED) {
                    throw RuntimeException("Error al subir el snippet: Código de respuesta $postResponseStatus")
                }

                // Imprimir el estado de la respuesta
                println("Snippet actualizado exitosamente con código de respuesta: $postResponseStatus")
            } catch (e: Exception) {
                throw RuntimeException("Error en la operación de actualización del bucket: ${e.message}", e)
            }
        }
    }
