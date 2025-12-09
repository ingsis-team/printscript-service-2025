package com.ingsisteam.printscriptservice2025

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ingsisteam.printscriptservice2025.controller.SnippetController
import com.ingsisteam.printscriptservice2025.interfaces.IRedisService
import com.ingsisteam.printscriptservice2025.model.Output
import com.ingsisteam.printscriptservice2025.model.SCAOutput
import com.ingsisteam.printscriptservice2025.model.dto.FormatterRulesFileDTO
import com.ingsisteam.printscriptservice2025.model.dto.LinterRulesFileDTO
import com.ingsisteam.printscriptservice2025.model.dto.SnippetDTO
import com.ingsisteam.printscriptservice2025.model.dto.TestDTO
import com.ingsisteam.printscriptservice2025.model.dto.ValidationResult
import com.ingsisteam.printscriptservice2025.redis.dto.Rule
import com.ingsisteam.printscriptservice2025.redis.dto.Snippet
import com.ingsisteam.printscriptservice2025.service.FormatterRulesService
import com.ingsisteam.printscriptservice2025.service.LinterRulesService
import com.ingsisteam.printscriptservice2025.service.SnippetProcessingService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(controllers = [SnippetController::class])
class SnippetControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var snippetProcessingService: SnippetProcessingService

    @MockBean
    private lateinit var linterRulesService: LinterRulesService

    @MockBean
    private lateinit var formaterRulesService: FormatterRulesService

    @MockBean
    private lateinit var redisService: IRedisService

    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private lateinit var languageServiceMock: com.ingsisteam.printscriptservice2025.interfaces.LanguageService

    @BeforeEach
    fun setUp() {
        languageServiceMock = mock()
        whenever(snippetProcessingService.selectService(any())).thenReturn(languageServiceMock)
    }

    // --- POST /validate ---
    @Test
    fun `validateSnippet should return ValidationResult for valid input`() {
        val code = "let x = 1;"
        val expectedResult = ValidationResult(isValid = true, rule = "", line = 0, column = 0)
        whenever(languageServiceMock.validate(code, "1.1")).thenReturn(expectedResult)

        mockMvc.perform(
            post("/validate")
                .contentType(MediaType.TEXT_PLAIN) // Use TEXT_PLAIN if expecting raw string
                .content(code),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isValid").value(true))
            .andExpect(jsonPath("$.rule").value(""))
        verify(snippetProcessingService).selectService("printscript")
        verify(languageServiceMock).validate(code, "1.1")
    }

    @Test
    fun `validateSnippet should return invalid for blank input`() {
        val code = "   "
        mockMvc.perform(
            post("/validate")
                .contentType(MediaType.TEXT_PLAIN)
                .content(code),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isValid").value(false))
            .andExpect(jsonPath("$.rule").value("Empty content"))
        verify(snippetProcessingService, never()).selectService(any())
    }

    @Test
    fun `validateSnippet should return invalid result from service`() {
        val code = "invalid code"
        val expectedResult = ValidationResult(isValid = false, rule = "Syntax error", line = 1, column = 5)
        whenever(languageServiceMock.validate(code, "1.1")).thenReturn(expectedResult)

        mockMvc.perform(
            post("/validate")
                .contentType(MediaType.TEXT_PLAIN)
                .content(code),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isValid").value(false))
            .andExpect(jsonPath("$.rule").value("Syntax error"))
        verify(snippetProcessingService).selectService("printscript")
        verify(languageServiceMock).validate(code, "1.1")
    }

    // --- POST /run ---
    @Test
    fun `runSnippet should return SnippetOutputDTO`() {
        val dto = SnippetDTO(
            input = "print('hi')",
            snippetId = "s1",
            correlationId = UUID.randomUUID(),
            language = "printscript",
            version = "1.1",
            userId = "u1",
        )
        whenever(languageServiceMock.runScript(any(), any())).thenReturn(Output("ok"))

        mockMvc.perform(
            post("/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.snippet").value("ok"))
            .andExpect(jsonPath("$.snippetId").value("s1"))
        verify(snippetProcessingService).selectService("printscript")
        verify(languageServiceMock).runScript(any(), eq("1.1"))
    }

    // --- POST /format ---
    @Test
    fun `formatSnippet should return SnippetOutputDTO`() {
        val dto = SnippetDTO(
            input = "let x=1",
            snippetId = "s2",
            correlationId = UUID.randomUUID(),
            language = "printscript",
            version = "1.1",
            userId = "u2",
        )
        whenever(languageServiceMock.format(any(), any(), any(), any(), any())).thenReturn(Output("formatted"))

        mockMvc.perform(
            post("/format")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.snippet").value("formatted"))
            .andExpect(jsonPath("$.snippetId").value("s2"))
        verify(snippetProcessingService).selectService("printscript")
        verify(languageServiceMock).format(eq("s2"), any(), eq("1.1"), eq("u2"), eq(dto.correlationId))
    }

    // --- POST /runLinter ---
    @Test
    fun `runLinter should return list of SCAOutput`() {
        val dto = SnippetDTO(
            input = "let x=1",
            snippetId = "s3",
            correlationId = UUID.randomUUID(),
            language = "printscript",
            version = "1.1",
            userId = "u3",
        )
        val sca = listOf(SCAOutput(1, "rule", "desc"))
        whenever(languageServiceMock.lint(any(), any(), any(), any())).thenReturn(sca)

        mockMvc.perform(
            post("/lint")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].ruleBroken").value("rule"))
        verify(snippetProcessingService).selectService("printscript")
        verify(languageServiceMock).lint(any(), eq("1.1"), eq("u3"), any())
    }

    // --- GET rules ---
    @Test
    fun `getFormatterRules should return rules`() {
        val userId = "userA"
        val corr = UUID.randomUUID()
        whenever(formaterRulesService.getFormatterRulesByUserId(eq(userId), any())).thenReturn(
            com.ingsisteam.printscriptservice2025.model.rules.FormatterRules(
                userId = userId,
                spaceBeforeColon = true,
                spaceAfterColon = false,
                spaceAroundEquals = true,
                lineBreak = 2,
                lineBreakPrintln = 1,
                conditionalIndentation = 3,
            ),
        )
        mockMvc.perform(get("/rules/format/{userId}", userId).header("Correlation-id", corr.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("spaceBeforeColon"))
            .andExpect(jsonPath("$[3].name").value("lineBreak"))
        verify(formaterRulesService).getFormatterRulesByUserId(eq(userId), any())
    }

    @Test
    fun `getLinterRules should return rules`() {
        val userId = "userB"
        val corr = UUID.randomUUID()
        whenever(linterRulesService.getLinterRulesByUserId(eq(userId), any())).thenReturn(
            com.ingsisteam.printscriptservice2025.model.rules.LinterRules(
                userId = userId,
                identifierFormat = "snakecase",
                enablePrintOnly = true,
                enableInputOnly = false,
            ),
        )
        mockMvc.perform(get("/rules/lint/{userId}", userId).header("Correlation-id", corr.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("identifier_format"))
        verify(linterRulesService).getLinterRulesByUserId(eq(userId), any())
    }

    // --- POST rules updates ---
    @Test
    fun `updateFormatterRules should succeed`() {
        val userId = "userC"
        val corr = UUID.randomUUID()
        val rules = listOf(
            Rule("spaceBeforeColon", true),
            Rule("spaceAfterColon", false),
            Rule("spaceAroundEquals", true),
            Rule("lineBreak", 2),
            Rule("lineBreakPrintln", 1),
            Rule("conditionalIndentation", 3),
        )
        whenever(
            formaterRulesService.updateFormatterRules(any(), eq(userId)),
        ).thenReturn(FormatterRulesFileDTO(true, false, true, 2, 1, 3))

        mockMvc.perform(
            post("/rules/format/{userId}", userId)
                .header("Correlation-id", corr.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rules)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].value").value(true))
            .andExpect(jsonPath("$[5].value").value(3))
        verify(formaterRulesService).updateFormatterRules(any(), eq(userId))
    }

    @Test
    fun `updateLinterRules should succeed`() {
        val userId = "userD"
        val corr = UUID.randomUUID()
        val rules = listOf(
            Rule("identifier_format", "camelcase"),
            Rule("enablePrintOnly", true),
            Rule("enableInputOnly", false),
        )
        whenever(linterRulesService.updateLinterRules(any(), eq(userId)))
            .thenReturn(LinterRulesFileDTO(userId, "camelcase", true, false))

        mockMvc.perform(
            post("/rules/lint/{userId}", userId)
                .header("Correlation-id", corr.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rules)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].value").value("camelcase"))
        verify(linterRulesService).updateLinterRules(any(), eq(userId))
    }

    // --- Redis webhook endpoints in SnippetController ---
    @Test
    fun `redisFormatSnippet should return ok and call service`() {
        val snippet = Snippet("u1", "s1", "code", UUID.randomUUID())
        mockMvc.perform(
            put("/redis/format/snippet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(snippet)),
        ).andExpect(status().isOk)
        verify(redisService).formatSnippet(any())
    }

    @Test
    fun `redisFormatSnippet should return ok even on exception`() {
        val snippet = Snippet("u1", "s1", "code", UUID.randomUUID())
        whenever(redisService.formatSnippet(any())).thenThrow(RuntimeException("boom"))
        mockMvc.perform(
            put("/redis/format/snippet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(snippet)),
        ).andExpect(status().isOk)
        verify(redisService).formatSnippet(any())
    }

    @Test
    fun `redisLintSnippet should return ok and call service`() {
        val snippet = Snippet("u1", "s1", "code", UUID.randomUUID())
        mockMvc.perform(
            put("/redis/lint/snippet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(snippet)),
        ).andExpect(status().isOk)
        verify(redisService).lintSnippet(any())
    }

    @Test
    fun `redisTestSnippet should return ok and call service`() {
        val snippet = Snippet("u1", "s1", "code", UUID.randomUUID())
        mockMvc.perform(
            put("/redis/test/snippet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(snippet)),
        ).andExpect(status().isOk)
        verify(redisService).testSnippet(any())
    }

    // --- POST /test ---
    @Test
    fun `makeTest should return success for valid testDto`() {
        val testDto = TestDTO("input", "snippet", listOf("output"), "env")
        whenever(languageServiceMock.test(any(), any(), any(), any())).thenReturn("success")

        mockMvc.perform(
            post("/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDto)),
        )
            .andExpect(status().isOk)
            .andExpect(content().string("success"))
        verify(snippetProcessingService).selectService("printscript")
        verify(languageServiceMock).test(any(), any(), any(), any())
    }

    @Test
    fun `makeTest should return failure for failed testDto`() {
        val testDto = TestDTO("input", "snippet", listOf("output"), "env")
        whenever(languageServiceMock.test(any(), any(), any(), any())).thenReturn("failure")

        mockMvc.perform(
            post("/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testDto)),
        )
            .andExpect(status().isOk)
            .andExpect(content().string("failure"))
    }

    @Test
    fun `updateLinterRules should handle invalid identifier_format`() {
        val userId = "user1"
        val correlationId = UUID.randomUUID()
        val rules = listOf(Rule("identifier_format", "invalid_case"))

        mockMvc.perform(
            post("/rules/lint/{userId}", userId)
                .header("Correlation-id", correlationId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rules)),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("identifier_format must be either 'camelcase' or 'snakecase'"))
    }
}
