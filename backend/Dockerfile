FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar application.jar

LABEL maintainer="33sachinedu@gmail.com"
LABEL description="Spring Boot backend for AI Portfolio Generator"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/application.jar"]