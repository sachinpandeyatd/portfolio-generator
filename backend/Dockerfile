# Single-stage build using Maven image
FROM maven:3.9.6-openjdk-21-slim

WORKDIR /app

# Copy the entire project
COPY . .

# Build the application
RUN mvn clean package -DskipTests -B -q

# Labels
LABEL maintainer="33sachinedu@gmail.com"
LABEL description="Spring Boot backend for AI Portfolio Generator"

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/target/*.jar"]