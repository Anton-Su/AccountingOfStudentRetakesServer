FROM gradle:8.14-jdk21 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN gradle clean buildFatJar --no-daemon -Dkotlin.compiler.execution.strategy=in-process

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
EXPOSE 8080
COPY --from=build /home/gradle/src/build/libs/*-all.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]