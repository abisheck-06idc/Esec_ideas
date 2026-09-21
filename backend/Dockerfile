# Build stage
FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration first
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -DskipTests

# Copy source code
COPY src ./src

# Build Spring Boot application
RUN mvn clean package -DskipTests


# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy generated JAR from build stage
COPY --from=build /app/target/idea-submission-portal-1.0.0.jar app.jar

# Render provides PORT dynamically
EXPOSE 8080

# Start Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]