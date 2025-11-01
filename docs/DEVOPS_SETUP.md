# DevOps Setup Instructions

## Quick Start Commands

### 1. Local Development with Docker Compose

```bash
# Start all services (MySQL, Redis, App, Prometheus, Grafana)
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down

# Remove all data
docker-compose down -v
```

### 2. Build Docker Image

```bash
# Build the Docker image
docker build -t autocare360:latest .

# Run the container
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev autocare360:latest
```

### 3. Manual Deployment

```bash
# Build the application
mvnw clean package

# Run the JAR
java -jar target/autocare360-0.0.1-SNAPSHOT.jar
```

## GitHub Actions Setup

### Required Secrets

Set these secrets in your GitHub repository settings (`Settings > Secrets and variables > Actions`):

```
DOCKER_USERNAME         # Your Docker Hub username
DOCKER_PASSWORD         # Your Docker Hub password or access token
HEROKU_API_KEY         # Heroku API key (for staging)
HEROKU_EMAIL           # Your Heroku email
HEROKU_APP_NAME_STAGING # Heroku app name for staging
PRODUCTION_URL         # Production URL for smoke tests
INFRA_REPO            # Infrastructure repository (optional)
INFRA_PAT             # GitHub PAT for infrastructure repo (optional)
```

### Workflow Triggers

- **CI Pipeline** (`ci.yml`):
  - Runs on push to: `develop`, `feature/*`, `hotfix/*`
  - Runs on PRs to: `main`, `develop`
  - Executes: Tests → Security Scan → Build & Push

- **CD Pipeline** (`cd.yml`):
  - Runs on push to: `main`
  - Executes: Deploy to Production + Smoke Tests

## AWS Deployment with Terraform

### Prerequisites

```bash
# Install Terraform
choco install terraform  # Windows
# or
brew install terraform   # Mac/Linux

# Configure AWS credentials
aws configure
```

### Deploy Infrastructure

```bash
cd infrastructure/terraform

# Copy and edit variables
copy terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your values

# Initialize Terraform
terraform init

# Preview changes
terraform plan

# Apply changes
terraform apply
```

### Infrastructure Outputs

After deployment, Terraform will output:
- EKS Cluster endpoint
- RDS endpoint (sensitive)
- Redis endpoint (sensitive)

## Kubernetes Deployment

### Prerequisites

```bash
# Install kubectl
choco install kubernetes-cli  # Windows

# Configure kubectl to use EKS cluster
aws eks update-kubeconfig --name autocare360-cluster --region us-east-1
```

### Deploy Application

```bash
# Create namespace
kubectl create namespace autocare360-prod

# Create secrets from your .env file
kubectl create secret generic autocare360-secrets \
  --from-literal=db-url=jdbc:mysql://YOUR_RDS_ENDPOINT:3306/autocare360 \
  --from-literal=db-username=admin \
  --from-literal=db-password=YOUR_PASSWORD \
  --from-literal=jwt-secret=YOUR_JWT_SECRET \
  -n autocare360-prod

# Deploy using Kustomize
kubectl apply -k infrastructure/kubernetes/base -n autocare360-prod

# Check deployment status
kubectl get pods -n autocare360-prod
kubectl get svc -n autocare360-prod
kubectl get ingress -n autocare360-prod

# View logs
kubectl logs -f deployment/autocare360 -n autocare360-prod

# Check rollout status
kubectl rollout status deployment/autocare360 -n autocare360-prod
```

### Scaling

```bash
# Manual scaling
kubectl scale deployment/autocare360 --replicas=5 -n autocare360-prod

# HorizontalPodAutoscaler is configured automatically
kubectl get hpa -n autocare360-prod
```

### Rolling Updates

```bash
# Update image
kubectl set image deployment/autocare360 \
  autocare360=your-docker-username/autocare360:v1.0.1 \
  -n autocare360-prod

# Check rollout
kubectl rollout status deployment/autocare360 -n autocare360-prod

# Rollback if needed
kubectl rollout undo deployment/autocare360 -n autocare360-prod
```

## Monitoring Setup

### Prometheus

