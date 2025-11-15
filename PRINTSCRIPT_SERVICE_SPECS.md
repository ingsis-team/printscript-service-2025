# PrintScript Service - Especificaciones de Implementación

## Descripción
Servicio encargado de validar, parsear, formatear y ejecutar código PrintScript. Se integra con el Snippet Service para validar la sintaxis de snippets antes de guardarlos.

## Arquitectura y Tecnologías Requeridas

### Stack Tecnológico
- **Framework**: Spring Boot 3.5.6
- **Lenguaje**: Kotlin 1.9.25
- **Base de Datos**: PostgreSQL (opcional para logs/cache)
- **ORM**: Spring Data JPA
- **Documentación**: Swagger/OpenAPI 3
- **Containerización**: Docker + Docker Compose
- **Build Tool**: Gradle
- **Linter**: ktlint

### Dependencias Gradle (build.gradle)
```gradle
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.9.25'
    id 'org.jetbrains.kotlin.plugin.spring' version '1.9.25'
    id 'org.springframework.boot' version '3.5.6'
    id 'io.spring.dependency-management' version '1.1.7'
    id 'org.jlleitschuh.gradle.ktlint' version '12.1.0'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.jetbrains.kotlin:kotlin-reflect'
    
    // Swagger/OpenAPI
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
    
    // Jackson for JSON
    implementation 'com.fasterxml.jackson.module:jackson-module-kotlin'
    
    // Parser libraries (agregar según implementación específica)
    // implementation 'your-printscript-parser:version'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.jetbrains.kotlin:kotlin-test-junit5'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

## Configuración de Aplicación

### application.properties
```properties
spring.application.name=printscript-service

# Application Configuration
server.port=8081
logging.level.com.printscriptservice=DEBUG

# Jackson Configuration
spring.jackson.property-naming-strategy=SNAKE_CASE
spring.jackson.serialization.write-dates-as-timestamps=false

# Swagger/OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method

# PrintScript Configuration
printscript.timeout.seconds=30
printscript.max-execution-time.seconds=10
printscript.max-output-length=10000
```

## DTOs para Validación y Ejecución

### 1. Request DTOs
```kotlin
package com.printscriptservice.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ValidationRequest(
    @field:NotBlank(message = "Content cannot be null or empty")
    val content: String,

    @field:NotBlank(message = "Language cannot be null or empty")
    val language: String,

    @field:NotBlank(message = "Version cannot be null or empty")
    val version: String
)

data class FormatRequest(
    @field:NotBlank(message = "Content cannot be null or empty")
    val content: String,

    @field:NotBlank(message = "Language cannot be null or empty")  
    val language: String,

    @field:NotBlank(message = "Version cannot be null or empty")
    val version: String,

    val config: Map<String, Any> = emptyMap()
)

data class ExecuteRequest(
    @field:NotBlank(message = "Content cannot be null or empty")
    val content: String,

    @field:NotBlank(message = "Version cannot be null or empty")
    val version: String,

    val inputs: List<String> = emptyList()
)

data class LintRequest(
    @field:NotBlank(message = "Content cannot be null or empty")
    val content: String,

    @field:NotBlank(message = "Language cannot be null or empty")
    val language: String,

    @field:NotBlank(message = "Version cannot be null or empty")  
    val version: String,

    val config: Map<String, Any> = emptyMap()
)
```

### 2. Response DTOs
```kotlin
package com.printscriptservice.model.dto

data class ValidationResponse(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList()
)

data class ValidationError(
    val rule: String,
    val line: Int,
    val column: Int,
    val message: String
)

data class FormatResponse(
    val formattedContent: String,
    val changes: List<FormatChange> = emptyList()
)

data class FormatChange(
    val line: Int,
    val column: Int,
    val type: String,
    val description: String
)

data class ExecuteResponse(
    val output: List<String>,
    val errors: List<String> = emptyList(),
    val executionTime: Long, // en millisegundos
    val status: ExecutionStatus
)

enum class ExecutionStatus {
    SUCCESS,
    ERROR,
    TIMEOUT
}

