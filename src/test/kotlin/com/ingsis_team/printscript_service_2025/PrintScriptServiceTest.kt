package com.ingsis_team.printscript_service_2025
import formatter.FormatterPS
import lexer.Lexer
import lexer.TokenMapper
import linter.Linter
import linter.LinterVersion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import parser.Parser
import com.ingsis_team.printscript_service_2025.model.Output
import com.ingsis_team.printscript_service_2025.model.repository.FormatterRulesRepository
import com.ingsis_team.printscript_service_2025.model.repository.LinterRulesRepository
import com.ingsis_team.printscript_service_2025.service.FormatterRulesService
import com.ingsis_team.printscript_service_2025.service.LinterRulesService
import com.ingsis_team.printscript_service_2025.service.PrintScriptService
import rules.RulesReader

class PrintScriptServiceTest {
    private lateinit var service: PrintScriptService
    val tokenMapper = TokenMapper("1.0")

    @BeforeEach
    fun setUp() {
        val tokenMapper = TokenMapper("1.0")
        val parser = Parser()
        val linter = Linter(LinterVersion.fromString("1.0")!!)
        val formatter =
            FormatterPS(
                RulesReader(
                    mapOf(
                        "spaceBeforeColon" to Boolean::class,
                        "spaceAfterColon" to Boolean::class,
                        "spaceAroundEquals" to Boolean::class,
                        "lineBreakPrintln" to Int::class,
                    ),
                ),
                "C:\\Users\\vgian\\Projects\\printscript-service\\src\\main\\kotlin\\printscript\\model\\rules\\FormatterRules.kt",
                emptyList(),
                Lexer(TokenMapper("1.0")),
                Parser(),
            )
        val formatterRepository = mock(FormatterRulesRepository::class.java)
        val linterRepository = mock(LinterRulesRepository::class.java)
        val formatterService = FormatterRulesService(formatterRepository)
        val linterRulesService = LinterRulesService(linterRepository)
        val permissionUrl = "localhost"

        service =
            PrintScriptService(
                tokenMapper,
                parser,
                linter,
                formatter,
                formatterService,
                linterRulesService,
                permissionUrl,
            )
    }

    @Test
    fun `runScript should return output from parsed AST`() {
        val input = "let x : number = 8;".byteInputStream()
        val inputText = input.bufferedReader().readText()
        val version = "1.0"

        // Generate tokens
        val lexer = Lexer(tokenMapper)
        val tokens = lexer.execute(inputText)
        assertFalse(tokens.isEmpty(), "Lexer did not generate any tokens")

        // Parse tokens
        val parser = Parser()
        val parsedAst = parser.execute(tokens)
        assertNotNull(parsedAst, "Parser did not generate a valid AST")

        val expectedOutput = Output(parsedAst.toString())

        // Reset the input stream before passing it to the service
        val inputStream = "let x : number = 8;".byteInputStream()
        val result = service.runScript(inputStream, version)

        assertEquals(expectedOutput, result)
    }

    @Test
    fun `test should return failure when output does not match`() {
        val input = "let x : number = 8;"
        val output = listOf("20")
        val snippet = "let x : number = 7;"
        val envVars = "VAR1=value1,VAR2=value2"

        val result = service.test(input, output, snippet, envVars)

        assertEquals("failure", result)
    }

    @Test
    fun `validate should return ValidationResult`() {
        val input = "let x : number = 8;"
        val version = "1.0"

        val result = service.validate(input, version)

        assertNotNull(result, "ValidationResult should not be null")
        if (result.isValid) {
            assertEquals(true, result.isValid, "ValidationResult should be valid")
        } else {
            assertEquals(false, result.isValid, "ValidationResult should be invalid")
            assertNotNull(result.rule, "Rule should not be null")
            assertNotNull(result.line, "Line should not be null")
            assertNotNull(result.column, "Column should not be null")
        }
    }
}
