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
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayInputStream
import java.util.UUID


@RestController
class SnippetController(
    private val snippetProcessingService: SnippetProcessingService,
    val linterRulesService: LinterRulesService,
    val formaterRulesService: FormatterRulesService,
) {
    @PostMapping("/validate")
    fun validateSnippet(
        @RequestBody validate: String,
    ): ValidationResult {
        val languageService = snippetProcessingService.selectService("printscript")
        val result = languageService.validate(validate, "1.1")
        return result
    }
    // En caso de que no se cumpla alguna de las reglas isvalid:False y te dara la columna, y la fila, en cambio si es valido devolver isvalid: true, teniendo en cuenta

    @PostMapping("/run")
    fun runSnippet(
        @RequestBody snippetRunnerDTO: SnippetDTO,
    ): ResponseEntity<SnippetOutputDTO> {
        val languageService = snippetProcessingService.selectService(snippetRunnerDTO.language)
        val inputStream = ByteArrayInputStream(snippetRunnerDTO.input.toByteArray())
        val output: Output = languageService.runScript(inputStream, snippetRunnerDTO.version)
        val snippetOutput = SnippetOutputDTO(output.string, snippetRunnerDTO.correlationId, snippetRunnerDTO.snippetId)
        return ResponseEntity(snippetOutput, HttpStatus.OK)
    }

    @PostMapping("/format")
    fun formatSnippet(
        @RequestBody snippetRunnerDTO: SnippetDTO,
    ): ResponseEntity<SnippetOutputDTO> {
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
        return ResponseEntity(snippetOutput, HttpStatus.OK)
    }

    @PostMapping("/lint")
    fun runLinter(
        @RequestBody snippetRunnerDTO: SnippetDTO,
    ): ResponseEntity<List<SCAOutput>> {
        val languageService = snippetProcessingService.selectService(snippetRunnerDTO.language)
        val inputStream = ByteArrayInputStream(snippetRunnerDTO.input.toByteArray())
        val output =
            languageService.lint(
                inputStream,
                snippetRunnerDTO.version,
                snippetRunnerDTO.userId,
                snippetRunnerDTO.correlationId,
            )
        return ResponseEntity(output, HttpStatus.OK)
    }

    @GetMapping("/format/{userId}")
    fun getLinterRules(
        @PathVariable userId: String,
        @RequestHeader("Correlation-id") correlationId: UUID,
    ): ResponseEntity<List<Rule>> {
        val formatterRules = formaterRulesService.getFormatterRulesByUserId(userId, correlationId)
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

        return ResponseEntity.ok(rulesList)
    }

    @GetMapping("/lint/{userId}")
    fun getFormatterRules(
        @PathVariable userId: String,
        @RequestHeader("Correlation-id") correlationId: UUID,
    ): ResponseEntity<List<Rule>> {
        val linterRules = linterRulesService.getLinterRulesByUserId(userId, correlationId)
        val rulesList = mutableListOf<Rule>()

        rulesList.add(
            Rule(id = "1", name = "identifierFormat", isActive = linterRules.identifierFormat != "", value = linterRules.identifierFormat),
        )
        rulesList.add(Rule(id = "2", name = "enablePrintOnly", isActive = linterRules.enablePrintOnly, value = false))
        rulesList.add(Rule(id = "3", name = "enableInputOnly", isActive = linterRules.enableInputOnly, value = false))

        return ResponseEntity.ok(rulesList)
    }

    @PostMapping("/test")
    fun makeTest(
        @RequestBody testDto: TestDTO,
    ): ResponseEntity<String> {
        val languageService = snippetProcessingService.selectService("printscript")
        val result = languageService.test(testDto.input, testDto.output, testDto.snippet, testDto.envVars)
        return ResponseEntity(result, HttpStatus.OK)
    }
}
