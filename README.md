# AutoCare360 - Automotive Care Management System

A comprehensive automotive care management system built with Spring Boot, providing appointment scheduling, employee management, time tracking, and real-time notifications.

## 🚀 Features

- **Appointment Management**: Schedule and manage automotive service appointments
- **Employee Dashboard**: Track assigned jobs and daily activities
- **Time Logging**: Record work hours for different appointments
- **Real-time Notifications**: WebSocket-based notification system
- **Role-based Access Control**: Admin and Employee roles with different permissions
- **RESTful API**: Well-documented API endpoints

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 21
- **Database**: MySQL 8.0
- **Build Tool**: Maven
- **Security**: Spring Security with JWT
- **Real-time**: WebSocket (STOMP)
- **Database Migration**: Flyway
- **Containerization**: Docker & Docker Compose
- **Orchestration**: Kubernetes
- **Infrastructure**: Terraform (AWS)
- **Monitoring**: Prometheus & Grafana
- **CI/CD**: GitHub Actions

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- MySQL 8.0
- Docker & Docker Compose (for containerized setup)

## 🚀 Quick Start

### Local Development (Docker Compose)

1. **Clone the repository**
   ```bash
   git clone https://github.com/ColabDevelopers/dev-autocare360-backend.git
   cd dev-autocare360-backend
   ```

2. **Set up environment variables**
   ```bash
   copy .env.example .env
   # Edit .env with your configuration
   ```

3. **Run with Docker Compose**
   ```bash
   docker-compose up -d
   ```

4. **Access the application**
   - Application: http://localhost:8080
   - Grafana: http://localhost:3000 (admin/admin)
   - Prometheus: http://localhost:9090
   - API Documentation: http://localhost:8080/swagger-ui.html

### Local Development (Manual)

1. **Configure MySQL Database**
   ```sql
   CREATE DATABASE autocare360;
   CREATE USER 'appuser'@'localhost' IDENTIFIED BY 'apppass';
   GRANT ALL PRIVILEGES ON autocare360.* TO 'appuser'@'localhost';
   ```

2. **Configure application.properties**
   ```bash
   copy src\main\resources\application.properties.example src\main\resources\application.properties
   # Edit application.properties with your database credentials
   ```

3. **Build and run the application**
   ```bash
   mvnw.cmd clean package
   java -jar target\autocare360-0.0.1-SNAPSHOT.jar
   ```

## 🏗️ Project Structure

```
autocare360/
├── .github/workflows/       # CI/CD pipelines
│   ├── ci.yml              # Continuous Integration
│   └── cd.yml              # Continuous Deployment
├── infrastructure/
│   ├── terraform/          # Infrastructure as Code
│   └── kubernetes/         # K8s manifests
├── config/                 # Configuration files
│   └── prometheus/         # Prometheus config
├── scripts/                # Deployment scripts
├── src/
│   ├── main/
│   │   ├── java/com/autocare360/
│   │   │   ├── config/     # Configuration classes
│   │   │   ├── controller/ # REST controllers
│   │   │   ├── dto/        # Data Transfer Objects
│   │   │   ├── entity/     # JPA entities
│   │   │   ├── repo/       # Repositories
│   │   │   ├── security/   # Security configuration
│   │   │   └── service/    # Business logic
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/  # Flyway migrations
│   └── test/               # Test classes
├── docs/                   # Documentation
├── Dockerfile              # Multi-stage Docker build
├── docker-compose.yml      # Local development stack
└── pom.xml                 # Maven dependencies
```

## 🔒 Security

The application uses JWT (JSON Web Tokens) for authentication:

- Access tokens expire after 30 minutes (production) / 1 hour (development)
- Passwords are encrypted using BCrypt
- Role-based access control (RBAC)
- CORS configuration for frontend integration

## 📊 Monitoring & Observability

### Health Checks

The application includes Spring Boot Actuator endpoints:

- **Health**: `/actuator/health`
- **Liveness**: `/actuator/health/liveness`
- **Readiness**: `/actuator/health/readiness`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

### Grafana Dashboards

Access Grafana at http://localhost:3000 with:
- Username: `admin`
- Password: `admin`

Pre-configured dashboards monitor:
- Application metrics
- JVM performance
- Database connections
- HTTP request rates
- Error rates

## 🚀 CI/CD Pipeline

### Continuous Integration (CI)

Triggers on push to `develop`, `feature/*`, `hotfix/*` branches and PRs to `main`/`develop`:

1. **Test Stage**: Runs unit and integration tests with MySQL
2. **Security Scan**: OWASP dependency check and Trivy container scan
3. **Build Stage**: Builds JAR and Docker image, pushes to registry

### Continuous Deployment (CD)

Triggers on push to `main` branch:

1. **Deploy to Production**: Updates Kubernetes deployment
2. **Smoke Tests**: Validates deployment health

## ☁️ Cloud Deployment

### AWS Infrastructure (Terraform)

```bash
cd infrastructure/terraform
terraform init
terraform plan
terraform apply
```

Creates:
- EKS Cluster for Kubernetes
- RDS MySQL instance
- ElastiCache Redis cluster
- VPC with public/private subnets
- Security groups and IAM roles

### Kubernetes Deployment

```bash
# Create namespace
kubectl create namespace autocare360-prod

# Create secrets
kubectl create secret generic autocare360-secrets \
  --from-literal=db-url=jdbc:mysql://YOUR_RDS_ENDPOINT:3306/autocare360 \
  --from-literal=db-username=YOUR_USERNAME \
  --from-literal=db-password=YOUR_PASSWORD \
  --from-literal=jwt-secret=YOUR_JWT_SECRET \
  -n autocare360-prod

# Deploy application
kubectl apply -k infrastructure/kubernetes/base -n autocare360-prod

# Check deployment status
kubectl get pods -n autocare360-prod
kubectl rollout status deployment/autocare360 -n autocare360-prod
```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | Database JDBC URL | `jdbc:mysql://localhost:3306/autocare360` |
| `DB_USERNAME` | Database username | `appuser` |
| `DB_PASSWORD` | Database password | `apppass` |
| `JWT_SECRET` | JWT signing secret | (required) |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |

### Spring Profiles

- **dev**: Development with detailed logging and debugging
- **prod**: Production with optimized settings and security

## 📝 API Documentation

API documentation is available at:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: `docs/openapi.yaml`
- API Guide: `docs/API.md`

## 🧪 Testing

```bash
# Run all tests
mvnw.cmd test

# Run specific test class
mvnw.cmd test -Dtest=Autocare360ApplicationTests

# Run tests with coverage
mvnw.cmd test jacoco:report
```

## 🔍 Troubleshooting

### Database Connection Issues
```bash
# Check MySQL is running
docker ps | findstr mysql

# Check database logs
docker logs autocare360-mysql
```

### Application Not Starting
```bash
# Check application logs
docker logs autocare360-app

# Run health check
curl http://localhost:8080/actuator/health
```

## 📦 Deployment Scripts

### Windows
```cmd
set ENVIRONMENT=prod
set VERSION=1.0.0
set DOCKER_REGISTRY=your-docker-username
scripts\deploy.cmd
```

### Linux/Mac
```bash
export ENVIRONMENT=prod
export VERSION=1.0.0
export DOCKER_REGISTRY=your-docker-username
./scripts/deploy.sh
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- ColabDevelopers Team

## 📞 Support

For support, email support@autocare360.com or open an issue in the repository.

---

**Built with ❤️ by ColabDevelopers**
