package com.ingsisteam.printscriptservice2025.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.LocalDateTime

/**
 * Manejador global de excepciones para la aplicación
 * Captura todas las excepciones y devuelve respuestas HTTP apropiadas
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Maneja ResourceNotFoundException - 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        ex: ResourceNotFoundException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Resource not found: ${ex.message}", ex)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Resource Not Found",
            message = ex.message ?: "The requested resource was not found",
            path = extractPath(request),
            correlationId = extractCorrelationId(request),
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    /**
     * Maneja ValidationException - 400 Bad Request
     */
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(
        ex: ValidationException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Validation error: ${ex.message}", ex)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Error",
            message = ex.message ?: "Validation failed",
            path = extractPath(request),
            correlationId = extractCorrelationId(request),
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Maneja FormattingException - 422 Unprocessable Entity
     */
    @ExceptionHandler(FormattingException::class)
    fun handleFormattingException(
        ex: FormattingException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Formatting error: ${ex.message}", ex)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            error = "Formatting Error",
            message = ex.message ?: "Failed to format code",
            path = extractPath(request),
            correlationId = extractCorrelationId(request),
        )

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse)
    }

    /**
     * Maneja LintingException - 422 Unprocessable Entity
     */
    @ExceptionHandler(LintingException::class)
    fun handleLintingException(
        ex: LintingException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Linting error: ${ex.message}", ex)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            error = "Linting Error",
            message = ex.message ?: "Failed to lint code",
            path = extractPath(request),
            correlationId = extractCorrelationId(request),
        )

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse)
    }

    /**
     * Maneja ExternalServiceException - 502 Bad Gateway
     */
    @ExceptionHandler(ExternalServiceException::class)
    fun handleExternalServiceException(
        ex: ExternalServiceException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error("External service error: ${ex.message}", ex)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_GATEWAY.value(),
            error = "External Service Error",
            message = ex.message ?: "Failed to communicate with external service",
            path = extractPath(request),
            correlationId = extractCorrelationId(request),
        )

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse)
    }

    /**
     * Maneja DatabaseException - 500 Internal Server Error
     */
    @ExceptionHandler(DatabaseException::class)
    fun handleDatabaseException(
        ex: DatabaseException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Database error: ${ex.message}", ex)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Database Error",
            message = "An error occurred while accessing the database",
            path = extractPath(request),
            correlationId = extractCorrelationId(request),
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    /**
     * Maneja RedisException - 500 Internal Server Error
     */
    @ExceptionHandler(RedisException::class)
    fun handleRedisException(
        ex: RedisException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Redis error: ${ex.message}", ex)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Redis Error",
            message = "An error occurred while accessing Redis",
            path = extractPath(request),
            correlationId = extractCorrelationId(request),
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    /**
     * Maneja UnsupportedLanguageException - 400 Bad Request
     */
    @ExceptionHandler(UnsupportedLanguageException::class)
    fun handleUnsupportedLanguageException(
        ex: UnsupportedLanguageException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Unsupported language: ${ex.message}", ex)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Unsupported Language",
            message = ex.message ?: "The specified language is not supported",
            path = extractPath(request),
            correlationId = extractCorrelationId(request),
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Maneja ScriptExecutionException - 422 Unprocessable Entity
     */
    @ExceptionHandler(ScriptExecutionException::class)
    fun handleScriptExecutionException(
        ex: ScriptExecutionException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Script execution error: ${ex.message}", ex)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            error = "Script Execution Error",
            message = ex.message ?: "Failed to execute script",
            path = extractPath(request),
            correlationId = extractCorrelationId(request),
        )

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse)
    }

    /**
     * Maneja ParsingException - 422 Unprocessable Entity
     */
    @ExceptionHandler(ParsingException::class)
    fun handleParsingException(
        ex: ParsingException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Parsing error: ${ex.message}", ex)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            error = "Parsing Error",
            message = ex.message ?: "Failed to parse code",
            path = extractPath(request),
            correlationId = extractCorrelationId(request),
        )

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse)
    }

    /**
     * Maneja MethodArgumentNotValidException - 400 Bad Request
     * Para validaciones de @Valid en los DTOs
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: WebRequest,
    ): ResponseEntity<ValidationErrorResponse> {
        logger.warn("Validation error: ${ex.message}")

        val validationErrors = ex.bindingResult.fieldErrors.map { fieldError ->
            ValidationError(
                field = fieldError.field,
                message = fieldError.defaultMessage ?: "Invalid value",
                rejectedValue = fieldError.rejectedValue,
            )
        }

        val errorResponse = ValidationErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "Request validation failed",
            path = extractPath(request),
            correlationId = extractCorrelationId(request),
            validationErrors = validationErrors,
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Maneja MethodArgumentTypeMismatchException - 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(
        ex: MethodArgumentTypeMismatchException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Type mismatch error: ${ex.message}")

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Type Mismatch",
            message = "Invalid parameter type: ${ex.name}",
            path = extractPath(request),
            correlationId = extractCorrelationId(request),
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Maneja IllegalArgumentException - 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Illegal argument: ${ex.message}", ex)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Invalid Argument",
            message = ex.message ?: "Invalid argument provided",
            path = extractPath(request),
            correlationId = extractCorrelationId(request),
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    /**
     * Maneja todas las demás excepciones no capturadas - 500 Internal Server Error
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        val path = extractPath(request)

        // Ignorar rutas de actuator - dejar que Spring las maneje
        if (path.contains("/actuator/")) {
            throw ex
        }

        logger.error("Unexpected error: ${ex.message}", ex)

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred. Please try again later.",
            path = path,
            correlationId = extractCorrelationId(request),
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    /**
     * Extrae la ruta de la petición
     */
    private fun extractPath(request: WebRequest): String {
        return request.getDescription(false).replace("uri=", "")
    }

    /**
     * Extrae el correlation ID del header de la petición
     */
    private fun extractCorrelationId(request: WebRequest): String? {
        return request.getHeader("Correlation-id")
    }
}
