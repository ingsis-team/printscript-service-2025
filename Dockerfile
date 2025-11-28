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
COPY newrelic/newrelic.jar /newrelic/newrelic.jar
COPY newrelic/newrelic.yml /newrelic/newrelic.yml

EXPOSE 8080


ENTRYPOINT ["java","-jar","app.jar"]