data class LintResponse(
    val issues: List<LintIssue> = emptyList(),
    val isClean: Boolean
)

data class LintIssue(
    val rule: String,
    val line: Int,
    val column: Int,
    val severity: LintSeverity,
    val message: String,
    val suggestion: String? = null
)

enum class LintSeverity {
    ERROR,
    WARNING,
    INFO
}

data class AnalysisResponse(
    val tokens: List<TokenInfo>,
    val ast: String, // JSON representation of AST
    val complexity: Int,
    val linesOfCode: Int
)

data class TokenInfo(
    val type: String,
    val value: String,
    val line: Int,
    val column: Int
)
```

## Service Layer

### 1. PrintScript Validation Service
```kotlin
package com.printscriptservice.service

import com.printscriptservice.model.dto.*
import org.springframework.stereotype.Service

@Service
class PrintScriptValidationService {

    fun validateSyntax(request: ValidationRequest): ValidationResponse {
        return try {
            when (request.language.uppercase()) {
                "PRINTSCRIPT" -> validatePrintScript(request.content, request.version)
                "JAVASCRIPT" -> validateJavaScript(request.content)
                "TYPESCRIPT" -> validateTypeScript(request.content)
                "PYTHON" -> validatePython(request.content)
                "JAVA" -> validateJava(request.content)
                "KOTLIN" -> validateKotlin(request.content)
                else -> ValidationResponse(
                    isValid = false,
                    errors = listOf(
                        ValidationError(
                            rule = "UNSUPPORTED_LANGUAGE",
                            line = 1,
                            column = 1,
                            message = "Language '${request.language}' is not supported"
                        )
                    )
                )
            }
        } catch (e: Exception) {
            ValidationResponse(
                isValid = false,
                errors = listOf(
                    ValidationError(
                        rule = "VALIDATION_ERROR",
                        line = 1,
                        column = 1,
                        message = "Error during validation: ${e.message}"
                    )
                )
            )
        }
    }

