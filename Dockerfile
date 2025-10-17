# syntax=docker/dockerfile:1.4
FROM gradle:8.5-jdk21 AS builder
WORKDIR /home/gradle/project

# ← get them from compose
ARG GITHUB_USERNAME
ARG GITHUB_TOKEN
# ← make them visible to Gradle (System.getenv)
ENV GITHUB_USERNAME=${GITHUB_USERNAME}
ENV GITHUB_TOKEN=${GITHUB_TOKEN}

# (optional) keep your gradle.properties setup if you want
# but it's not necessary if build.gradle reads env vars
# COPY build files first for better caching
COPY build.gradle settings.gradle gradle/ ./
RUN gradle dependencies --no-daemon

COPY src src
RUN gradle bootJar --no-daemon

# ---- runtime ----
FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]
