# STAGE 1: Build the application JAR using Maven
#------------------------------------------------------------------------------
# Use an official Maven image that includes JDK 21.
# 'maven:3.9-eclipse-temurin-21' means Maven 3.9.x with Eclipse Temurin JDK 21.
# This image has Maven and Java pre-installed.
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set the working directory in the container
WORKDIR /app

# Copy only the pom.xml first. This leverages Docker's layer caching.
# If pom.xml hasn't changed, Docker can reuse the dependency download layer.
COPY pom.xml .

# Download all project dependencies.
# '-B' runs Maven in non-interactive (batch) mode.
# 'dependency:go-offline' attempts to resolve all dependencies.
RUN mvn dependency:go-offline -B

# Copy the rest of your application's source code
COPY src ./src

# Package the application to create the JAR file.
# '-DskipTests' skips running tests during the Docker build for speed.
RUN mvn package -DskipTests -B

#------------------------------------------------------------------------------
# STAGE 2: Create the final, lean runtime image
#------------------------------------------------------------------------------
# Use an official OpenJDK JRE (Java Runtime Environment) image for JDK 21.
# JRE is smaller than JDK as it doesn't contain development tools like compilers.
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the executable JAR from the 'builder' stage.
# The JAR will be in /app/target/ in the 'builder' stage.
# We're copying it to /app/application.jar in the final image.
COPY --from=builder /app/target/*.jar application.jar

# Add metadata
LABEL maintainer="33sachinedu@gmail.com"
LABEL description="Spring Boot backend for AI Portfolio Generator"

# Expose the port your Spring Boot application listens on
EXPOSE 8080

# Define the command to run when the container starts
ENTRYPOINT ["java", "-jar", "/app/application.jar"]