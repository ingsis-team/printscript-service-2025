package com.ingsis_team.printscript_service_2025

import org.junit.jupiter.api.Test
import com.ingsis_team.printscript_service_2025.redis.dto.ChangeRulesDTO
import com.ingsis_team.printscript_service_2025.redis.dto.ExecutionDataDTO
import com.ingsis_team.printscript_service_2025.redis.dto.Rule
import com.ingsis_team.printscript_service_2025.redis.dto.Snippet
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RedisDtoTest {
    @Test
    fun `test ChangeRulesDTO properties`() {
        val userId = "user123"
        val rules = listOf(Rule("Rule1", "value1"))
        val snippets =
            listOf(
                ExecutionDataDTO(
                    UUID.randomUUID(),
                    "snippet1",
                    "Kotlin",
                    "1.0",
                    "println('Hello')",
                ),
            )
        val correlationId = UUID.randomUUID()

        val dto = ChangeRulesDTO(userId, rules, snippets, correlationId)

        assertEquals(userId, dto.userId)
        assertEquals(rules, dto.rules)
        assertEquals(snippets, dto.snippets)
        assertEquals(correlationId, dto.correlationId)
    }

    @Test
    fun `test ExecutionDataDTO equality`() {
        val correlationId = UUID.randomUUID()
        val snippetId = "snippet123"
        val dto1 = ExecutionDataDTO(correlationId, snippetId, "Kotlin", "1.0", "input data")
        val dto2 = ExecutionDataDTO(correlationId, snippetId, "Kotlin", "1.0", "input data")

        assertEquals(dto1, dto2)
        assertEquals(dto1.hashCode(), dto2.hashCode())
    }

    @Test
    fun `test Rule properties`() {
        val name = "Sample Rule"
        val value = "RuleValue"

        val rule = Rule(name, value)

        assertEquals(name, rule.name)
        assertEquals(value, rule.value)
    }

    @Test
    fun `test Snippet creation and toString`() {
        val userId = "user123"
        val snippetId = "snippet456"
        val content = "fun main() { println('Hello World') }"
        val correlationID = UUID.randomUUID()

        val snippet = Snippet(userId, snippetId, content, correlationID)

        assertEquals(userId, snippet.userId)
        assertEquals(snippetId, snippet.id)
        assertEquals(content, snippet.content)
        assertEquals(correlationID, snippet.correlationID)

        // Test toString output (optional)
        val toStringOutput = snippet.toString()
        assertTrue(toStringOutput.contains(userId))
        assertTrue(toStringOutput.contains(snippetId))
        assertTrue(toStringOutput.contains(content))
        assertTrue(toStringOutput.contains(correlationID.toString()))
    }

    @Test
    fun `test different instances are not equal`() {
        val rule1 = Rule("Rule1", "value1")
        val rule2 = Rule("Rule2", "value2")
        assertNotEquals(rule1, rule2)

        val snippet1 = Snippet("user1", "id1", "content1", UUID.randomUUID())
        val snippet2 = Snippet("user2", "id2", "content2", UUID.randomUUID())
        assertNotEquals(snippet1, snippet2)
    }
}
