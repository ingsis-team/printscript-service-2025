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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayInputStream
import java.util.UUID
import com.ingsis_team.printscript_service_2025.model.dto.FormatterRulesFileDTO
import com.ingsis_team.printscript_service_2025.model.dto.LinterRulesFileDTO
import com.ingsis_team.printscript_service_2025.exception.ValidationException
import com.ingsis_team.printscript_service_2025.interfaces.IRedisService
import com.ingsis_team.printscript_service_2025.redis.dto.Snippet
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag


@RestController
@RequestMapping
@Tag(name = "Snippet Operations", description = "API para operaciones de snippets de PrintScript: validación, ejecución, formateo, linting y gestión de reglas")
class SnippetController(
    private val snippetProcessingService: SnippetProcessingService,
    val linterRulesService: LinterRulesService,
    val formaterRulesService: FormatterRulesService,
    private val redisService: IRedisService,
) {
    private val logger = LoggerFactory.getLogger(SnippetController::class.java)

    @Operation(
        summary = "Validar código de snippet",
        description = "Valida la sintaxis de un snippet de código PrintScript sin ejecutarlo"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Validación completada exitosamente",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ValidationResult::class))]),
        ApiResponse(responseCode = "400", description = "Contenido inválido o vacío"),
        ApiResponse(responseCode = "500", description = "Error interno del servidor")
    ])
    @PostMapping("/validate")
    fun validateSnippet(
        @Parameter(description = "Código del snippet a validar", required = true)
        @RequestBody validate: String,
    ): ValidationResult {
        logger.info("Received validation request. Content length: ${validate.length}, Content preview: '${validate.take(100).replace("\n", "\\n").replace("\r", "\\r")}'")
        if (validate.isBlank()) {
            logger.warn("Received empty or blank content for validation")
            return ValidationResult(
                isValid = false,
                rule = "Empty content",
                line = 0,
                column = 0
            )
        }
        val languageService = snippetProcessingService.selectService("printscript")
        val result = languageService.validate(validate, "1.1")
        logger.info("Validation completed. IsValid: ${result.isValid}")
        return result
    }

    @Operation(
        summary = "Ejecutar snippet",
        description = "Ejecuta un snippet de código PrintScript y devuelve el resultado"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Snippet ejecutado exitosamente",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = SnippetOutputDTO::class))]),
        ApiResponse(responseCode = "400", description = "Datos del snippet inválidos"),
        ApiResponse(responseCode = "500", description = "Error durante la ejecución")
    ])
    @PostMapping("/run")
    fun runSnippet(
        @Parameter(description = "Datos del snippet a ejecutar", required = true)
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

    @Operation(
        summary = "Formatear snippet",
        description = "Aplica reglas de formateo al código del snippet según las reglas configuradas por el usuario"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Snippet formateado exitosamente",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = SnippetOutputDTO::class))]),
        ApiResponse(responseCode = "400", description = "Datos del snippet inválidos"),
        ApiResponse(responseCode = "404", description = "Reglas de formateo no encontradas"),
        ApiResponse(responseCode = "500", description = "Error durante el formateo")
    ])
    @PostMapping("/format")
    fun formatSnippet(
        @Parameter(description = "Datos del snippet a formatear", required = true)
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

    @Operation(
        summary = "Analizar snippet con linter",
        description = "Ejecuta análisis estático de código (linting) sobre el snippet según las reglas configuradas"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Análisis completado, retorna lista de problemas encontrados",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = SCAOutput::class))]),
        ApiResponse(responseCode = "400", description = "Datos del snippet inválidos"),
        ApiResponse(responseCode = "404", description = "Reglas de linter no encontradas"),
        ApiResponse(responseCode = "500", description = "Error durante el análisis")
    ])
    @PostMapping("/lint")
    fun runLinter(
        @Parameter(description = "Datos del snippet a analizar", required = true)
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

    @Operation(
        summary = "Obtener reglas de formateo",
        description = "Obtiene las reglas de formateo configuradas para un usuario específico"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Reglas obtenidas exitosamente",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = Rule::class))]),
        ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        ApiResponse(responseCode = "500", description = "Error al obtener reglas")
    ])
    @GetMapping("/rules/format/{userId}")
    fun getFormatterRules(
        @Parameter(description = "ID del usuario", required = true)
        @PathVariable userId: String,
        @Parameter(description = "ID de correlación para trazabilidad")
        @RequestHeader(value = "Correlation-id", required = false) correlationId: UUID?,
    ): ResponseEntity<List<Rule>> {
        val corr = correlationId ?: UUID.randomUUID()
        logger.info("Received get formatter rules request. UserId: $userId, CorrelationId: $corr")
        val formatterRules = formaterRulesService.getFormatterRulesByUserId(userId, corr)
        val rulesList = listOf(
            Rule(name = "spaceBeforeColon", value = formatterRules.spaceBeforeColon),
            Rule(name = "spaceAfterColon", value = formatterRules.spaceAfterColon),
            Rule(name = "spaceAroundEquals", value = formatterRules.spaceAroundEquals),
            Rule(name = "lineBreak", value = formatterRules.lineBreak),
            Rule(name = "lineBreakPrintln", value = formatterRules.lineBreakPrintln),
            Rule(name = "conditionalIndentation", value = formatterRules.conditionalIndentation),
        )

        logger.info("Returning ${rulesList.size} formatter rules for userId: $userId")
        return ResponseEntity.ok(rulesList)
    }

    @Operation(
        summary = "Obtener reglas de linter",
        description = "Obtiene las reglas de análisis estático configuradas para un usuario específico"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Reglas obtenidas exitosamente",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = Rule::class))]),
        ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
        ApiResponse(responseCode = "500", description = "Error al obtener reglas")
    ])
    @GetMapping("/rules/lint/{userId}")
    fun getLinterRules(
        @Parameter(description = "ID del usuario", required = true)
        @PathVariable userId: String,
        @Parameter(description = "ID de correlación para trazabilidad")
        @RequestHeader(value = "Correlation-id", required = false) correlationId: UUID?,
    ): ResponseEntity<List<Rule>> {
        val corr = correlationId ?: UUID.randomUUID()
        logger.info("Received get linter rules request. UserId: $userId, CorrelationId: $corr")
        val linterRules = linterRulesService.getLinterRulesByUserId(userId, corr)
        val rulesList = listOf(
            Rule(name = "identifier_format", value = linterRules.identifierFormat),
            Rule(name = "enablePrintOnly", value = linterRules.enablePrintOnly),
            Rule(name = "enableInputOnly", value = linterRules.enableInputOnly),
        )

        logger.info("Returning ${rulesList.size} linter rules for userId: $userId")
        return ResponseEntity.ok(rulesList)
    }

    @Operation(
        summary = "Actualizar reglas de formateo",
        description = "Actualiza las reglas de formateo para un usuario. Las reglas disponibles son: spaceBeforeColon, spaceAfterColon, spaceAroundEquals, lineBreak, lineBreakPrintln, conditionalIndentation"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Reglas actualizadas exitosamente",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = Rule::class))]),
        ApiResponse(responseCode = "400", description = "Reglas inválidas"),
        ApiResponse(responseCode = "500", description = "Error al actualizar reglas")
    ])
    @PostMapping("/rules/format/{userId}")
    fun updateFormatterRules(
        @Parameter(description = "ID del usuario", required = true)
        @PathVariable userId: String,
        @Parameter(description = "ID de correlación para trazabilidad")
        @RequestHeader(value = "Correlation-id", required = false) correlationId: UUID?,
        @Parameter(description = "Lista de reglas a actualizar (name-value pairs)", required = true)
        @RequestBody rules: List<Rule>,
    ): ResponseEntity<List<Rule>> {
        val corr = correlationId ?: UUID.randomUUID()
        logger.info("Received update formatter rules request. UserId: $userId, CorrelationId: $corr, Rules: $rules")

        val spaceBeforeColon = rules.find { it.name == "spaceBeforeColon" }?.value as? Boolean ?: true
        val spaceAfterColon = rules.find { it.name == "spaceAfterColon" }?.value as? Boolean ?: true
        val spaceAroundEquals = rules.find { it.name == "spaceAroundEquals" }?.value as? Boolean ?: true
        val lineBreak = (rules.find { it.name == "lineBreak" }?.value as? Number)?.toInt() ?: 1
        val lineBreakPrintln = (rules.find { it.name == "lineBreakPrintln" }?.value as? Number)?.toInt() ?: 1
        val conditionalIndentation = (rules.find { it.name == "conditionalIndentation" }?.value as? Number)?.toInt() ?: 4

        val formatterRulesDto = FormatterRulesFileDTO(
            spaceBeforeColon,
            spaceAfterColon,
            spaceAroundEquals,
            lineBreak,
            lineBreakPrintln,
            conditionalIndentation,
        )

        val updated = formaterRulesService.updateFormatterRules(formatterRulesDto, userId)

        val responseList = listOf(
            Rule(name = "spaceBeforeColon", value = updated.spaceBeforeColon),
            Rule(name = "spaceAfterColon", value = updated.spaceAfterColon),
            Rule(name = "spaceAroundEquals", value = updated.spaceAroundEquals),
            Rule(name = "lineBreak", value = updated.lineBreak),
            Rule(name = "lineBreakPrintln", value = updated.lineBreakPrintln),
            Rule(name = "conditionalIndentation", value = updated.conditionalIndentation),
        )

        logger.info("Formatter rules updated for userId: $userId")
        return ResponseEntity.ok(responseList)
    }

    @Operation(
        summary = "Actualizar reglas de linter",
        description = "Actualiza las reglas de análisis estático para un usuario. Las reglas disponibles son: identifier_format (camelcase/snakecase), enablePrintOnly, enableInputOnly"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Reglas actualizadas exitosamente",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = Rule::class))]),
        ApiResponse(responseCode = "400", description = "Reglas inválidas o identifier_format debe ser 'camelcase' o 'snakecase'"),
        ApiResponse(responseCode = "500", description = "Error al actualizar reglas")
    ])
    @PostMapping("/rules/lint/{userId}")
    fun updateLinterRules(
        @Parameter(description = "ID del usuario", required = true)
        @PathVariable userId: String,
        @Parameter(description = "ID de correlación para trazabilidad")
        @RequestHeader(value = "Correlation-id", required = false) correlationId: UUID?,
        @Parameter(description = "Lista de reglas a actualizar (name-value pairs)", required = true)
        @RequestBody rules: List<Rule>,
    ): ResponseEntity<List<Rule>> {
        val corr = correlationId ?: UUID.randomUUID()
        logger.info("Received update linter rules request. UserId: $userId, CorrelationId: $corr, Rules: $rules")

        val identifierFormat = rules.find { it.name == "identifier_format" }?.value as? String ?: "camelcase"
        val enablePrintOnly = rules.find { it.name == "enablePrintOnly" }?.value as? Boolean ?: true
        val enableInputOnly = rules.find { it.name == "enableInputOnly" }?.value as? Boolean ?: true

        // Validar que identifier_format sea válido
        if (identifierFormat != "camelcase" && identifierFormat != "snakecase") {
            logger.warn("Invalid identifier_format value: $identifierFormat. Using 'camelcase' as default")
            throw ValidationException("identifier_format must be either 'camelcase' or 'snakecase'")
        }

        val linterRulesDto = LinterRulesFileDTO(
            userId,
            identifierFormat,
            enablePrintOnly,
            enableInputOnly,
        )

        val updated = linterRulesService.updateLinterRules(linterRulesDto, userId)

        val responseList = listOf(
            Rule(name = "identifier_format", value = updated.identifier_format),
            Rule(name = "enablePrintOnly", value = updated.enablePrintOnly),
            Rule(name = "enableInputOnly", value = updated.enableInputOnly),
        )

        logger.info("Linter rules updated for userId: $userId")
        return ResponseEntity.ok(responseList)
    }

    @Operation(
        summary = "Ejecutar tests sobre snippet",
        description = "Ejecuta tests sobre un snippet comparando la salida esperada con la salida real"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Test ejecutado, retorna 'success' o 'failure'"),
        ApiResponse(responseCode = "400", description = "Datos de test inválidos"),
        ApiResponse(responseCode = "500", description = "Error durante la ejecución del test")
    ])
    @PostMapping("/test")
    fun makeTest(
        @Parameter(description = "Datos del test a ejecutar", required = true)
        @RequestBody testDto: TestDTO,
    ): ResponseEntity<String> {
        logger.info("Received test request")
        logger.debug("Test with ${testDto.output.size} expected outputs")
        val languageService = snippetProcessingService.selectService("printscript")
        val result = languageService.test(testDto.input, testDto.output, testDto.snippet, testDto.envVars)
        logger.info("Test completed with result: $result")
        return ResponseEntity(result, HttpStatus.OK)
    }

    @Operation(
        summary = "Webhook para formateo de snippet desde Redis",
        description = "Endpoint interno para recibir eventos de formateo desde Redis Stream"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Evento procesado"),
        ApiResponse(responseCode = "500", description = "Error procesando evento (se devuelve 200 para tolerancia)")
    ])
    @PutMapping("/redis/format/snippet")
    fun redisFormatSnippet(
        @Parameter(description = "Datos del snippet recibidos desde Redis", required = true)
        @RequestBody snippet: Snippet,
    ): ResponseEntity<Any> {
        logger.info("Received redis format snippet event: $snippet")
        return try {
            redisService.formatSnippet(snippet)
            ResponseEntity.ok(mapOf<String, String>())
        } catch (e: Exception) {
            logger.error("Error processing redis format snippet: ${e.message}", e)
            ResponseEntity.ok(mapOf<String, String>())
        }
    }

    @Operation(
        summary = "Webhook para linting de snippet desde Redis",
        description = "Endpoint interno para recibir eventos de linting desde Redis Stream"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Evento procesado"),
        ApiResponse(responseCode = "500", description = "Error procesando evento (se devuelve 200 para tolerancia)")
    ])
    @PutMapping("/redis/lint/snippet")
    fun redisLintSnippet(
        @Parameter(description = "Datos del snippet recibidos desde Redis", required = true)
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

    @Operation(
        summary = "Webhook para testing de snippet desde Redis",
        description = "Endpoint interno para recibir eventos de testing desde Redis Stream"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Evento procesado"),
        ApiResponse(responseCode = "500", description = "Error procesando evento (se devuelve 200 para tolerancia)")
    ])
    @PutMapping("/redis/test/snippet")
    fun redisTestSnippet(
        @Parameter(description = "Datos del snippet recibidos desde Redis", required = true)
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
