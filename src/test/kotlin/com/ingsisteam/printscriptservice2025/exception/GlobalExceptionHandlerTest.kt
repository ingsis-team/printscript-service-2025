package com.ingsisteam.printscriptservice2025.exception

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// Dummy DTO for testing MethodArgumentNotValidException
data class TestRequestDTO(
    @field:NotBlank(message = "Name cannot be blank")
    val name: String,
    @field:Min(value = 1, message = "Age must be at least 1")
    val age: Int,
)

// Dummy Controller to trigger exceptions
@RestController
@RequestMapping("/test-exceptions")
class DummyExceptionController {

    @GetMapping("/resource-not-found")
    fun throwResourceNotFound(): String {
        throw ResourceNotFoundException("Test Resource Not Found")
    }

    @GetMapping("/validation-error")
    fun throwValidationException(): String {
        throw ValidationException("Test Validation Error")
    }

    @GetMapping("/formatting-error")
    fun throwFormattingException(): String {
        throw FormattingException("Test Formatting Error")
    }

    @GetMapping("/linting-error")
    fun throwLintingException(): String {
        throw LintingException("Test Linting Error")
    }

    @GetMapping("/external-service-error")
    fun throwExternalServiceException(): String {
        throw ExternalServiceException("Test External Service Error")
    }

    @GetMapping("/database-error")
    fun throwDatabaseException(): String {
        throw DatabaseException("Test Database Error")
    }

    @GetMapping("/redis-error")
    fun throwRedisException(): String {
        throw RedisException("Test Redis Error")
    }

    @GetMapping("/unsupported-language-error")
    fun throwUnsupportedLanguageException(): String {
        throw UnsupportedLanguageException("Test Unsupported Language Error")
    }

    @GetMapping("/script-execution-error")
    fun throwScriptExecutionException(): String {
        throw ScriptExecutionException("Test Script Execution Error")
    }

    @GetMapping("/parsing-error")
    fun throwParsingException(): String {
        throw ParsingException("Test Parsing Error")
    }

    @GetMapping("/type-mismatch/{id}")
    fun throwTypeMismatch(@PathVariable id: Int): String {
        // This will throw MethodArgumentTypeMismatchException if 'id' is not convertible to expected type
        return id.toString()
    }

    @GetMapping("/illegal-argument")
    fun throwIllegalArgument(): String {
        throw IllegalArgumentException("Test Illegal Argument")
    }

    @GetMapping("/generic-error")
    fun throwGenericError(): String {
        throw RuntimeException("Test Generic Error")
    }

    @GetMapping("/actuator/info") // Simulate an actuator endpoint
    fun throwErrorOnActuatorPath(): String {
        throw RuntimeException("Error on Actuator path")
    }

    @PostMapping("/validation-dto")
    fun validateDTO(@Valid @RequestBody dto: TestRequestDTO): String {
        return "DTO Validated"
    }
}

