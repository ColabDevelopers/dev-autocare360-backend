# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn/ .mvn/
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml hasn't changed)
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the port the app runs on
EXPOSE 8080

# Run the jar file
CMD ["java", "-jar", "target/autocare360-0.0.1-SNAPSHOT.jar"]