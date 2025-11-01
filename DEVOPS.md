# AutoCare360 Backend - DevOps Documentation

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.9+
- Docker & Docker Compose
- Git

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd dev-autocare360-backend
   ```

2. **Configure environment**
   ```bash
   copy .env.example .env
   # Edit .env with your configuration
   ```

3. **Run with Docker Compose**
   ```bash
   docker-compose up -d
   ```

4. **Access the application**
   - API: http://localhost:8080
   - Health Check: http://localhost:8080/actuator/health
   - API Documentation: http://localhost:8080/swagger-ui.html

## 📁 Project Structure

```
dev-autocare360-backend/
├── .github/
│   └── workflows/          # GitHub Actions CI/CD pipelines
│       ├── ci-cd.yml       # Main CI/CD pipeline
│       └── database-migration.yml
├── docker/                 # Docker-related files
├── kubernetes/            # Kubernetes deployment manifests
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   ├── deployment.yaml
│   ├── ingress.yaml
│   └── hpa.yaml
├── scripts/               # Automation scripts
│   ├── build.cmd/sh
│   ├── deploy.cmd/sh
│   └── health-check.cmd/sh
├── src/                   # Application source code
├── Dockerfile             # Multi-stage Docker build
├── docker-compose.yml     # Local development environment
└── .env.example          # Environment variables template
```

## 🛠️ Build & Deployment

### Build Application

**Windows:**
```cmd
scripts\build.cmd
```

**Linux/Mac:**
```bash
chmod +x scripts/build.sh
./scripts/build.sh
```

### Build Docker Image

```bash
docker build -t autocare360-backend:latest .
```

### Deploy with Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Deploy to Kubernetes

```bash
# Create namespace
kubectl apply -f kubernetes/namespace.yaml

# Create secrets (edit first!)
kubectl apply -f kubernetes/secret.yaml

# Create configmap
kubectl apply -f kubernetes/configmap.yaml

# Deploy application
kubectl apply -f kubernetes/deployment.yaml

# Create ingress
kubectl apply -f kubernetes/ingress.yaml

# Setup autoscaling
kubectl apply -f kubernetes/hpa.yaml
```

## 🔄 CI/CD Pipeline

### GitHub Actions Workflows

1. **CI/CD Pipeline** (`.github/workflows/ci-cd.yml`)
   - Triggered on push/PR to main, DevOps, develop branches
   - Steps:
     - Build and test
     - Code quality analysis
     - Security scanning
     - Docker image build and push
     - Automated deployment

2. **Database Migration** (`.github/workflows/database-migration.yml`)
   - Manual trigger via workflow_dispatch
   - Supports multiple environments
   - Flyway migration operations

### Required GitHub Secrets

```
DOCKER_USERNAME           # Docker Hub username
DOCKER_PASSWORD          # Docker Hub password/token
DB_URL                   # Database connection URL
DB_USERNAME              # Database username
DB_PASSWORD              # Database password
JWT_SECRET               # JWT secret key
SONAR_TOKEN             # SonarCloud token (optional)
```

## 🗄️ Database Management

### Run Flyway Migrations Locally

```bash
mvn flyway:migrate
```

### Migration Commands

```bash
# Migrate
mvn flyway:migrate

# Validate
mvn flyway:validate

# Info
mvn flyway:info

# Repair
mvn flyway:repair

# Clean (BE CAREFUL - DROPS ALL OBJECTS)
mvn flyway:clean
```

## 🔒 Security

### Environment Variables

Never commit sensitive data. Use environment variables for:
- Database credentials
- JWT secrets
- API keys
- Third-party service credentials

### Secrets Management

**Local Development:**
- Use `.env` files (gitignored)
- Copy from `.env.example`

**Kubernetes:**
- Use Kubernetes Secrets
- Consider using sealed-secrets or external secret managers (AWS Secrets Manager, HashiCorp Vault)

**CI/CD:**
- Use GitHub Secrets
- Rotate secrets regularly

## 📊 Monitoring & Logging

### Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health

# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness
```

### View Logs

**Docker:**
```bash
docker logs autocare360-backend
docker-compose logs -f app
```

**Kubernetes:**
```bash
kubectl logs -f deployment/autocare360-backend -n autocare360
```

## 🔧 Configuration

### Application Profiles

- `dev` - Development environment
- `staging` - Staging environment
- `prod` - Production environment

Set profile using:
```bash
SPRING_PROFILES_ACTIVE=prod
```

### Environment-Specific Configuration

Create environment-specific files:
- `.env.dev`
- `.env.staging`
- `.env.prod`

## 🚢 Deployment Strategies

### Rolling Deployment (Default)
- Zero downtime
- Gradual rollout
- Easy rollback

### Blue-Green Deployment
- Two identical environments
- Instant switch
- Easy rollback

### Canary Deployment
- Gradual traffic shift
- Risk mitigation
- A/B testing capability

## 📈 Scaling

### Horizontal Pod Autoscaler (HPA)

Automatically scales based on:
- CPU utilization (70% threshold)
- Memory utilization (80% threshold)
- Min replicas: 2
- Max replicas: 10

```bash
# View HPA status
kubectl get hpa -n autocare360

# Describe HPA
kubectl describe hpa autocare360-backend-hpa -n autocare360
```

## 🐛 Troubleshooting

### Application won't start

1. Check logs: `docker logs autocare360-backend`
2. Verify database connectivity
3. Confirm environment variables are set
4. Check port 8080 availability

### Database connection issues

1. Verify database is running: `docker ps`
2. Check connection string in `.env`
3. Confirm credentials are correct
4. Test database connectivity: `docker exec -it autocare360-mysql mysql -u root -p`

### Build failures

1. Clean build: `mvn clean`
2. Update dependencies: `mvn dependency:resolve`
3. Check Java version: `java -version`
4. Verify Maven installation: `mvn -version`

## 🤝 Contributing

1. Create feature branch from `DevOps`
2. Make changes
3. Run tests: `mvn test`
4. Submit pull request
5. Wait for CI/CD pipeline to pass

## 📝 Best Practices

1. **Never commit secrets** - Use environment variables
2. **Test locally** - Use Docker Compose before deploying
3. **Review logs** - Monitor application behavior
4. **Backup database** - Before migrations
5. **Use feature flags** - For gradual rollouts
6. **Monitor metrics** - Set up alerts
7. **Document changes** - Update README and docs
8. **Version control** - Tag releases properly

## 📞 Support

For issues or questions:
- Create a GitHub issue
- Contact the DevOps team
- Check documentation in `/docs`

## 🔗 Useful Links

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Docker Documentation](https://docs.docker.com/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
