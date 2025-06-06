FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app
LABEL maintainer="33sachinedu@gmail.com"
LABEL description="Spring Boot backend for AI Portfolio Generator"
COPY target/*.jar application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/application.jar"]