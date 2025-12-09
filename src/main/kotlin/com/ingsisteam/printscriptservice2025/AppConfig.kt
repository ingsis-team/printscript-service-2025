package com.ingsisteam.printscriptservice2025

import formatOperations.FormattingOperation
import formatter.FormatterPS
import lexer.Lexer
import lexer.TokenMapper
import linter.Linter
import linter.LinterVersion
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import parser.Parser
import rules.RulesReader

@Configuration
class AppConfig {
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

    @Bean
    fun formatter(): FormatterPS {
        val allowedDeclarationKeywords = listOf("let", "const")
        val allowedDataTypes = listOf("string", "number", "boolean")
        val tokenMapper = TokenMapper("1.1")
        val parser = Parser()
        val lexer = Lexer(tokenMapper)
        val formattingOperations =
            listOf<FormattingOperation>(
                formatOperations.BlockFormatter(),
                formatOperations.PrintFormatter(),
                formatOperations.BinaryFormatter(),
                formatOperations.LiteralFormatter(),
                formatOperations.AssignationFormatter(),
                formatOperations.ConditionalFormatter(),
                formatOperations.FunctionFormatter(),
                formatOperations.DeclarationFormatter(allowedDeclarationKeywords, allowedDataTypes),
            )
        val rulesReader =
            RulesReader(
                mapOf(
                    "spaceBeforeColon" to Boolean::class,
                    "spaceAfterColon" to Boolean::class,
                    "spaceAroundEquals" to Boolean::class,
                    "lineBreakPrintln" to Int::class,
                ),
            )
        val rulesPath =
            this::class.java.classLoader.getResource("files/StandardRules.json")?.path
                ?: throw IllegalArgumentException("Rules file not found")

        return FormatterPS(rulesReader, rulesPath, formattingOperations, lexer, parser)
    }

    @Bean
    fun tokenMapper(): TokenMapper {
        val tokenVersion = "1.1"
        return TokenMapper(tokenVersion)
    }

    @Bean
    fun parser(): Parser {
        return Parser()
    }

    @Bean
    fun linter(): Linter {
        val version = LinterVersion.VERSION_1_1
        return Linter(version)
    }
}
