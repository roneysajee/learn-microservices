# Stage 1: Runtime Extractor
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Extract the fat jar built by the root maven pom orchestration
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]