Prometheus is automatically configured in `docker-compose.yml` and scrapes metrics from:
- Application: `http://app:8080/actuator/prometheus`
- MySQL (if exporter is added)
- Redis (if exporter is added)

Access: http://localhost:9090

### Grafana

Access: http://localhost:3000
- Username: `admin`
- Password: `admin`

**Import Dashboards:**
1. Go to Dashboards → Import
2. Use these dashboard IDs:
   - **4701** - JVM Micrometer
   - **11378** - Spring Boot Statistics
   - **12900** - Spring Boot Observability

**Or create custom queries:**
```promql
# HTTP request rate
rate(http_server_requests_seconds_count[5m])

# JVM memory usage
jvm_memory_used_bytes{area="heap"}

# Database connection pool
hikaricp_connections_active
```

## Health Checks

### Manual Health Checks

```bash
# Basic health
curl http://localhost:8080/actuator/health

# Detailed health
curl http://localhost:8080/actuator/health | json_pp

# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness

# Metrics
curl http://localhost:8080/actuator/metrics

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

### Automated Health Checks

```bash
# Using the health-check script
scripts\health-check.cmd http://localhost:8080/actuator/health 30
```

## Database Management

### Flyway Migrations

```bash
# Run migrations
mvnw flyway:migrate

# Check migration status
mvnw flyway:info

# Validate migrations
mvnw flyway:validate

# Repair (if needed)
mvnw flyway:repair
```

### Backup & Restore

```bash
# Backup MySQL
docker exec autocare360-mysql mysqldump -u root -proot autocare360 > backup.sql

# Restore MySQL
docker exec -i autocare360-mysql mysql -u root -proot autocare360 < backup.sql
```

## Troubleshooting

### Common Issues

**1. Port already in use**
```bash
# Windows - Find process using port 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Or change port in application.properties
server.port=8081
```

**2. Docker build fails**
```bash
# Clean Docker cache
docker system prune -a

# Rebuild without cache
docker-compose build --no-cache
```

**3. Database connection errors**
```bash
# Check MySQL is running
docker ps | findstr mysql

# Check MySQL logs
docker logs autocare360-mysql

# Connect to MySQL directly
docker exec -it autocare360-mysql mysql -u root -proot
```

**4. JWT token errors**
```bash
# Ensure JWT_SECRET is set
echo %JWT_SECRET%

# Generate a new strong secret (PowerShell)
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

### Logs

```bash
# Docker Compose logs
docker-compose logs -f app
docker-compose logs -f mysql

# Kubernetes logs
kubectl logs -f deployment/autocare360 -n autocare360-prod

# Application logs (if file logging enabled)
tail -f logs/autocare360.log
```

## Performance Tuning

### JVM Options

Add to `JAVA_OPTS` in Dockerfile or deployment:
```
-Xms512m -Xmx1024m
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+HeapDumpOnOutOfMemoryError
```

### Database Optimization

Edit `application-prod.yml`:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
```

### Kubernetes Resources

Edit `infrastructure/kubernetes/base/deployment.yaml`:
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "2Gi"
    cpu: "1000m"
```

## Security Best Practices

1. **Never commit secrets** - Use `.env` files (gitignored)
2. **Rotate JWT secrets** regularly in production
3. **Use strong database passwords** - Generate with password managers
4. **Enable HTTPS** in production - Use cert-manager with Let's Encrypt
5. **Regularly update dependencies** - Run `mvn versions:display-dependency-updates`
6. **Scan for vulnerabilities** - CI pipeline includes OWASP and Trivy scans

## Next Steps

1. ✅ Set up GitHub Actions secrets
2. ✅ Configure AWS credentials for Terraform
3. ✅ Deploy infrastructure with Terraform
4. ✅ Deploy application to Kubernetes
5. ✅ Configure domain and SSL certificates
6. ✅ Set up monitoring alerts in Grafana
7. ✅ Configure backup strategy for production database
8. ✅ Set up log aggregation (ELK/Loki)
9. ✅ Configure API rate limiting
10. ✅ Implement comprehensive testing strategy

## Support

- **Documentation**: `docs/` directory
- **Issues**: GitHub Issues
- **Email**: support@autocare360.com
