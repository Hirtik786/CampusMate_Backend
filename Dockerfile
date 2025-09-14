# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Railway sets $PORT dynamically, so don’t hardcode 8080
ENV PORT=8080
EXPOSE 8080

# Health check (optional, use $PORT not fixed 8080)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:$PORT/actuator/health || exit 1

# Run with $PORT (important for Railway)
CMD ["sh", "-c", "java -Dserver.port=$PORT -jar app.jar"]
