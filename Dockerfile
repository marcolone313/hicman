FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copia solo il JAR necessario
COPY target/CorporateSite.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]