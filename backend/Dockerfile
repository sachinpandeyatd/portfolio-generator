FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY target/*.jar application.jar

LABEL maintainer="33sachinedu@gmail.com"
LABEL description="Spring Boot backend for AI Portfolio Generator"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/application.jar"]