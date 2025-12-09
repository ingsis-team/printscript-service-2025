package com.ingsisteam.printscriptservice2025.service

import ast.ASTNode
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.ingsisteam.printscriptservice2025.exception.DatabaseException
import com.ingsisteam.printscriptservice2025.exception.FormattingException
import com.ingsisteam.printscriptservice2025.exception.LintingException
import com.ingsisteam.printscriptservice2025.exception.ScriptExecutionException
import com.ingsisteam.printscriptservice2025.exception.ValidationException
import com.ingsisteam.printscriptservice2025.interfaces.LanguageService
import com.ingsisteam.printscriptservice2025.model.Output
import com.ingsisteam.printscriptservice2025.model.SCAOutput
import com.ingsisteam.printscriptservice2025.model.dto.FormatterFileDTO
import com.ingsisteam.printscriptservice2025.model.dto.LinterFileDTO
import com.ingsisteam.printscriptservice2025.model.dto.ValidationResult
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
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import parser.Parser
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

        // ObjectMapper para archivos de reglas del paquete printscript
        // Usa camelCase por defecto, pero mantiene identifier_format tal como está (con guión bajo)
        fun rulesObjectMapper(): ObjectMapper {
            val mapper = ObjectMapper()
            // NO usar ninguna estrategia de naming - las propiedades se serializarán tal como están definidas
            // Esto significa: camelCase para todo EXCEPTO identifier_format que ya tiene guión bajo
            // Aceptar propiedades independientemente de mayúsculas/minúsculas
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            // Ignorar campos desconocidos para ser tolerante con cambios en JSON
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            return mapper
        }

        // Sanitize userId for use in filenames by replacing invalid characters
        fun sanitizeUserId(userId: String): String {
            return userId.replace(Regex("[^a-zA-Z0-9._-]"), "_")
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
            logger.debug("Input content: '$input'")
            val lexer = Lexer(tokenMapper)
            val tokens = lexer.execute(input)
            val tokenTypes = tokens.take(10).map { token -> token.getType().name }
            logger.debug("Generated ${tokens.size} tokens: $tokenTypes")
            val script = parser.execute(tokens)
            val linterVersion =
                LinterVersion.fromString(version)
                    ?: return ValidationResult(
                        isValid = false,
                        rule = "Unsupported version: $version",
                        line = 0,
                        column = 0,
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
                column = 0,
            )
        }
    }

    override fun lint(
        input: InputStream,
        version: String,
        userId: String,
        correlationId: UUID,
    ): MutableList<SCAOutput> {
        logger.info("=== STARTING LINTING PROCESS ===")
        logger.info("Parameters - userId: $userId, correlationId: $correlationId, version: $version")
        val sanitizedUserId = sanitizeUserId(userId)
        val defaultPath = "./$sanitizedUserId-linterRules.json"
        logger.info("Sanitized userId: $sanitizedUserId, Rules file path: $defaultPath")

        try {
            logger.info("Step 1: Fetching linter rules from database for userId: $userId")
            val lintRules = linterRulesService.getLinterRulesByUserId(userId, correlationId)
            logger.info("Step 1 COMPLETE: Retrieved rules from DB - identifierFormat: '${lintRules.identifierFormat}', enablePrintOnly: ${lintRules.enablePrintOnly}, enableInputOnly: ${lintRules.enableInputOnly}")

            val linterDto =
                LinterFileDTO(
                    lintRules.identifierFormat,
                    lintRules.enablePrintOnly,
                    lintRules.enableInputOnly,
                )
            logger.info("Step 2: Created LinterFileDTO - identifier_format: '${linterDto.identifier_format}', enablePrintOnly: ${linterDto.enablePrintOnly}, enableInputOnly: ${linterDto.enableInputOnly}")

            val rulesFile = File(defaultPath)
            logger.info("Step 3: Writing rules to file: $defaultPath")
            // Usar rulesObjectMapper() que mantiene las propiedades correctas
            rulesObjectMapper().writeValue(rulesFile, linterDto)
            logger.info("Step 3 COMPLETE: Rules file written successfully")

            // Log para debug: mostrar contenido del archivo
            val rulesFileContent = rulesFile.readText()
            logger.info("Step 4: Rules file content (JSON): $rulesFileContent")
            logger.info("Step 4: Rules file exists: ${rulesFile.exists()}, Size: ${rulesFile.length()} bytes")

            logger.info("Step 5: Reading code from input stream")
            val code = input.bufferedReader().use { it.readText() }
            logger.info("Step 5 COMPLETE: Code read successfully, length: ${code.length} characters")
            logger.debug("Step 5: Code preview (first 200 chars): ${code.take(200)}")

            logger.info("Step 6: Tokenizing code with version: $version")
            val tokenMapper = TokenMapper(version)
            val lexer = Lexer(tokenMapper)
            val tokens: List<Token> = lexer.execute(code)
            logger.info("Step 6 COMPLETE: Generated ${tokens.size} tokens")

            logger.info("Step 7: Parsing tokens into AST")
            val parser = Parser()
            val trees: List<ASTNode> = parser.execute(tokens)
            logger.info("Step 7 COMPLETE: Generated ${trees.size} AST nodes")

            // Instantiate the linter and apply the rules
            logger.info("Step 8: Creating linter instance with version: $version")
            val linterVersion =
                LinterVersion.fromString(version)
                    ?: throw ValidationException("Unsupported linter version: $version")
            val linter = Linter(linterVersion)
            logger.info("Step 8 COMPLETE: Linter instance created")

            logger.info("Step 9: Loading rules into linter from JSON content")
            logger.info("Step 9: About to call linter.readJson() with content length: ${rulesFileContent.length}")
            linter.readJson(rulesFileContent)
            logger.info("Step 9 COMPLETE: Rules loaded into linter")

            val loadedRules = linter.getRules()
            logger.info("Step 9: Linter now has ${loadedRules.size} rules loaded")
            loadedRules.forEachIndexed { index, rule ->
                logger.info("Step 9: Rule $index - name: '${rule.getRuleName()}', description: '${rule.getRuleDescription()}'")
            }

            logger.info("Step 10: Running linter.check() on ${trees.size} AST nodes")
            logger.info("Step 10: Rules being applied - identifier_format: '${linterDto.identifier_format}', enablePrintOnly: ${linterDto.enablePrintOnly}, enableInputOnly: ${linterDto.enableInputOnly}")
            val results = linter.check(trees)
            logger.info("Step 10 COMPLETE: Linter check completed")

            // Convertir los BrokenRules a SCAOutput
            val brokenRules = results.getBrokenRules()
            logger.info("Step 11: Converting ${brokenRules.size} broken rules to SCAOutput")
            brokenRules.forEachIndexed { index, brokenRule ->
                logger.info("Step 11: Broken rule $index - rule: '${brokenRule.ruleDescription}', line: ${brokenRule.errorPosition.row}, column: ${brokenRule.errorPosition.column}")
            }

            val scaOutputs: MutableList<SCAOutput> =
                brokenRules.map { brokenRule ->
                    SCAOutput(
                        lineNumber = brokenRule.errorPosition.row,
                        ruleBroken = brokenRule.ruleDescription,
                        description = "Broken rule at line ${brokenRule.errorPosition.row}, column ${brokenRule.errorPosition.column}",
                    )
                }.toMutableList()

            logger.info("=== LINTING PROCESS COMPLETE ===")
            logger.info("Total issues found: ${scaOutputs.size}")
            scaOutputs.forEachIndexed { index, output ->
                logger.info("Issue $index: rule='${output.ruleBroken}', line=${output.lineNumber}, description='${output.description}'")
            }
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
            // Usar rulesObjectMapper() que mantiene camelCase para compatibilidad con RulesReader
            rulesObjectMapper().writeValue(rulesFile, formatterDto)

            // Log para debug: mostrar contenido del archivo
            logger.debug("Formatter rules file created at: $defaultPath with content: ${rulesFile.readText()}")

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

            // Return the formatted result
            return Output(formattedCode)
        } catch (e: ValidationException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error during formatting: ${e.message}", e)
            throw FormattingException("Failed to format code: ${e.message}", e)
        } finally {
            // Delete the rules file
            val rulesFile = File(defaultPath)
            if (rulesFile.exists()) {
                rulesFile.delete()
                logger.debug("Temporary formatter rules file deleted: $defaultPath")
            }
        }
    }
}
