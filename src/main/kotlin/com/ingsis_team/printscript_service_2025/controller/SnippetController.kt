package com.ingsis_team.printscript_service_2025.controller

import com.ingsis_team.printscript_service_2025.model.Output
import com.ingsis_team.printscript_service_2025.model.SCAOutput
import com.ingsis_team.printscript_service_2025.model.dto.SnippetDTO
import com.ingsis_team.printscript_service_2025.model.dto.SnippetOutputDTO
import com.ingsis_team.printscript_service_2025.model.dto.TestDTO
import com.ingsis_team.printscript_service_2025.model.dto.ValidationResult
import com.ingsis_team.printscript_service_2025.redis.dto.Rule
import com.ingsis_team.printscript_service_2025.service.FormatterRulesService
import com.ingsis_team.printscript_service_2025.service.LinterRulesService
import com.ingsis_team.printscript_service_2025.service.SnippetProcessingService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayInputStream
import java.util.UUID
import com.ingsis_team.printscript_service_2025.model.dto.FormatterRulesFileDTO
import com.ingsis_team.printscript_service_2025.model.dto.LinterRulesFileDTO
import com.ingsis_team.printscript_service_2025.exception.ValidationException
import com.ingsis_team.printscript_service_2025.interfaces.IRedisService
import com.ingsis_team.printscript_service_2025.redis.dto.Snippet


