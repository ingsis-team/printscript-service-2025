package com.ingsis_team.printscript_service_2025.exception

import java.time.LocalDateTime

/**
 * Estructura de respuesta para errores
 */
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null,
    val correlationId: String? = null
)

/**
 * Respuesta de error para validaciones con detalles adicionales
 */
data class ValidationErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null,
    val correlationId: String? = null,
    val validationErrors: List<ValidationError>? = null
)

/**
 * Detalle de error de validación
 */
data class ValidationError(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null
)

