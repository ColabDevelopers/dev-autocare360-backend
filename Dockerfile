# ---------- STAGE 1: Build ----------
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copy the rest of the project
COPY . .
RUN mvn -B -q clean package -DskipTests


# ---------- STAGE 2: Runtime ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring

# Copy final jar
COPY --from=builder /app/target/*.jar app.jar

# Health check (no curl)
HEALTHCHECK CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