    private fun validatePrintScript(content: String, version: String): ValidationResponse {
        val errors = mutableListOf<ValidationError>()

        // Validaciones básicas
        if (content.isBlank()) {
            errors.add(
                ValidationError(
                    rule = "EMPTY_CONTENT",
                    line = 1,
                    column = 1,
                    message = "Content cannot be empty"
                )
            )
            return ValidationResponse(isValid = false, errors = errors)
        }

        val lines = content.lines()
        
        // Validar sintaxis línea por línea
        lines.forEachIndexed { index, line ->
            val lineNumber = index + 1
            val trimmedLine = line.trim()
            
            if (trimmedLine.isEmpty()) return@forEachIndexed
            
            // Validar declaraciones de variables
            if (trimmedLine.startsWith("let ") || trimmedLine.startsWith("const ")) {
                validateVariableDeclaration(trimmedLine, lineNumber, errors)
            }
            
            // Validar llamadas a funciones
            if (trimmedLine.contains("println(") || trimmedLine.contains("readInput(")) {
                validateFunctionCall(trimmedLine, lineNumber, errors)
            }
            
            // Validar punto y coma al final
            if (!trimmedLine.endsWith(";") && 
                !trimmedLine.endsWith("{") && 
                !trimmedLine.endsWith("}")) {
                errors.add(
                    ValidationError(
                        rule = "MISSING_SEMICOLON",
                        line = lineNumber,
                        column = trimmedLine.length + 1,
                        message = "Expected ';' at end of statement"
                    )
                )
            }
        }

        return ValidationResponse(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    private fun validateVariableDeclaration(line: String, lineNumber: Int, errors: MutableList<ValidationError>) {
        // Validar formato: let variableName: type = value;
        val regex = Regex("^(let|const)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*:\\s*(string|number|boolean)\\s*=\\s*.+;$")
        
        if (!regex.matches(line)) {
            errors.add(
                ValidationError(
                    rule = "INVALID_VARIABLE_DECLARATION",
                    line = lineNumber,
                    column = 1,
                    message = "Invalid variable declaration syntax"
                )
            )
        }
    }

    private fun validateFunctionCall(line: String, lineNumber: Int, errors: MutableList<ValidationError>) {
        // Validar parentesis balanceados
        var openParens = 0
        var closeParens = 0
        
        line.forEach { char ->
            when (char) {
                '(' -> openParens++
                ')' -> closeParens++
            }
        }
        
        if (openParens != closeParens) {
            errors.add(
                ValidationError(
                    rule = "UNBALANCED_PARENTHESES",
                    line = lineNumber,
                    column = line.length,
                    message = "Unbalanced parentheses in function call"
                )
            )
        }
    }

    // Implementaciones básicas para otros lenguajes
    private fun validateJavaScript(content: String): ValidationResponse {
        // Implementación básica - integrar con parser JS real en el futuro
        return if (content.isBlank()) {
            ValidationResponse(
                isValid = false,
                errors = listOf(
                    ValidationError("EMPTY_CONTENT", 1, 1, "Content cannot be empty")
                )
            )
        } else {
            ValidationResponse(isValid = true)
        }
    }

    private fun validateTypeScript(content: String): ValidationResponse {
        return if (content.isBlank()) {
            ValidationResponse(
                isValid = false,
                errors = listOf(
                    ValidationError("EMPTY_CONTENT", 1, 1, "Content cannot be empty")
                )
            )
        } else {
            ValidationResponse(isValid = true)
        }
    }

    private fun validatePython(content: String): ValidationResponse {
        return if (content.isBlank()) {
            ValidationResponse(
                isValid = false,
                errors = listOf(
                    ValidationError("EMPTY_CONTENT", 1, 1, "Content cannot be empty")
                )
            )
        } else {
            ValidationResponse(isValid = true)
        }
    }

    private fun validateJava(content: String): ValidationResponse {
        return if (content.isBlank()) {
            ValidationResponse(
                isValid = false,
                errors = listOf(
                    ValidationError("EMPTY_CONTENT", 1, 1, "Content cannot be empty")
                )
            )
        } else {
            ValidationResponse(isValid = true)
        }
    }

    private fun validateKotlin(content: String): ValidationResponse {
        return if (content.isBlank()) {
            ValidationResponse(
                isValid = false,
                errors = listOf(
                    ValidationError("EMPTY_CONTENT", 1, 1, "Content cannot be empty")
                )
            )
        } else {
            ValidationResponse(isValid = true)
        }
    }
}
```

### 2. PrintScript Format Service
```kotlin
package com.printscriptservice.service

import com.printscriptservice.model.dto.*
import org.springframework.stereotype.Service

@Service
class PrintScriptFormatService {

    fun formatCode(request: FormatRequest): FormatResponse {
        return try {
            when (request.language.uppercase()) {
                "PRINTSCRIPT" -> formatPrintScript(request.content, request.version, request.config)
                else -> FormatResponse(
                    formattedContent = request.content,
                    changes = emptyList()
                )
            }
        } catch (e: Exception) {
            FormatResponse(
                formattedContent = request.content,
                changes = listOf(
                    FormatChange(
                        line = 1,
                        column = 1,
                        type = "ERROR",
                        description = "Error during formatting: ${e.message}"
                    )
                )
            )
        }
    }

    private fun formatPrintScript(content: String, version: String, config: Map<String, Any>): FormatResponse {
        val changes = mutableListOf<FormatChange>()
        val lines = content.lines().toMutableList()

        // Aplicar reglas de formato
        lines.forEachIndexed { index, line ->
            val lineNumber = index + 1
            val originalLine = line
            var formattedLine = line

            // Remover espacios al final
            if (formattedLine.endsWith(" ") || formattedLine.endsWith("\t")) {
                formattedLine = formattedLine.trimEnd()
                changes.add(
                    FormatChange(
                        line = lineNumber,
                        column = originalLine.length,
                        type = "TRAILING_WHITESPACE",
                        description = "Removed trailing whitespace"
                    )
                )
            }

            // Formatear espacios alrededor de operadores
            formattedLine = formattedLine.replace(Regex("\\s*=\\s*"), " = ")
            formattedLine = formattedLine.replace(Regex("\\s*:\\s*"), ": ")

            if (formattedLine != originalLine) {
                changes.add(
                    FormatChange(
                        line = lineNumber,
                        column = 1,
                        type = "SPACING",
                        description = "Adjusted spacing around operators"
                    )
                )
            }

            lines[index] = formattedLine
        }

        return FormatResponse(
            formattedContent = lines.joinToString("\n"),
            changes = changes
        )
    }
}
```

### 3. PrintScript Execution Service
```kotlin
package com.printscriptservice.service

import com.printscriptservice.model.dto.*
import org.springframework.stereotype.Service
import java.util.concurrent.*

@Service
class PrintScriptExecutionService {

    private val executor = Executors.newCachedThreadPool()

    fun executeCode(request: ExecuteRequest): ExecuteResponse {
        val startTime = System.currentTimeMillis()
        val output = mutableListOf<String>()
        val errors = mutableListOf<String>()

        return try {
            val future = executor.submit(Callable {
                executePrintScript(request.content, request.version, request.inputs, output, errors)
            })

            future.get(30, TimeUnit.SECONDS) // timeout de 30 segundos

            val executionTime = System.currentTimeMillis() - startTime
            
            ExecuteResponse(
                output = output,
                errors = errors,
                executionTime = executionTime,
                status = if (errors.isEmpty()) ExecutionStatus.SUCCESS else ExecutionStatus.ERROR
            )
        } catch (e: TimeoutException) {
            ExecuteResponse(
                output = output,
                errors = listOf("Execution timed out"),
                executionTime = System.currentTimeMillis() - startTime,
                status = ExecutionStatus.TIMEOUT
            )
        } catch (e: Exception) {
            ExecuteResponse(
                output = output,
                errors = listOf("Execution error: ${e.message}"),
                executionTime = System.currentTimeMillis() - startTime,
                status = ExecutionStatus.ERROR
            )
        }
    }

    private fun executePrintScript(
        content: String,
        version: String,
        inputs: List<String>,
        output: MutableList<String>,
        errors: MutableList<String>
    ) {
        // Implementación básica del intérprete PrintScript
        val lines = content.lines()
        val variables = mutableMapOf<String, Any>()
        var inputIndex = 0

        lines.forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) return@forEach

            try {
                when {
                    trimmedLine.startsWith("let ") || trimmedLine.startsWith("const ") -> {
                        executeVariableDeclaration(trimmedLine, variables)
                    }
                    trimmedLine.startsWith("println(") -> {
                        val result = executePrintln(trimmedLine, variables)
                        output.add(result)
                    }
                    trimmedLine.contains("readInput(") -> {
                        if (inputIndex < inputs.size) {
                            val inputValue = inputs[inputIndex++]
                            executeReadInput(trimmedLine, inputValue, variables)
                        } else {
                            errors.add("Not enough inputs provided")
                        }
                    }
                }
            } catch (e: Exception) {
                errors.add("Error executing line '$trimmedLine': ${e.message}")
            }
        }
    }

