package com.ingsisteam.printscriptservice2025

import com.ingsisteam.printscriptservice2025.service.FormatterRulesService
import com.ingsisteam.printscriptservice2025.service.LinterRulesService
import com.ingsisteam.printscriptservice2025.service.PrintScriptService
import formatter.FormatterPS
import lexer.Lexer
import lexer.TokenMapper
import linter.Linter
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import parser.Parser

class PrintScriptServiceTests {

    private lateinit var printScriptService: PrintScriptService
    private lateinit var tokenMapper: TokenMapper
    private lateinit var parser: Parser
    private lateinit var linter: Linter
    private lateinit var formatter: FormatterPS
    private lateinit var formatterService: FormatterRulesService
    private lateinit var linterRulesService: LinterRulesService
    private lateinit var lexer: Lexer

    @BeforeEach
    fun setUp() {
        tokenMapper = mock(TokenMapper::class.java)
        parser = mock(Parser::class.java)
        linter = mock(Linter::class.java)
        formatter = mock(FormatterPS::class.java)
        formatterService = mock(FormatterRulesService::class.java)
        linterRulesService = mock(LinterRulesService::class.java)
        lexer = mock(Lexer::class.java)

        printScriptService = PrintScriptService(
            tokenMapper,
            parser,
            linter,
            formatter,
            formatterService,
            linterRulesService,
            "localhost",
        )
    }
}
