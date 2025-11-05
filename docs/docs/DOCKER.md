# Docker Setup Guide

This guide covers Docker containerization for the AutoCare360 backend application.

## Docker Architecture

The application uses a multi-stage Docker build for optimal image size and security.

### Dockerfile Structure

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar file
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
```

## Building Docker Images

### Local Build

```bash
# Build application image
docker build -t autocare360-backend:latest .

# Build with specific target
docker build --target build -t autocare360-build:latest .

# Build with build args
docker build \
  --build-arg MAVEN_VERSION=3.9 \
  --build-arg JAVA_VERSION=21 \
  -t autocare360-backend:latest .
```

### Docker Build Options

```bash
# Build with no cache
docker build --no-cache -t autocare360-backend:latest .

# Build for multiple platforms
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t autocare360-backend:latest \
  --push .
```

## Docker Compose Setup

### Development Environment

```bash
# Start all services with environment file
docker-compose --env-file .env up --build -d

# Start in background
docker-compose --env-file .env up -d --build

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Clean up volumes
docker-compose down -v
```

### Service Configuration

The `docker-compose.yml` defines:

- **MySQL 8.0**: Database service with health checks
- **App**: Spring Boot application with dependency on MySQL
- **Networks**: Isolated network for service communication
- **Volumes**: Persistent data storage

### Environment Variables

Create a `.env` file in the project root:

```env
# Database Configuration
MYSQL_ROOT_PASSWORD=your_mysql_root_password_here
MYSQL_DATABASE=autocare360
MYSQL_USER=autocare
MYSQL_PASSWORD=your_mysql_password_here

# Application Configuration
DB_URL=jdbc:mysql://mysql:3306/autocare360
JWT_SECRET=your_jwt_secret_key_here_minimum_256_bits_long
```

## Container Management

### Running Containers

```bash
# Run application container
docker run -d \
  --name autocare360-app \
  -p 8080:8080 \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/autocare360 \
  -e MYSQL_USER=autocare \
  -e MYSQL_PASSWORD=your_password \
  -e JWT_SECRET=your_secret \
  autocare360-backend:latest

# Run with volume mounts for development
docker run -d \
  --name autocare360-dev \
  -p 8080:8080 \
  -v $(pwd)/src:/app/src \
  -v $(pwd)/target:/app/target \
  -e SPRING_PROFILES_ACTIVE=dev \
  autocare360-backend:latest
```

### Debugging Containers

```bash
# View container logs
docker logs autocare360-app

# Follow logs
docker logs -f autocare360-app

# Execute commands in running container
docker exec -it autocare360-app /bin/bash

# Check container resource usage
docker stats autocare360-app

# Inspect container configuration
docker inspect autocare360-app
```

### Container Health Checks

```bash
# Check health status
docker ps

# View health check logs
docker inspect autocare360-app | grep -A 10 "Health"

# Manual health check
curl http://localhost:8080/actuator/health
```

## Docker Networking

### Network Configuration

```bash
# Create custom network
docker network create autocare360-network

# Connect containers to network
docker network connect autocare360-network autocare360-app
docker network connect autocare360-network mysql-container

# Inspect network
docker network inspect autocare360-network
```

### Service Discovery

- Use service names as hostnames within Docker networks
- MySQL is accessible as `mysql:3306` from the app container
- External access via port mapping (`-p 8080:8080`)

## Data Persistence

### Volumes

```bash
# Create named volume
docker volume create mysql_data

# List volumes
docker volume ls

# Inspect volume
docker volume inspect mysql_data

# Remove volume
docker volume rm mysql_data
```

### Bind Mounts (Development)

```bash
# Mount source code for hot reload
docker run -v $(pwd)/src:/app/src \
  -v $(pwd)/target:/app/target \
  autocare360-backend
```

## Security Best Practices

### Image Security

```bash
# Scan image for vulnerabilities
docker scan autocare360-backend:latest

# Use trusted base images
FROM eclipse-temurin:21-jre-alpine  # Minimal attack surface

# Run as non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring
```

### Runtime Security

```bash
# Limit container resources
docker run \
  --memory=512m \
  --cpus=0.5 \
  --read-only \
  --tmpfs /tmp \
  autocare360-backend

# Drop capabilities
docker run --cap-drop ALL --cap-add NET_BIND_SERVICE autocare360-backend
```

## Multi-Environment Setup

### Development

```yaml
# docker-compose.dev.yml
version: '3.8'
services:
  app:
    build:
      context: .
      target: development
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    volumes:
      - ./src:/app/src
```

### Production

```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  app:
    image: ghcr.io/yourorg/autocare360-backend:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    secrets:
      - db_password
      - jwt_secret
```

## Docker Registry

### GitHub Container Registry

```bash
# Login to GHCR
echo $GITHUB_TOKEN | docker login ghcr.io -u $GITHUB_USERNAME --password-stdin

# Tag image
docker tag autocare360-backend:latest ghcr.io/yourorg/autocare360-backend:latest

# Push image
docker push ghcr.io/yourorg/autocare360-backend:latest

# Pull image
docker pull ghcr.io/yourorg/autocare360-backend:latest
```

### Private Registry

```bash
# Login to private registry
docker login your-registry.com

# Push with registry
docker tag autocare360-backend:latest your-registry.com/autocare360-backend:latest
docker push your-registry.com/autocare360-backend:latest
```

## Performance Optimization

### Image Optimization

```dockerfile
# Use .dockerignore
# .dockerignore
target/
.mvn/
mvnw
mvnw.cmd
*.md
docs/
.github/

# Multi-stage builds reduce size
FROM maven:3.9-eclipse-temurin-21-alpine AS build
# Build stage

FROM eclipse-temurin:21-jre-alpine AS runtime
# Runtime stage - only includes JRE
```

### Runtime Optimization

```bash
# JVM tuning for containers
JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Connection pooling
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

## Troubleshooting

### Common Issues

1. **Port already in use**:
   ```bash
   docker run -p 8081:8080 autocare360-backend  # Use different host port
   ```

2. **Database connection failed**:
   ```bash
   # Check if MySQL is healthy
   docker-compose ps
   docker-compose logs mysql
   ```

3. **Out of memory**:
   ```bash
   # Increase Docker memory limit
   # Docker Desktop: Settings > Resources > Memory
   ```

4. **Slow builds**:
   ```bash
   # Use BuildKit
   DOCKER_BUILDKIT=1 docker build -t autocare360-backend .
   ```

### Debug Commands

```bash
# View all containers
docker ps -a

# Clean up
docker system prune -a

# View disk usage
docker system df

# Check Docker version compatibility
docker version
```

## CI/CD Integration

### GitHub Actions

```yaml
- name: Build and push Docker image
  uses: docker/build-push-action@v5
  with:
    context: .
    push: true
    tags: ghcr.io/${{ github.repository }}:latest
    cache-from: type=gha
    cache-to: type=gha,mode=max
```

### Automated Testing

```bash
# Run tests in container
docker run --rm \
  -v $(pwd):/app \
  -w /app \
  maven:3.9-eclipse-temurin-21-alpine \
  mvn test
```

For more information, see the [Development Guide](DEVELOPMENT.md) and [Deployment Guide](DEPLOYMENT.md).