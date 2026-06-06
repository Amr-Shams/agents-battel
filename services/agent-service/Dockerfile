FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY services/pom.xml services/pom.xml
COPY services/agent-service/pom.xml services/agent-service/pom.xml
COPY services/agent-service/src services/agent-service/src
RUN mvn -f services/agent-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /build/services/agent-service/target/agent-service*.jar app.jar
EXPOSE 8005
ENTRYPOINT ["java", "-jar", "/app.jar"]
