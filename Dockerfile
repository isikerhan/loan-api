# syntax=docker/dockerfile:1
FROM amazoncorretto:21-alpine3.20
WORKDIR /app
COPY ./target/loan-api-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