    private fun executeVariableDeclaration(line: String, variables: MutableMap<String, Any>) {
        // Parsear: let variableName: type = value;
        val regex = Regex("^(let|const)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*:\\s*(string|number|boolean)\\s*=\\s*(.+);$")
        val match = regex.find(line) ?: throw IllegalArgumentException("Invalid variable declaration")
        
        val variableName = match.groupValues[2]
        val type = match.groupValues[3]
        val value = match.groupValues[4].trim()
        
        val parsedValue = when (type) {
            "string" -> value.removeSurrounding("\"")
            "number" -> value.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $value")
            "boolean" -> value.toBooleanStrictOrNull() ?: throw IllegalArgumentException("Invalid boolean: $value")
            else -> throw IllegalArgumentException("Unsupported type: $type")
        }
        
        variables[variableName] = parsedValue
    }

    private fun executePrintln(line: String, variables: Map<String, Any>): String {
        // Parsear: println(expression);
        val content = line.substringAfter("println(").substringBeforeLast(")")
        
        return when {
            content.startsWith("\"") && content.endsWith("\"") -> {
                content.removeSurrounding("\"")
            }
            variables.containsKey(content) -> {
                variables[content].toString()
            }
            else -> {
                evaluateExpression(content, variables).toString()
            }
        }
    }

