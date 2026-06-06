FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY services/pom.xml services/pom.xml
COPY services/prompt-service/pom.xml services/prompt-service/pom.xml
COPY services/prompt-service/src services/prompt-service/src
RUN mvn -f services/prompt-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /build/services/prompt-service/target/prompt-service*.jar app.jar
EXPOSE 8003
ENTRYPOINT ["java", "-jar", "/app.jar"]
