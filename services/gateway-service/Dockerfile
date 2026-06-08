FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY services/pom.xml services/pom.xml
COPY services/gateway-service/pom.xml services/gateway-service/pom.xml
COPY services/gateway-service/src services/gateway-service/src
RUN mvn -f services/gateway-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
COPY --from=build /build/services/gateway-service/target/gateway-service*.jar app.jar
EXPOSE 8060
ENTRYPOINT ["java", "-jar", "/app.jar"]
