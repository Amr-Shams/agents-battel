FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY services/pom.xml services/pom.xml
COPY services/discovery-service/pom.xml services/discovery-service/pom.xml
COPY services/discovery-service/src services/discovery-service/src
RUN mvn -f services/discovery-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
COPY --from=build /build/services/discovery-service/target/discovery-service*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app.jar"]
