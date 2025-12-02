# syntax=docker/dockerfile:1.4

# ---- build ----
FROM gradle:8.5-jdk21 AS builder
WORKDIR /home/gradle/project

ARG GITHUB_USERNAME
ARG GITHUB_TOKEN
ENV GITHUB_USERNAME=${GITHUB_USERNAME}
ENV GITHUB_TOKEN=${GITHUB_TOKEN}

# Copy wrapper & build files first for cache
COPY --chown=gradle:gradle gradlew ./
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle settings.gradle* build.gradle* ./
RUN chmod +x ./gradlew && ./gradlew --no-daemon --version

# Copy sources and build
COPY --chown=gradle:gradle src src
RUN ./gradlew --no-daemon -x test bootJar

# ---- run ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

# Create /newrelic directory and copy New Relic agent and config
RUN mkdir -p /newrelic


ADD https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-agent.jar /newrelic/newrelic.jar

# Copiamos SOLO la configuración (el .yml es texto, no falla con git)
COPY newrelic/newrelic.yml /newrelic/newrelic.yml

# Damos permisos de lectura
RUN chmod 644 /newrelic/newrelic.jar

EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:/newrelic/newrelic.jar", "-jar", "app.jar"]