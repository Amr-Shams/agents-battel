FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY services/pom.xml services/pom.xml
COPY services/response-service services/response-service
RUN mvn -pl services/response-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /build/services/response-service/target/response-service*.jar app.jar
EXPOSE 8004
ENTRYPOINT ["java", "-jar", "/app.jar"]
