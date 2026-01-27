FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copia solo il JAR necessario
COPY target/HicmanCorporateSite-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]