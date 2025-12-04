package com.ingsisteam.printscriptservice2025.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ExceptionHandlingTest {

    // Tests for CustomExceptions.kt
    @Test
    fun `PrintScriptServiceException properties should be propagated by subclass`() {
        val cause = RuntimeException("Root cause")
        val exception = ValidationException("Test message", cause) // Test a concrete subclass
        assertEquals("Test message", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `ResourceNotFoundException should be instantiable`() {
        val ex = ResourceNotFoundException("Resource not found.")
        assertEquals("Resource not found.", ex.message)
    }

    @Test
    fun `ValidationException should be instantiable`() {
        val ex = ValidationException("Invalid input.")
        assertEquals("Invalid input.", ex.message)
    }

    @Test
    fun `FormattingException should be instantiable`() {
        val ex = FormattingException("Formatting failed.")
        assertEquals("Formatting failed.", ex.message)
    }

    @Test
    fun `LintingException should be instantiable`() {
        val ex = LintingException("Linting failed.")
        assertEquals("Linting failed.", ex.message)
    }

    @Test
    fun `ExternalServiceException should be instantiable`() {
        val ex = ExternalServiceException("External service unavailable.")
        assertEquals("External service unavailable.", ex.message)
    }

    @Test
    fun `DatabaseException should be instantiable`() {
        val ex = DatabaseException("DB connection lost.")
        assertEquals("DB connection lost.", ex.message)
    }

    @Test
    fun `RedisException should be instantiable`() {
        val ex = RedisException("Redis connection error.")
        assertEquals("Redis connection error.", ex.message)
    }

    @Test
    fun `UnsupportedLanguageException should be instantiable`() {
        val ex = UnsupportedLanguageException("Language not supported.")
        assertEquals("Language not supported.", ex.message)
    }

    @Test
    fun `ScriptExecutionException should be instantiable`() {
        val ex = ScriptExecutionException("Script execution failed.")
        assertEquals("Script execution failed.", ex.message)
    }

    @Test
    fun `ParsingException should be instantiable`() {
        val ex = ParsingException("Parsing failed.")
        assertEquals("Parsing failed.", ex.message)
    }

    // Tests for ErrorResponse.kt
    @Test
    fun `ErrorResponse should be correctly initialized`() {
        val now = LocalDateTime.now()
        val errorResponse = ErrorResponse(
            timestamp = now,
            status = 400,
            error = "Bad Request",
            message = "Invalid parameters",
            path = "/api/test",
            correlationId = "abc-123",
        )
        assertEquals(now, errorResponse.timestamp)
        assertEquals(400, errorResponse.status)
        assertEquals("Bad Request", errorResponse.error)
        assertEquals("Invalid parameters", errorResponse.message)
        assertEquals("/api/test", errorResponse.path)
        assertEquals("abc-123", errorResponse.correlationId)
    }

    @Test
    fun `ErrorResponse should generate timestamp by default`() {
        val errorResponse = ErrorResponse(
            status = 500,
            error = "Internal Server Error",
            message = "Something went wrong",
        )
        assertNotNull(errorResponse.timestamp)
        assertEquals(500, errorResponse.status)
    }

    @Test
    fun `ValidationErrorResponse should be correctly initialized`() {
        val now = LocalDateTime.now()
        val validationErrors = listOf(ValidationError("field1", "message1"))
        val errorResponse = ValidationErrorResponse(
            timestamp = now,
            status = 400,
            error = "Validation Failed",
            message = "Input errors",
            path = "/api/validate",
            correlationId = "def-456",
            validationErrors = validationErrors,
        )
        assertEquals(now, errorResponse.timestamp)
        assertEquals(400, errorResponse.status)
        assertEquals("Validation Failed", errorResponse.error)
        assertEquals("Input errors", errorResponse.message)
        assertEquals("/api/validate", errorResponse.path)
        assertEquals("def-456", errorResponse.correlationId)
        assertEquals(validationErrors, errorResponse.validationErrors)
    }

    @Test
    fun `ValidationError should be correctly initialized`() {
        val validationError = ValidationError(
            field = "name",
            message = "Name cannot be empty",
            rejectedValue = "",
        )
        assertEquals("name", validationError.field)
        assertEquals("Name cannot be empty", validationError.message)
        assertEquals("", validationError.rejectedValue)
    }
}
