# 1) Construir el JAR con Gradle
FROM gradle:8.5-jdk21 AS builder
WORKDIR /home/gradle/project

# Copiar configuración de Gradle primero para cachear dependencias
COPY build.gradle settings.gradle gradle/ ./
RUN gradle dependencies --no-daemon

# Copiar el código fuente y construir el JAR ejecutable
COPY src src
RUN gradle bootJar --no-daemon

# 2) Imagen final ligera
FROM openjdk:21-slim

# (Opcional) Cliente de PostgreSQL si necesitás hacer healthchecks o scripts
RUN apt-get update && apt-get install -y --no-install-recommends postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# Crear usuario no-root
RUN groupadd -r spring && useradd -r -g spring spring

# Directorio de trabajo
WORKDIR /app

# Copiar el artefacto desde la etapa de build
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

# Exponer puerto y ejecutar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
