package com.ingsisteam.printscriptservice2025

import formatOperations.AssignationFormatter
import formatOperations.BinaryFormatter
import formatOperations.BlockFormatter
import formatOperations.ConditionalFormatter
import formatOperations.DeclarationFormatter
import formatOperations.FunctionFormatter
import formatOperations.LiteralFormatter
import formatOperations.PrintFormatter
import formatter.FormatterPS
import lexer.Lexer
import lexer.TokenMapper
import linter.Linter
import linter.LinterVersion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.client.RestTemplate
import parser.Parser
import rules.RulesReader
import java.io.File // Needed for File().exists() check

@SpringBootTest(
    classes = [AppConfig::class, TestApplication::class],
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.open-in-view=false",
    ],
)
class AppConfigTest {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Autowired
    private lateinit var formatterPS: FormatterPS

    @Autowired
    private lateinit var tokenMapper: TokenMapper

    @Autowired
    private lateinit var parser: Parser

    @Autowired
    private lateinit var linter: Linter

    @Test
    fun `restTemplate bean should be created`() {
        assertNotNull(restTemplate, "RestTemplate bean should not be null")
    }

    @Test
    fun `formatter bean should be created with correct dependencies`() {
        assertNotNull(formatterPS, "FormatterPS bean should not be null")

        val rulesReaderField = FormatterPS::class.java.getDeclaredField("rulesReader")
        rulesReaderField.isAccessible = true
        val rulesReader = rulesReaderField.get(formatterPS) as RulesReader
        assertNotNull(rulesReader)

        val rulesPathField = FormatterPS::class.java.getDeclaredField("rulesPath")
        rulesPathField.isAccessible = true
        val rulesPath = rulesPathField.get(formatterPS) as String
        assertNotNull(rulesPath)
        assertTrue(rulesPath.contains("files/StandardRules.json")) { "Rules path should contain StandardRules.json" }
        assertTrue(File(rulesPath).exists()) { "Rules file should exist at the specified path" }

        val formattingOperationsField = FormatterPS::class.java.getDeclaredField("formattingOperations")
        formattingOperationsField.isAccessible = true
        val formattingOperations = formattingOperationsField.get(formatterPS) as List<*>
        assertEquals(8, formattingOperations.size)
        assertTrue(formattingOperations.any { it is BlockFormatter })
        assertTrue(formattingOperations.any { it is PrintFormatter })
        assertTrue(formattingOperations.any { it is BinaryFormatter })
        assertTrue(formattingOperations.any { it is LiteralFormatter })
        assertTrue(formattingOperations.any { it is AssignationFormatter })
        assertTrue(formattingOperations.any { it is ConditionalFormatter })
        assertTrue(formattingOperations.any { it is FunctionFormatter })
        assertTrue(formattingOperations.any { it is DeclarationFormatter })

        val lexerField = FormatterPS::class.java.getDeclaredField("lexer")
        lexerField.isAccessible = true
        val lexer = lexerField.get(formatterPS) as Lexer
        assertNotNull(lexer)

        val parserField = FormatterPS::class.java.getDeclaredField("parser")
        parserField.isAccessible = true
        val parser = parserField.get(formatterPS) as Parser
        assertNotNull(parser)
    }

    @Test
    fun `tokenMapper bean should be created with correct version`() {
        assertNotNull(tokenMapper, "TokenMapper bean should not be null")
        val versionField = TokenMapper::class.java.getDeclaredField("version")
        versionField.isAccessible = true
        val version = versionField.get(tokenMapper) as String
        assertEquals("1.1", version)
    }

    @Test
    fun `parser bean should be created`() {
        assertNotNull(parser, "Parser bean should not be null")
    }

    @Test
    fun `linter bean should be created with correct version`() {
        assertNotNull(linter, "Linter bean should not be null")
        val versionField = Linter::class.java.getDeclaredField("version")
        versionField.isAccessible = true
        val version = versionField.get(linter) as LinterVersion
        assertEquals(LinterVersion.VERSION_1_1, version)
    }
}