@RestController
class SnippetController(
    private val snippetProcessingService: SnippetProcessingService,
    val linterRulesService: LinterRulesService,
    val formaterRulesService: FormatterRulesService,
    // Inyectar implementación de Redis para procesar eventos asíncronos
    private val redisService: IRedisService,
) {
    private val logger = LoggerFactory.getLogger(SnippetController::class.java)

    @PostMapping("/validate")
    fun validateSnippet(
        @RequestBody validate: String,
    ): ValidationResult {
        logger.info("Received validation request")
        logger.debug("Validating snippet content")
        val languageService = snippetProcessingService.selectService("printscript")
        val result = languageService.validate(validate, "1.1")
        logger.info("Validation completed. IsValid: ${result.isValid}")
        return result
    }

    @PostMapping("/run")
    fun runSnippet(
        @RequestBody snippetRunnerDTO: SnippetDTO,
    ): ResponseEntity<SnippetOutputDTO> {
        logger.info("Received run snippet request. SnippetId: ${snippetRunnerDTO.snippetId}, CorrelationId: ${snippetRunnerDTO.correlationId}")
        logger.debug("Language: ${snippetRunnerDTO.language}, Version: ${snippetRunnerDTO.version}")
        val languageService = snippetProcessingService.selectService(snippetRunnerDTO.language)
        val inputStream = ByteArrayInputStream(snippetRunnerDTO.input.toByteArray())
        val output: Output = languageService.runScript(inputStream, snippetRunnerDTO.version)
        val snippetOutput = SnippetOutputDTO(output.string, snippetRunnerDTO.correlationId, snippetRunnerDTO.snippetId)
        logger.info("Snippet executed successfully. SnippetId: ${snippetRunnerDTO.snippetId}")
        return ResponseEntity(snippetOutput, HttpStatus.OK)
    }

    @PostMapping("/format")
    fun formatSnippet(
        @RequestBody snippetRunnerDTO: SnippetDTO,
    ): ResponseEntity<SnippetOutputDTO> {
        logger.info("Received format snippet request. SnippetId: ${snippetRunnerDTO.snippetId}, UserId: ${snippetRunnerDTO.userId}, CorrelationId: ${snippetRunnerDTO.correlationId}")
        val languageService = snippetProcessingService.selectService(snippetRunnerDTO.language)
        val inputStream = ByteArrayInputStream(snippetRunnerDTO.input.toByteArray())
        val output =
            languageService.format(
                snippetRunnerDTO.snippetId,
                inputStream,
                snippetRunnerDTO.version,
                snippetRunnerDTO.userId,
                snippetRunnerDTO.correlationId,
            )
        val snippetOutput = SnippetOutputDTO(output.string, snippetRunnerDTO.correlationId, snippetRunnerDTO.snippetId)
        logger.info("Snippet formatted successfully. SnippetId: ${snippetRunnerDTO.snippetId}")
        return ResponseEntity(snippetOutput, HttpStatus.OK)
    }

    @PostMapping("/lint")
    fun runLinter(
        @RequestBody snippetRunnerDTO: SnippetDTO,
    ): ResponseEntity<List<SCAOutput>> {
        logger.info("Received lint request. UserId: ${snippetRunnerDTO.userId}, CorrelationId: ${snippetRunnerDTO.correlationId}")
        val languageService = snippetProcessingService.selectService(snippetRunnerDTO.language)
        val inputStream = ByteArrayInputStream(snippetRunnerDTO.input.toByteArray())
        val output =
            languageService.lint(
                inputStream,
                snippetRunnerDTO.version,
                snippetRunnerDTO.userId,
                snippetRunnerDTO.correlationId,
            )
        logger.info("Linting completed. Found ${output.size} issues")
        return ResponseEntity(output, HttpStatus.OK)
    }

    @GetMapping("/rules/format/{userId}")
    fun getFormatterRules(
        @PathVariable userId: String,
        @RequestHeader(value = "Correlation-id", required = false) correlationId: UUID?,
    ): ResponseEntity<List<Rule>> {
        val corr = correlationId ?: UUID.randomUUID()
        logger.info("Received get formatter rules request. UserId: $userId, CorrelationId: $corr")
        val formatterRules = formaterRulesService.getFormatterRulesByUserId(userId, corr)
        val rulesList = mutableListOf<Rule>()

        rulesList.add(Rule(id = "1", name = "spaceBeforeColon", isActive = formatterRules.spaceBeforeColon, value = false))
        rulesList.add(Rule(id = "2", name = "spaceAfterColon", isActive = formatterRules.spaceAfterColon, value = false))
        rulesList.add(Rule(id = "3", name = "spaceAroundEquals", isActive = formatterRules.spaceAroundEquals, value = false))
        rulesList.add(Rule(id = "4", name = "lineBreak", isActive = formatterRules.lineBreak != 0, value = formatterRules.lineBreak))
        rulesList.add(
            Rule(
                id = "5",
                name = "lineBreakPrintln",
                isActive = formatterRules.lineBreakPrintln != 0,
                value = formatterRules.lineBreakPrintln,
            ),
        )
        rulesList.add(
            Rule(
                id = "6",
                name = "conditionalIndentation",
                isActive = formatterRules.conditionalIndentation != 0,
                value = formatterRules.conditionalIndentation,
            ),
        )

        logger.info("Returning ${rulesList.size} formatter rules for userId: $userId")
        return ResponseEntity.ok(rulesList)
    }

    @GetMapping("/rules/lint/{userId}")
    fun getLinterRules(
        @PathVariable userId: String,
        @RequestHeader(value = "Correlation-id", required = false) correlationId: UUID?,
    ): ResponseEntity<List<Rule>> {
        val corr = correlationId ?: UUID.randomUUID()
        logger.info("Received get linter rules request. UserId: $userId, CorrelationId: $corr")
        val linterRules = linterRulesService.getLinterRulesByUserId(userId, corr)
        val rulesList = mutableListOf<Rule>()

        rulesList.add(
            Rule(id = "1", name = "identifierFormat", isActive = linterRules.identifierFormat != "", value = linterRules.identifierFormat),
        )
        rulesList.add(Rule(id = "2", name = "enablePrintOnly", isActive = linterRules.enablePrintOnly, value = false))
        rulesList.add(Rule(id = "3", name = "enableInputOnly", isActive = linterRules.enableInputOnly, value = false))

        logger.info("Returning ${rulesList.size} linter rules for userId: $userId")
        return ResponseEntity.ok(rulesList)
    }

    @PostMapping("/rules/format/{userId}")
    fun updateFormatterRules(
        @PathVariable userId: String,
        @RequestHeader(value = "Correlation-id", required = false) correlationId: UUID?,
        @RequestBody rules: List<Rule>,
    ): ResponseEntity<List<Rule>> {
        val corr = correlationId ?: UUID.randomUUID()
        logger.info("Received update formatter rules request. UserId: $userId, CorrelationId: $corr")

        // Validar header X-User-Id si viene presente
        // Nota: Spring no permite duplicar parámetros en firma; leer el header manualmente si es necesario en overload.
        // Aquí se asume la validación por encabezado adicional en la ruta /rules

        // Map incoming list to FormatterRulesFileDTO
        val spaceBeforeColon = rules.find { it.name == "spaceBeforeColon" }?.isActive ?: false
        val spaceAfterColon = rules.find { it.name == "spaceAfterColon" }?.isActive ?: false
        val spaceAroundEquals = rules.find { it.name == "spaceAroundEquals" }?.isActive ?: false
        val lineBreak = rules.find { it.name == "lineBreak" }?.value as? Int ?: 0
        val lineBreakPrintln = rules.find { it.name == "lineBreakPrintln" }?.value as? Int ?: 0
        val conditionalIndentation = rules.find { it.name == "conditionalIndentation" }?.value as? Int ?: 0

        val formatterRulesDto = FormatterRulesFileDTO(
            spaceBeforeColon,
            spaceAfterColon,
            spaceAroundEquals,
            lineBreak,
            lineBreakPrintln,
            conditionalIndentation,
        )

        val updated = formaterRulesService.updateFormatterRules(formatterRulesDto, userId)

        // Convert back to List<Rule> for response
        val responseList = mutableListOf<Rule>()
        responseList.add(Rule(id = "1", name = "spaceBeforeColon", isActive = updated.spaceBeforeColon, value = false))
        responseList.add(Rule(id = "2", name = "spaceAfterColon", isActive = updated.spaceAfterColon, value = false))
        responseList.add(Rule(id = "3", name = "spaceAroundEquals", isActive = updated.spaceAroundEquals, value = false))
        responseList.add(Rule(id = "4", name = "lineBreak", isActive = updated.lineBreak != 0, value = updated.lineBreak))
        responseList.add(Rule(id = "5", name = "lineBreakPrintln", isActive = updated.lineBreakPrintln != 0, value = updated.lineBreakPrintln))
        responseList.add(Rule(id = "6", name = "conditionalIndentation", isActive = updated.conditionalIndentation != 0, value = updated.conditionalIndentation))

        logger.info("Formatter rules updated for userId: $userId")
        return ResponseEntity.ok(responseList)
    }

    @PostMapping("/rules/lint/{userId}")
    fun updateLinterRules(
        @PathVariable userId: String,
        @RequestHeader(value = "Correlation-id", required = false) correlationId: UUID?,
        @RequestBody rules: List<Rule>,
    ): ResponseEntity<List<Rule>> {
        val corr = correlationId ?: UUID.randomUUID()
        logger.info("Received update linter rules request. UserId: $userId, CorrelationId: $corr")

        val identifierFormat = rules.find { it.name == "identifierFormat" }?.value as? String ?: ""
        val enablePrintOnly = rules.find { it.name == "enablePrintOnly" }?.isActive ?: false
        val enableInputOnly = rules.find { it.name == "enableInputOnly" }?.isActive ?: false

        val linterRulesDto = LinterRulesFileDTO(
            userId,
            identifierFormat,
            enablePrintOnly,
            enableInputOnly,
        )

        val updated = linterRulesService.updateLinterRules(linterRulesDto, userId)

        // Convert back to List<Rule>
        val responseList = mutableListOf<Rule>()
        responseList.add(Rule(id = "1", name = "identifierFormat", isActive = updated.identifier_format != "", value = updated.identifier_format))
        responseList.add(Rule(id = "2", name = "enablePrintOnly", isActive = updated.enablePrintOnly, value = false))
        responseList.add(Rule(id = "3", name = "enableInputOnly", isActive = updated.enableInputOnly, value = false))

        logger.info("Linter rules updated for userId: $userId")
        return ResponseEntity.ok(responseList)
    }

    @PostMapping("/test")
    fun makeTest(
        @RequestBody testDto: TestDTO,
    ): ResponseEntity<String> {
        logger.info("Received test request")
        logger.debug("Test with ${testDto.output.size} expected outputs")
        val languageService = snippetProcessingService.selectService("printscript")
        val result = languageService.test(testDto.input, testDto.output, testDto.snippet, testDto.envVars)
        logger.info("Test completed with result: $result")
        return ResponseEntity(result, HttpStatus.OK)
    }

    // Endpoints PUT para recibir eventos desde el conector (redis stream)
    @PutMapping("/redis/format/snippet")
    fun redisFormatSnippet(
        @RequestBody snippet: Snippet,
    ): ResponseEntity<Any> {
        logger.info("Received redis format snippet event: $snippet")
        return try {
            // Procesar de forma tolerant: no lanzar si hay error en producción
            redisService.formatSnippet(snippet)
            ResponseEntity.ok(mapOf<String, String>())
        } catch (e: Exception) {
            logger.error("Error processing redis format snippet: ${e.message}", e)
            // Devolver 200 con body vacío para ser tolerante, o 500 si se prefiere; aquí devolvemos 200 con {} según especificación
            ResponseEntity.ok(mapOf<String, String>())
        }
    }

    @PutMapping("/redis/lint/snippet")
    fun redisLintSnippet(
        @RequestBody snippet: Snippet,
    ): ResponseEntity<Any> {
        logger.info("Received redis lint snippet event: $snippet")
        return try {
            redisService.lintSnippet(snippet)
            ResponseEntity.ok(mapOf<String, String>())
        } catch (e: Exception) {
            logger.error("Error processing redis lint snippet: ${e.message}", e)
            ResponseEntity.ok(mapOf<String, String>())
        }
    }

    @PutMapping("/redis/test/snippet")
    fun redisTestSnippet(
        @RequestBody snippet: Snippet,
    ): ResponseEntity<Any> {
        logger.info("Received redis test snippet event: $snippet")
        return try {
            redisService.testSnippet(snippet)
            ResponseEntity.ok(mapOf<String, String>())
        } catch (e: Exception) {
            logger.error("Error processing redis test snippet: ${e.message}", e)
            ResponseEntity.ok(mapOf<String, String>())
        }
    }
}
