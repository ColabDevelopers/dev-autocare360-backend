# Multi-stage build for production
FROM eclipse-temurin:21-jdk-jammy as builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Production stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create a non-root user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

# Copy the built JAR from builder stage
COPY --from=builder /app/target/autocare360-*.jar app.jar

# Expose application port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]