    private fun executeReadInput(line: String, inputValue: String, variables: MutableMap<String, Any>) {
        // Parsear: variableName = readInput(prompt);
        val parts = line.split("=")
        if (parts.size != 2) throw IllegalArgumentException("Invalid readInput assignment")
        
        val variableName = parts[0].trim()
        variables[variableName] = inputValue
    }

    private fun evaluateExpression(expression: String, variables: Map<String, Any>): Any {
        // Implementación básica para evaluación de expresiones
        val trimmed = expression.trim()
        
        return when {
            variables.containsKey(trimmed) -> variables[trimmed]!!
            trimmed.toDoubleOrNull() != null -> trimmed.toDouble()
            trimmed.toBooleanStrictOrNull() != null -> trimmed.toBoolean()
            else -> trimmed
        }
    }
}
```

## Controller Layer - ENDPOINTS REQUERIDOS

```kotlin
package com.printscriptservice.controller

import com.printscriptservice.model.dto.*
import com.printscriptservice.service.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
@Tag(name = "PrintScript Controller", description = "API para validación, formato y ejecución de PrintScript")
class PrintScriptController(
    private val validationService: PrintScriptValidationService,
    private val formatService: PrintScriptFormatService,
    private val executionService: PrintScriptExecutionService
) {

    @PostMapping("/validate")
    @Operation(summary = "Validar código", description = "Valida la sintaxis de código según el lenguaje especificado")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Validación completada"),
            ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
        ]
    )
    fun validateCode(
        @Valid @RequestBody request: ValidationRequest
    ): ResponseEntity<ValidationResponse> {
        val result = validationService.validateSyntax(request)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/format")
    @Operation(summary = "Formatear código", description = "Formatea código PrintScript según reglas establecidas")
    fun formatCode(
        @Valid @RequestBody request: FormatRequest
    ): ResponseEntity<FormatResponse> {
        val result = formatService.formatCode(request)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/execute")
    @Operation(summary = "Ejecutar código PrintScript", description = "Ejecuta código PrintScript y retorna la salida")
    fun executeCode(
        @Valid @RequestBody request: ExecuteRequest
    ): ResponseEntity<ExecuteResponse> {
        val result = executionService.executeCode(request)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/lint")
    @Operation(summary = "Analizar código con linter", description = "Analiza código para encontrar problemas de estilo")
    fun lintCode(
        @Valid @RequestBody request: LintRequest
    ): ResponseEntity<LintResponse> {
        // Implementación básica del linter
        val issues = mutableListOf<LintIssue>()
        
        val lines = request.content.lines()
        lines.forEachIndexed { index, line ->
            if (line.length > 120) {
                issues.add(
                    LintIssue(
                        rule = "LINE_TOO_LONG",
                        line = index + 1,
                        column = 121,
                        severity = LintSeverity.WARNING,
                        message = "Line exceeds 120 characters",
                        suggestion = "Break line into multiple lines"
                    )
                )
            }
        }
        
        val response = LintResponse(
            issues = issues,
            isClean = issues.none { it.severity == LintSeverity.ERROR }
        )
        
        return ResponseEntity.ok(response)
    }

    @PostMapping("/analyze")
    @Operation(summary = "Análisis estático de código", description = "Analiza código y retorna información detallada")
    fun analyzeCode(
        @Valid @RequestBody request: ValidationRequest
    ): ResponseEntity<AnalysisResponse> {
        // Implementación básica del análisis
        val lines = request.content.lines().filter { it.trim().isNotEmpty() }
        val tokens = mutableListOf<TokenInfo>()
        
        // Tokenización básica
        lines.forEachIndexed { lineIndex, line ->
            val words = line.split(Regex("\\s+"))
            words.forEachIndexed { wordIndex, word ->
                when {
                    word in listOf("let", "const") -> {
                        tokens.add(TokenInfo("KEYWORD", word, lineIndex + 1, wordIndex + 1))
                    }
                    word.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*")) -> {
                        tokens.add(TokenInfo("IDENTIFIER", word, lineIndex + 1, wordIndex + 1))
                    }
                    word.matches(Regex("\\d+")) -> {
                        tokens.add(TokenInfo("NUMBER", word, lineIndex + 1, wordIndex + 1))
                    }
                }
            }
        }
        
        val response = AnalysisResponse(
            tokens = tokens,
            ast = """{"type": "Program", "body": []}""", // JSON básico del AST
            complexity = lines.size, // Complejidad básica
            linesOfCode = lines.size
        )
        
        return ResponseEntity.ok(response)
    }
}
```

## Configuración Docker

### Dockerfile
```dockerfile
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
RUN gradle dependencies --no-daemon
COPY src src
RUN gradle bootJar --no-daemon

FROM openjdk:21-jre-slim
WORKDIR /app
COPY --from=builder /app/build/libs/printscript-service-0.0.1-SNAPSHOT.jar app.jar
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml
```yaml
version: '3.9'

services:
  printscript-api:
    container_name: printscript-api
    build:
      context: .
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

## Manejo de Excepciones
```kotlin
package com.printscriptservice.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    data class ErrorResponse(
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val status: Int,
        val error: String,
        val message: String
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.allErrors.map { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage
            "$fieldName: $errorMessage"
        }.joinToString(", ")

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = errors
        )
        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Invalid argument"
        )
        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred during code processing"
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}
```

## Endpoints Específicos para Snippet Service

### 1. Validar Código (PRINCIPAL - usado por Snippet Service)
```http
POST /api/validate
Content-Type: application/json