@ExtendWith(SpringExtension::class)
@WebMvcTest // This loads a slice of the Spring context for web layers
@ContextConfiguration(classes = [GlobalExceptionHandler::class, DummyExceptionController::class])
class GlobalExceptionHandlerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `handleResourceNotFoundException returns 404`() {
        mockMvc.perform(get("/test-exceptions/resource-not-found").header("Correlation-id", "test-123"))
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Resource Not Found"))
            .andExpect(jsonPath("$.message").value("Test Resource Not Found"))
            .andExpect(jsonPath("$.path").value("/test-exceptions/resource-not-found"))
            .andExpect(jsonPath("$.correlationId").value("test-123"))
    }

    @Test
    fun `handleValidationException returns 400`() {
        mockMvc.perform(get("/test-exceptions/validation-error").header("Correlation-id", "test-456"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Error"))
            .andExpect(jsonPath("$.message").value("Test Validation Error"))
            .andExpect(jsonPath("$.path").value("/test-exceptions/validation-error"))
            .andExpect(jsonPath("$.correlationId").value("test-456"))
    }

    @Test
    fun `handleFormattingException returns 422`() {
        mockMvc.perform(get("/test-exceptions/formatting-error").header("Correlation-id", "test-789"))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.error").value("Formatting Error"))
            .andExpect(jsonPath("$.message").value("Test Formatting Error"))
            .andExpect(jsonPath("$.path").value("/test-exceptions/formatting-error"))
            .andExpect(jsonPath("$.correlationId").value("test-789"))
    }

    @Test
    fun `handleLintingException returns 422`() {
        mockMvc.perform(get("/test-exceptions/linting-error").header("Correlation-id", "test-012"))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.error").value("Linting Error"))
            .andExpect(jsonPath("$.message").value("Test Linting Error"))
            .andExpect(jsonPath("$.path").value("/test-exceptions/linting-error"))
            .andExpect(jsonPath("$.correlationId").value("test-012"))
    }

    @Test
    fun `handleExternalServiceException returns 502`() {
        mockMvc.perform(get("/test-exceptions/external-service-error").header("Correlation-id", "test-345"))
            .andExpect(status().isBadGateway)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(502))
            .andExpect(jsonPath("$.error").value("External Service Error"))
            .andExpect(jsonPath("$.message").value("Test External Service Error"))
            .andExpect(jsonPath("$.path").value("/test-exceptions/external-service-error"))
            .andExpect(jsonPath("$.correlationId").value("test-345"))
    }

    @Test
    fun `handleDatabaseException returns 500`() {
        mockMvc.perform(get("/test-exceptions/database-error").header("Correlation-id", "test-678"))
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Database Error"))
            .andExpect(jsonPath("$.message").value("An error occurred while accessing the database"))
            .andExpect(jsonPath("$.path").value("/test-exceptions/database-error"))
            .andExpect(jsonPath("$.correlationId").value("test-678"))
    }

    @Test
    fun `handleRedisException returns 500`() {
        mockMvc.perform(get("/test-exceptions/redis-error").header("Correlation-id", "test-901"))
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Redis Error"))
            .andExpect(jsonPath("$.message").value("An error occurred while accessing Redis"))
            .andExpect(jsonPath("$.path").value("/test-exceptions/redis-error"))
            .andExpect(jsonPath("$.correlationId").value("test-901"))
    }

    @Test
    fun `handleUnsupportedLanguageException returns 400`() {
        mockMvc.perform(get("/test-exceptions/unsupported-language-error").header("Correlation-id", "test-234"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Unsupported Language"))
            .andExpect(jsonPath("$.message").value("Test Unsupported Language Error"))
            .andExpect(jsonPath("$.path").value("/test-exceptions/unsupported-language-error"))
            .andExpect(jsonPath("$.correlationId").value("test-234"))
    }

    @Test
    fun `handleScriptExecutionException returns 422`() {
        mockMvc.perform(get("/test-exceptions/script-execution-error").header("Correlation-id", "test-567"))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.error").value("Script Execution Error"))
            .andExpect(jsonPath("$.message").value("Test Script Execution Error"))
            .andExpect(jsonPath("$.path").value("/test-exceptions/script-execution-error"))
            .andExpect(jsonPath("$.correlationId").value("test-567"))
    }

    @Test
    fun `handleParsingException returns 422`() {
        mockMvc.perform(get("/test-exceptions/parsing-error").header("Correlation-id", "test-890"))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.error").value("Parsing Error"))
            .andExpect(jsonPath("$.message").value("Test Parsing Error"))
            .andExpect(jsonPath("$.path").value("/test-exceptions/parsing-error"))
            .andExpect(jsonPath("$.correlationId").value("test-890"))
    }

    @Test
    fun `handleIllegalArgumentException returns 400`() {
        mockMvc.perform(get("/test-exceptions/illegal-argument").header("Correlation-id", "arg-333"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Invalid Argument"))
            .andExpect(jsonPath("$.message").value("Test Illegal Argument"))
            .andExpect(jsonPath("$.path").value("/test-exceptions/illegal-argument"))
            .andExpect(jsonPath("$.correlationId").value("arg-333"))
    }

    @Test
    fun `handleMethodArgumentTypeMismatchException returns 400 for invalid parameter type`() {
        mockMvc.perform(get("/test-exceptions/type-mismatch/{id}", "not-an-int").header("Correlation-id", "type-222"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Type Mismatch"))
            .andExpect(jsonPath("$.message").value("Invalid parameter type: id"))
            .andExpect(jsonPath("$.path").value("/test-exceptions/type-mismatch/not-an-int"))
            .andExpect(jsonPath("$.correlationId").value("type-222"))
    }

    @Test
    fun `handleGenericException returns 500 for non-actuator paths`() {
        mockMvc.perform(get("/test-exceptions/generic-error").header("Correlation-id", "gen-444"))
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."))
            .andExpect(jsonPath("$.path").value("/test-exceptions/generic-error"))
            .andExpect(jsonPath("$.correlationId").value("gen-444"))
    }
}
