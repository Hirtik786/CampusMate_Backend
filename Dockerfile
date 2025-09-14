# Stage 1: Build the application using Maven Wrapper
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only mvnw and pom.xml first
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make wrapper executable
RUN chmod +x mvnw

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy the source code last
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests
