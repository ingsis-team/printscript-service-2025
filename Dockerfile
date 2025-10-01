FROM gradle:8.5-jdk21 AS build

COPY . /home/gradle/src
WORKDIR /home/gradle/src

RUN --mount=type=secret,id=github_token,env=GITHUB_TOKEN,required \
    gradle assemble

FROM amazoncorretto:21.0.4
EXPOSE 8080

COPY --from=build /home/gradle/src/build/libs/printscript-service-2025-0.0.1-SNAPSHOT.jar ./printscript-service-2025-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "./printscript-service-2025-0.0.1-SNAPSHOT.jar"]
