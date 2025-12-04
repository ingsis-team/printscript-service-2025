package com.ingsisteam.printscriptservice2025

import com.ingsisteam.printscriptservice2025.model.Output
import com.ingsisteam.printscriptservice2025.service.FormatterRulesService
import com.ingsisteam.printscriptservice2025.service.LinterRulesService
import com.ingsisteam.printscriptservice2025.service.PrintScriptService
import formatter.FormatterPS
import lexer.TokenMapper
import linter.Linter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import parser.Parser
import java.io.InputStream

class PrintScriptServiceTest {

    private lateinit var tokenMapper: TokenMapper
    private lateinit var parser: Parser
    private lateinit var linter: Linter
    private lateinit var formatter: FormatterPS
    private lateinit var formatterRulesService: FormatterRulesService
    private lateinit var linterRulesService: LinterRulesService
    private lateinit var printScriptService: PrintScriptService

    private lateinit var printScriptServiceSpy: PrintScriptService

    @BeforeEach
    fun setUp() {
        tokenMapper = mock()
        parser = mock()
        linter = mock()
        formatter = mock()
        formatterRulesService = mock()
        linterRulesService = mock()
        printScriptService = PrintScriptService(
            tokenMapper,
            parser,
            linter,
            formatter,
            formatterRulesService,
            linterRulesService,
            "localhost:8080",
        )
        printScriptServiceSpy = spy(printScriptService)
    }

    @Test
    fun `test should return success when output matches`() {
        val snippet = "println('hello');"
        val expectedOutput = listOf("hello")

        doReturn(Output("hello")).`when`(printScriptServiceSpy).runScript(any<InputStream>(), any())

        val result = printScriptServiceSpy.test("", expectedOutput, snippet, "")
        assertEquals("success", result)
    }

    @Test
    fun `test should return failure when output does not match`() {
        val snippet = "println('hello');"
        val expectedOutput = listOf("world")

        doReturn(Output("hello")).`when`(printScriptServiceSpy).runScript(any<InputStream>(), any())

        val result = printScriptServiceSpy.test("", expectedOutput, snippet, "")
        assertEquals("failure", result)
    }
}
