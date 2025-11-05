# Development Setup Guide

This guide provides step-by-step instructions for setting up the AutoCare360 backend development environment.

> **Note for Windows Users**: The `make` command is not available by default on Windows. You can either:
> - Install Make using Chocolatey: `choco install make`
> - Install Make using winget: `winget install GnuWin32.Make`
> - Or use the direct commands provided in each section

## Prerequisites

Before you begin, ensure you have the following installed on your system:

### Required Software

- **Java 21**: Download from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/)
- **Maven 3.9+**: Download from [Apache Maven](https://maven.apache.org/download.cgi)
- **Docker**: Download from [Docker Desktop](https://www.docker.com/products/docker-desktop)
- **Docker Compose**: Included with Docker Desktop
- **Git**: Download from [Git SCM](https://git-scm.com/)
- **IDE**: [IntelliJ IDEA](https://www.jetbrains.com/idea/) (recommended) or [VS Code](https://code.visualstudio.com/) with Java extensions

### Optional Tools

- **Kubernetes CLI (kubectl)**: For local Kubernetes development
- **Minikube** or **k3s**: For local Kubernetes cluster
- **Postman** or **Insomnia**: For API testing

## First-Time Developer Setup

### 1. Clone the Repository

```bash
git clone https://github.com/ColabDevelopers/dev-autocare360-backend.git
cd dev-autocare360-backend
```

### 2. Verify Prerequisites

Check that all required tools are installed and accessible:

```bash
# Java
java -version
# Should output: Java 21.x.x

# Maven
mvn -version
# Should output: Apache Maven 3.9.x

# Docker
docker --version
# Should output: Docker version 24.x.x

# Docker Compose
docker-compose --version
# Should output: Docker Compose version 2.x.x

# Git
git --version
# Should output: git version 2.x.x
```

### 3. Environment Configuration

Create environment variables file:

```bash
# Copy the example environment file (if it exists)
cp .env.example .env
```

Edit the `.env` file with your local configuration:

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

**Note**: The actual `.env` file in the project uses these exact variable names and structure.

### 4. IDE Setup

#### IntelliJ IDEA
1. Open the project: `File > Open > Select the project root directory`
2. Ensure JDK 21 is configured: `File > Project Structure > Project SDK`
3. Enable annotation processing for Lombok
4. Install Lombok plugin if prompted

#### VS Code
1. Install recommended extensions:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Docker
2. Open the project folder

### 5. Build the Project

```bash
### 5. Build the Project

```bash
# Clean and compile
mvn clean compile

# Or use the Makefile (Linux/Mac)
make build
```
```

### 6. Run Tests

```bash
### 6. Run Tests

```bash
# Run unit tests
mvn test

# Or use the Makefile (Linux/Mac)
make test
```
```

### 7. Start Local Development Environment

#### Option A: Docker Compose (Recommended)

**On Linux/Mac with Make:**
```bash
# Start MySQL and application with environment file
docker-compose --env-file .env up --build -d

# Or use the Makefile
make setup
```

**On Windows (without Make):**
```bash
# Start services
docker-compose --env-file .env up --build -d

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

This will:
- Start MySQL 8.0 container with the database and user from `.env`
- Run Flyway migrations automatically
- Start the Spring Boot application on port 8080

#### Option B: Manual Setup

If you prefer not to use Docker:

1. Install MySQL 8.0 locally
2. Create database and user as specified in `.env`
3. Update `application.properties` with local DB credentials
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### 8. Verify Setup

1. **Check Application Health**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```
   Should return: `{"status":"UP"}`

2. **Access API Documentation**:
   - Open `http://localhost:8080/swagger-ui.html` in your browser

3. **Check Database**:
   - Connect to MySQL: `mysql -u autocare360_user -p autocare360`
   - Verify tables: `SHOW TABLES;`

## Development Workflow

### Code Style and Linting

```bash
### Code Style and Linting

```bash
# Check code style
mvn spotless:check

# Fix code style automatically
mvn spotless:apply

# Or use the Makefile (Linux/Mac)
make lint
```
```

### Running the Application in Development

```bash
# With hot reload (devtools)
mvn spring-boot:run

# Or with Docker
docker-compose up --build
```

### Database Migrations

Migrations are automatically applied on startup. To create a new migration:

1. Create a new SQL file in `src/main/resources/db/migration/`
2. Follow the naming convention: `V{version}__{description}.sql`
3. Example: `V6__add_new_feature.sql`

### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test jacoco:report
```

## Docker Development

### Building Docker Image

```bash
# Build image
docker build -t autocare360-backend .

# Run container
docker run -p 8080:8080 \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/autocare360 \
  -e MYSQL_USER=autocare360_user \
  -e MYSQL_PASSWORD=your_password \
  -e JWT_SECRET=your_secret \
  autocare360-backend
```

### Docker Compose Commands

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Clean up volumes
docker-compose down -v
```

## Kubernetes Development

### Local Kubernetes Setup

1. Install kubectl and a local cluster (minikube/k3s)
2. Apply manifests:
   ```bash
   kubectl apply -f deployment/kubernetes/
   ```
3. Port forward to access the application:
   ```bash
   kubectl port-forward svc/autocare360-app 8080:8080 -n autocare360
   ```

### Kustomize Overlays

- **Development**: `deployment/kubernetes/overlays/dev/`
- **Production**: `deployment/kubernetes/overlays/prod/`

Apply specific overlay:
```bash
kubectl apply -k deployment/kubernetes/overlays/dev/
```

## Troubleshooting

### Common Issues

1. **Port 8080 already in use**:
   ```bash
   # Find process using port
   netstat -ano | findstr :8080
   # Kill the process or change port in application.properties
   ```

2. **Database connection failed**:
   - Ensure MySQL is running
   - Check credentials in `.env`
   - Verify database exists

3. **Maven build fails**:
   ```bash
   # Clear Maven cache
   rm -rf ~/.m2/repository
   mvn clean install
   ```

4. **Docker build fails**:
   - Ensure Docker Desktop is running
   - Check available disk space
   - Try `docker system prune`

### Logs and Debugging

```bash
# Application logs
docker-compose logs -f app

# Database logs
docker-compose logs -f mysql

# Kubernetes pod logs
kubectl logs -f deployment/autocare360-app -n autocare360
```

## Next Steps

- Read the [API Documentation](API.md)
- Learn about [Deployment](DEPLOYMENT.md)
- Check out [Contributing Guidelines](CONTRIBUTING.md)

For additional help, check the [Issues](https://github.com/ColabDevelopers/dev-autocare360-backend/issues) page or create a new issue.