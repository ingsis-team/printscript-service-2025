# PrintScript Service 2025

## Description
Service for processing PrintScript code developed with Spring Boot and Kotlin. Provides validation, formatting, linting, execution, and testing functionality for PrintScript code.

## Features

- **Syntax Validation**: Validate PrintScript code before execution
- **Code Formatting**: Apply customizable formatting rules to code
- **Linting**: Detect code quality and style issues
- **Script Execution**: Execute PrintScript code and get results
- **Testing**: Run tests with test cases and environment variables
- **Redis Integration**: Asynchronous snippet processing via Redis

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

- `POST /api/validate` - Validate PrintScript code syntax
- `POST /api/format` - Format code according to configured rules
- `POST /api/lint` - Analyze code with the linter
- `POST /api/run` - Execute PrintScript code
- `POST /api/test` - Run tests with test cases
- `GET /api/formatter-rules` - Get formatting rules
- `PUT /api/formatter-rules` - Update formatting rules
- `GET /api/linter-rules` - Get linting rules
- `PUT /api/linter-rules` - Update linting rules

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