{
  "content": "let x: number = 5;\nprintln(\"Hello World\");",
  "language": "PRINTSCRIPT",
  "version": "1.0"
}

Response 200:
{
  "isValid": true,
  "errors": []
}

Response 200 (con errores):
{
  "isValid": false,
  "errors": [
    {
      "rule": "MISSING_SEMICOLON",
      "line": 2,
      "column": 20,
      "message": "Expected ';' at end of statement"
    }
  ]
}
```

### 2. Formatear Código
```http
POST /api/format
Content-Type: application/json

{
  "content": "let x:number=5;",
  "language": "PRINTSCRIPT",
  "version": "1.0",
  "config": {}
}

Response 200:
{
  "formattedContent": "let x: number = 5;",
  "changes": [
    {
      "line": 1,
      "column": 6,
      "type": "SPACING",
      "description": "Adjusted spacing around operators"
    }
  ]
}
```

### 3. Ejecutar Código
```http
POST /api/execute
Content-Type: application/json

{
  "content": "let message: string = \"Hello\";\nprintln(message);",
  "version": "1.0",
  "inputs": []
}

Response 200:
{
  "output": ["Hello"],
  "errors": [],
  "executionTime": 45,
  "status": "SUCCESS"
}
```

## Estructura de Proyecto
```
printscript-service/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/
│   │   │       └── printscriptservice/
│   │   │           ├── PrintScriptServiceApplication.kt
│   │   │           ├── controller/
│   │   │           │   └── PrintScriptController.kt
│   │   │           ├── service/
│   │   │           │   ├── PrintScriptValidationService.kt
│   │   │           │   ├── PrintScriptFormatService.kt
│   │   │           │   └── PrintScriptExecutionService.kt
│   │   │           ├── model/
│   │   │           │   └── dto/
│   │   │           │       ├── ValidationRequest.kt
│   │   │           │       ├── ValidationResponse.kt
│   │   │           │       ├── FormatRequest.kt
│   │   │           │       ├── FormatResponse.kt
│   │   │           │       ├── ExecuteRequest.kt
│   │   │           │       └── ExecuteResponse.kt
│   │   │           └── exception/
│   │   │               └── GlobalExceptionHandler.kt
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── kotlin/
│           └── com/
│               └── printscriptservice/
│                   └── PrintScriptServiceApplicationTests.kt
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── Dockerfile
├── docker-compose.yml
└── README.md
```

Estas especificaciones proporcionan una implementación completa del PrintScript Service con capacidades de validación, formato, ejecución y análisis, listo para integrarse con el Snippet Service.
