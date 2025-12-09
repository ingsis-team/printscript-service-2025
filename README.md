# PrintScript Service 2025

## Description
Service for processing PrintScript code developed with Spring Boot and Kotlin. Provides validation, formatting, linting, execution, and testing functionality for PrintScript code.

## Features

- **Syntax Validation**: Validate PrintScript code before execution
- **Code Formatting**: Apply customizable formatting rules to code
- **Linting**: Detect code quality and style issues with configurable rules
  - **Identifier Format**: Enforce camelCase or snake_case naming conventions
  - **Print Only**: Restrict code to use only print statements
  - **Input Only**: Restrict code to use only input statements (v1.1+)
- **Script Execution**: Execute PrintScript code and get results
- **Testing**: Run tests with test cases and environment variables
- **Redis Integration**: Asynchronous snippet processing via Redis
- **User-specific Rules**: Each user can configure their own formatting and linting rules

## Running the Project

### With Docker
```bash
docker-compose up -d --build
```

### Local Development

**Prerequisites:**
- Java 21
- Gradle 8.5+
- Environment variables configured:
  - `GITHUB_USERNAME`: GitHub username for accessing GitHub Packages
  - `GITHUB_TOKEN`: GitHub token with read permissions

```bash
./gradlew bootRun
```

### Access Swagger
Once running: http://localhost:8080/swagger-ui.html

## Main Endpoints

### Code Processing
- `POST /validate` - Validate PrintScript code syntax
- `POST /format` - Format code according to user's formatting rules
- `POST /lint` - Analyze code with user's linting rules
- `POST /run` - Execute PrintScript code
- `POST /test` - Run tests with test cases

### Rules Management
- `GET /rules/format/{userId}` - Get formatting rules for a user
- `POST /rules/format/{userId}` - Update formatting rules for a user
- `GET /rules/lint/{userId}` - Get linting rules for a user
- `POST /rules/lint/{userId}` - Update linting rules for a user

### Redis Integration (Asynchronous Processing)
- `PUT /redis/format/snippet` - Trigger asynchronous formatting
- `PUT /redis/lint/snippet` - Trigger asynchronous linting
- `PUT /redis/test/snippet` - Trigger asynchronous testing

## Technologies

- **Spring Boot 3.3.4** - Framework
- **Kotlin 1.9.25** - Language
- **Spring Data JPA** - Persistence
- **Spring WebFlux** - Reactive programming
- **Redis** - Cache and asynchronous processing
- **PostgreSQL** - Database
- **PrintScript Modules** - Internal modules (lexer, parser, linter, formatter, interpreter)
- **Swagger/OpenAPI** - Documentation

## PrintScript Modules

The service uses the following internal PrintScript modules:
- `commons`: Shared utilities
- `lexer`: Lexical analysis
- `parser`: Syntactic analysis
- `linter`: Static code analysis
- `formatter`: Code formatting
- `interpreter`: Code execution

These modules are obtained from GitHub Packages and require authentication.

## Configuration

The service runs on port **8080** by default.

Environment variables (configured in `docker-compose.yml`):
- `DB_HOST`: PostgreSQL host
- `DB_PORT`: PostgreSQL port
- `DB_NAME`: Database name
- `DB_USER`: PostgreSQL user
- `DB_PASSWORD`: PostgreSQL password
- `REDIS_HOST`: Redis host
- `REDIS_PORT`: Redis port
- `ASSET_URL`: Asset service URL
- `GITHUB_USERNAME`: GitHub username (for GitHub Packages)
- `GITHUB_TOKEN`: GitHub token (for GitHub Packages)

## PrintScript Versions

The service supports different versions of PrintScript. The version is specified in each request and determines which language features are available.

## Linting Rules

The linting system supports the following configurable rules:

- **identifier_format**: 
  - `"camelcase"`: Enforces camelCase naming (e.g., `myVariable`)
  - `"snakecase"`: Enforces snake_case naming (e.g., `my_variable`)
- **enablePrintOnly**: Boolean - If `true`, only allows `print` statements (disallows `println`)
- **enableInputOnly**: Boolean - If `true`, only allows `input` statements (v1.1+)

Rules are stored per user and applied automatically when linting snippets. The linter loads rules from a JSON file and applies them during code analysis.

## Recent Updates

- **Fixed linting rules application**: Rules are now properly loaded and applied during linting
- **Added comprehensive logging**: Step-by-step logging for debugging linting process
- **Language case handling**: Service now handles both uppercase and lowercase language names

