package com.ingsisteam.printscriptservice2025.exception

/**
 * Excepción base para todas las excepciones personalizadas del servicio
 */
sealed class PrintScriptServiceException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * Excepción cuando no se encuentra un recurso (404)
 */
class ResourceNotFoundException(
    message: String,
    cause: Throwable? = null,
) : PrintScriptServiceException(message, cause)

/**
 * Excepción cuando hay un error de validación (400)
 */
class ValidationException(
    message: String,
    cause: Throwable? = null,
) : PrintScriptServiceException(message, cause)

/**
 * Excepción cuando hay un error en el formato (422)
 */
class FormattingException(
    message: String,
    cause: Throwable? = null,
) : PrintScriptServiceException(message, cause)

/**
 * Excepción cuando hay un error en el linter (422)
 */
class LintingException(
    message: String,
    cause: Throwable? = null,
) : PrintScriptServiceException(message, cause)

/**
 * Excepción cuando hay un error de comunicación con servicios externos (502)
 */
class ExternalServiceException(
    message: String,
    cause: Throwable? = null,
) : PrintScriptServiceException(message, cause)

/**
 * Excepción cuando hay un error en la base de datos (500)
 */
class DatabaseException(
    message: String,
    cause: Throwable? = null,
) : PrintScriptServiceException(message, cause)

/**
 * Excepción cuando hay un error en Redis (500)
 */
class RedisException(
    message: String,
    cause: Throwable? = null,
) : PrintScriptServiceException(message, cause)

/**
 * Excepción cuando el lenguaje no está soportado (400)
 */
class UnsupportedLanguageException(
    message: String,
    cause: Throwable? = null,
) : PrintScriptServiceException(message, cause)

/**
 * Excepción cuando hay un error en la ejecución del script (422)
 */
class ScriptExecutionException(
    message: String,
    cause: Throwable? = null,
) : PrintScriptServiceException(message, cause)

/**
 * Excepción cuando hay un error en el parseo (422)
 */
class ParsingException(
    message: String,
    cause: Throwable? = null,
) : PrintScriptServiceException(message, cause)
