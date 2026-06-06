FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY services/pom.xml services/pom.xml
COPY services/response-service/pom.xml services/response-service/pom.xml
COPY services/response-service/src services/response-service/src
RUN mvn -f services/response-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /build/services/response-service/target/response-service*.jar app.jar
EXPOSE 8004
ENTRYPOINT ["java", "-jar", "/app.jar"]
