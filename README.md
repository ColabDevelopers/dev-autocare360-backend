# 🚗 AutoCare360 Backend

Enterprise-grade Spring Boot backend application for AutoCare360 vehicle management system.

[![CI/CD Pipeline](https://github.com/ColabDevelopers/dev-autocare360-backend/workflows/CI/CD%20Pipeline/badge.svg)](https://github.com/ColabDevelopers/dev-autocare360-backend/actions)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Quick Start](#-quick-start)
- [Project Structure](#-project-structure)
- [Development](#-development)
- [Deployment](#-deployment)
- [API Documentation](#-api-documentation)
- [Contributing](#-contributing)
- [License](#-license)

## ✨ Features

- 🔐 **JWT Authentication & Authorization**
- 👥 **Role-Based Access Control** (Admin, Employee, Customer)
- 📅 **Appointment Management**
- ⏱️ **Time Tracking & Logging**
- 🔔 **Real-time Notifications** (WebSocket)
- 📊 **Dashboard Analytics**
- 🗄️ **Database Migration** with Flyway
- 🐳 **Containerized** with Docker
- ☸️ **Kubernetes Ready**
- 📈 **Monitoring & Metrics** (Prometheus & Grafana)
- 🔍 **Health Checks & Actuator**
- 🚀 **CI/CD Pipeline** (GitHub Actions)

## 🛠️ Tech Stack

### Core
- **Java 21** - Latest LTS version
- **Spring Boot 3.5.6** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database access
- **Hibernate** - ORM
- **MySQL 8.0** - Database

### Additional Technologies
- **Flyway** - Database migrations
- **JWT** - Token-based authentication
- **WebSocket** - Real-time communication
- **Lombok** - Reduce boilerplate code
- **Maven** - Dependency management

### DevOps & Infrastructure
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Kubernetes** - Container orchestration
- **GitHub Actions** - CI/CD
- **Prometheus** - Metrics collection
- **Grafana** - Metrics visualization

## 🚀 Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.9+
- Docker & Docker Compose (for containerized setup)
- MySQL 8.0 (for local setup)

### Option 1: Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone https://github.com/ColabDevelopers/dev-autocare360-backend.git
   cd dev-autocare360-backend
   ```

2. **Configure environment**
   ```bash
   copy .env.example .env
   # Edit .env with your configuration
   ```

3. **Start all services**
   ```bash
   docker-compose up -d
   ```

4. **Access the application**
   - API: http://localhost:8080
   - Health Check: http://localhost:8080/actuator/health
   - Metrics: http://localhost:8080/actuator/prometheus

### Option 2: Local Development

1. **Clone and configure**
   ```bash
   git clone https://github.com/ColabDevelopers/dev-autocare360-backend.git
   cd dev-autocare360-backend
   ```

2. **Setup MySQL database**
   ```bash
   mysql -u root -p
   CREATE DATABASE autocare360;
   CREATE USER 'autocare360user'@'localhost' IDENTIFIED BY 'autocare360pass';
   GRANT ALL PRIVILEGES ON autocare360.* TO 'autocare360user'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Configure application**
   ```bash
   copy src\main\resources\application.properties.example src\main\resources\application.properties
   # Edit application.properties with your database credentials
   ```

4. **Build and run**
   ```bash
   # Windows
   mvnw.cmd clean install
   mvnw.cmd spring-boot:run

   # Linux/Mac
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

## 📁 Project Structure

```
dev-autocare360-backend/
├── .github/
│   └── workflows/              # GitHub Actions CI/CD workflows
│       ├── ci-cd.yml          # Main CI/CD pipeline
│       └── database-migration.yml
├── docker/                     # Docker configurations
│   ├── prometheus.yml         # Prometheus configuration
│   ├── grafana-datasource.yml # Grafana datasource
│   └── docker-compose.monitoring.yml
├── docs/                       # Documentation
│   ├── API.md
│   ├── openapi.yaml
│   ├── database/
│   └── notifications/
├── kubernetes/                 # Kubernetes manifests
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   ├── deployment.yaml
│   ├── ingress.yaml
│   └── hpa.yaml
├── scripts/                    # Automation scripts
│   ├── build.cmd/sh
│   ├── deploy.cmd/sh
│   └── health-check.cmd/sh
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/autocare360/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── repo/
│   │   │       ├── security/
│   │   │       ├── service/
│   │   │       └── util/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-staging.properties
│   │       ├── application-prod.properties
│   │       └── db/migration/
│   └── test/
├── Dockerfile                  # Multi-stage Docker build
├── docker-compose.yml         # Development environment
├── .dockerignore
├── .env.example               # Environment variables template
├── Makefile                   # Build automation
├── DEVOPS.md                  # DevOps documentation
└── pom.xml                    # Maven configuration
```

## 💻 Development

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=YourTestClass

# Run with coverage
mvn test jacoco:report
```

### Code Style

```bash
# Check code style
mvn checkstyle:check

# Format code
mvn spotless:apply
```

### Database Migrations

```bash
# Run migrations
mvn flyway:migrate

# Validate migrations
mvn flyway:validate

# Migration info
mvn flyway:info

# Repair migration history
mvn flyway:repair
```

### Build Commands

```bash
# Clean build
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Build Docker image
docker build -t autocare360-backend:latest .
```

## 🚢 Deployment

### Docker Deployment

```bash
# Build and deploy
docker-compose up --build -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Kubernetes Deployment

```bash
# Deploy to Kubernetes
kubectl apply -f kubernetes/

# Check deployment status
kubectl get pods -n autocare360

# View logs
kubectl logs -f deployment/autocare360-backend -n autocare360

# Scale deployment
kubectl scale deployment autocare360-backend --replicas=5 -n autocare360
```

### Using Make Commands

```bash
# Build application
make build

# Run tests
make test

# Docker build and run
make docker-build
make docker-run

# Deploy to Kubernetes
make k8s-deploy

# Health check
make health-check
```

## 📚 API Documentation

### Base URL

```
http://localhost:8080/api
```

### Authentication

All endpoints (except auth endpoints) require JWT token:

```bash
Authorization: Bearer <your-jwt-token>
```

### Key Endpoints

#### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/refresh` - Refresh token

#### Users
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `POST /api/users/change-password` - Change password

#### Appointments
- `GET /api/appointments` - List appointments
- `POST /api/appointments` - Create appointment
- `PUT /api/appointments/{id}` - Update appointment
- `DELETE /api/appointments/{id}` - Delete appointment

#### Notifications
- `GET /api/notifications` - Get notifications
- `PUT /api/notifications/{id}/read` - Mark as read

For detailed API documentation, see [docs/API.md](docs/API.md) or visit `/swagger-ui.html` when running the application.

## 📊 Monitoring

### Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health

# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness
```

### Metrics

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Actuator Metrics**: http://localhost:8080/actuator/metrics

### Logs

```bash
# Docker logs
docker logs autocare360-backend

# Kubernetes logs
kubectl logs -f deployment/autocare360-backend -n autocare360
```

## 🔐 Security

- JWT-based authentication
- Password encryption with BCrypt
- Role-based access control
- CORS configuration
- SQL injection prevention
- XSS protection
- HTTPS enforcement (production)

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## 📝 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | Database connection URL | - |
| `DB_USERNAME` | Database username | - |
| `DB_PASSWORD` | Database password | - |
| `JWT_SECRET` | JWT secret key (min 256 bits) | - |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | - |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | dev |
| `SERVER_PORT` | Server port | 8080 |

## 🐛 Troubleshooting

### Common Issues

1. **Database connection failed**
   - Check if MySQL is running
   - Verify database credentials
   - Ensure database exists

2. **Application won't start**
   - Check Java version (must be 21+)
   - Verify all environment variables are set
   - Check if port 8080 is available

3. **Tests failing**
   - Ensure test database is configured
   - Check for port conflicts
   - Verify test data initialization

For more troubleshooting tips, see [DEVOPS.md](DEVOPS.md).

## 📞 Support

- 📧 Email: support@autocare360.com
- 🐛 Issues: [GitHub Issues](https://github.com/ColabDevelopers/dev-autocare360-backend/issues)
- 📖 Documentation: [docs/](docs/)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **DevOps Team** - Infrastructure & deployment
- **Backend Team** - API development
- **QA Team** - Testing & quality assurance

## 🙏 Acknowledgments

- Spring Boot team for the amazing framework
- All contributors who helped make this project better
- Open source community

---

Made with ❤️ by the AutoCare360 Team
