FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY services/pom.xml services/pom.xml
COPY services/prompt-service services/prompt-service
RUN mvn -pl services/prompt-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /build/services/prompt-service/target/prompt-service*.jar app.jar
EXPOSE 8003
ENTRYPOINT ["java", "-jar", "/app.jar"]
