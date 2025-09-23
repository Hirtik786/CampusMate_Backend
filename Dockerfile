# Stage 1: build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# copy wrapper and pom first for dependency caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# copy the built jar from the build stage; adjust pattern if your jar name differs
COPY --from=build /app/target/*.jar app.jar

# Expose default port (optional)
EXPOSE 8080

# allow runtime JVM opts via env
ENV JAVA_OPTS=""

# start
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
