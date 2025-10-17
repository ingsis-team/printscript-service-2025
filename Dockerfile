FROM gradle:8.5-jdk21 AS builder
WORKDIR /home/gradle/project

ARG GITHUB_USERNAME

# Configure Gradle credentials using the secret token
# (token is mounted at /run/secrets/github_token)
RUN --mount=type=secret,id=github_token \
    mkdir -p /home/gradle/.gradle && \
    echo "gpr.user=${GITHUB_USERNAME}" > /home/gradle/.gradle/gradle.properties && \
    echo "gpr.key=$(cat /run/secrets/github_token)" >> /home/gradle/.gradle/gradle.properties && \
    chown -R gradle:gradle /home/gradle/.gradle

COPY build.gradle settings.gradle gradle/ ./
RUN gradle dependencies --no-daemon

COPY src src
RUN gradle bootJar --no-daemon

FROM openjdk:21-slim
RUN apt-get update && apt-get install -y --no-install-recommends postgresql-client && rm -rf /var/lib/apt/lists/*
RUN groupadd -r spring && useradd -r -g spring spring
WORKDIR /app
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
