# Deployment Guide

This guide covers deploying the AutoCare360 backend to production environments using Docker and Kubernetes.

## Prerequisites

- Kubernetes cluster (AWS EKS, Google GKE, Azure AKS, or self-hosted)
- kubectl configured to access the cluster
- Docker registry access (GitHub Container Registry used in CI/CD)
- Domain name and SSL certificate (optional)

## GitHub Actions CI/CD Setup

The project uses GitHub Actions for automated CI/CD. The pipelines are configured in `.github/workflows/`.

### Required GitHub Secrets

Set up the following secrets in your GitHub repository (`Settings > Secrets and variables > Actions`):

#### Database Secrets
```
MYSQL_ROOT_PASSWORD     # MySQL root password
MYSQL_DATABASE         # Database name (autocare360)
MYSQL_USER            # MySQL user for application (autocare)
MYSQL_PASSWORD        # MySQL user password
DB_URL               # JDBC connection URL
```

#### Security Secrets
```
JWT_SECRET           # JWT signing secret (min 256 bits)
```

#### Kubernetes Secrets
```
KUBE_CONFIG         # Base64 encoded kubeconfig file
```

### Setting up GitHub Secrets

1. **Generate JWT Secret**:
   ```bash
   # Generate a secure random secret
   openssl rand -hex 32
   ```

2. **Database Credentials**:
   - Use strong, unique passwords
   - Follow your organization's password policy

3. **Kubernetes Configuration**:
   ```bash
   # Get kubeconfig from your cluster
   kubectl config view --flatten --minify > kubeconfig.yaml

   # Base64 encode it
   cat kubeconfig.yaml | base64 -w 0
   ```

4. **Add to GitHub Secrets**:
   - Go to repository Settings
   - Navigate to Secrets and variables > Actions
   - Click "New repository secret"
   - Add each secret with its value

## Deployment Architecture

### Components

- **Application**: Spring Boot containerized app
- **Database**: MySQL 8.0 with persistent volume
- **Ingress**: NGINX ingress controller for external access
- **Secrets**: Kubernetes secrets for sensitive data
- **ConfigMaps**: Configuration data
- **RBAC**: Service account with minimal permissions

### Network Flow

```
Internet → Ingress → Service → Pods → Database
```

## Manual Deployment

### 1. Build and Push Docker Image

```bash
# Build the application
mvn clean package -DskipTests

# Build Docker image
docker build -t ghcr.io/yourusername/autocare360-backend:latest .

# Push to registry
docker push ghcr.io/yourusername/autocare360-backend:latest
```

### 2. Deploy to Kubernetes

```bash
# Apply base manifests
kubectl apply -f deployment/kubernetes/base/

# Apply network components
kubectl apply -f deployment/kubernetes/network/

# Apply RBAC
kubectl apply -f deployment/kubernetes/RBAC/

# Apply volumes
kubectl apply -f deployment/kubernetes/volumes/
```

### 3. Create Secrets

```bash
# Create database secrets
kubectl create secret generic autocare360-secrets \
  --from-literal=root-password='your-root-password' \
  --from-literal=user='autocare' \
  --from-literal=password='your-password' \
  --from-literal=database='autocare360' \
  --from-literal=db-url='jdbc:mysql://autocare360-mysql:3306/autocare360' \
  --from-literal=jwt-secret='your-256-bit-jwt-secret' \
  -n autocare360
```

### 4. Verify Deployment

```bash
# Check pod status
kubectl get pods -n autocare360

# Check services
kubectl get svc -n autocare360

# Check ingress
kubectl get ingress -n autocare360

# View logs
kubectl logs -f deployment/autocare360-app -n autocare360
```

## Environment-Specific Deployments

### Development Environment

```bash
# Apply dev overlay
kubectl apply -k deployment/kubernetes/overlays/dev/
```

### Production Environment

```bash
# Apply prod overlay
kubectl apply -k deployment/kubernetes/overlays/prod/
```

## Configuration Management

### ConfigMaps

Application configuration is managed through ConfigMaps:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: autocare360-config
  namespace: autocare360
data:
  SPRING_PROFILES_ACTIVE: "prod"
  JAVA_OPTS: "-Xmx512m -Xms256m"
```

### Secrets

Sensitive data is stored in Kubernetes secrets:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: autocare360-secrets
  namespace: autocare360
type: Opaque
data:
  jwt-secret: <base64-encoded-secret>
  db-password: <base64-encoded-password>
```

## Database Management

### Initial Setup

The database is automatically initialized with Flyway migrations on pod startup.

### Backup Strategy

```bash
# Create database backup
kubectl exec -n autocare360 deployment/autocare360-mysql -- \
  mysqldump -u root -p autocare360 > backup.sql

# Copy backup to local machine
kubectl cp autocare360/autocare360-mysql-xxxxx:backup.sql ./backup.sql
```

### Restore

```bash
# Copy backup to pod
kubectl cp ./backup.sql autocare360/autocare360-mysql-xxxxx:/tmp/backup.sql

# Restore database
kubectl exec -n autocare360 deployment/autocare360-mysql -- \
  mysql -u root -p autocare360 < /tmp/backup.sql
```

## Monitoring and Logging

### Health Checks

The application exposes health endpoints:

```bash
# Health check
curl https://your-domain.com/actuator/health

# Metrics
curl https://your-domain.com/actuator/metrics

# Prometheus metrics
curl https://your-domain.com/actuator/prometheus
```

### Logging

```bash
# View application logs
kubectl logs -f deployment/autocare360-app -n autocare360

# View database logs
kubectl logs -f deployment/autocare360-mysql -n autocare360
```

## Scaling

### Horizontal Pod Autoscaling

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: autocare360-hpa
  namespace: autocare360
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: autocare360-app
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

### Database Scaling

For high-traffic applications, consider:
- Read replicas
- Connection pooling (HikariCP configured)
- Database sharding

## Security Considerations

### Network Security

- Use internal services for database access
- Configure network policies
- Enable pod security standards

### Secret Management

- Rotate secrets regularly
- Use external secret managers (AWS Secrets Manager, Vault)
- Never log sensitive information

### SSL/TLS

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: autocare360-ingress
  namespace: autocare360
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - your-domain.com
    secretName: autocare360-tls
  rules:
  - host: your-domain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: autocare360-app
            port:
              number: 8080
```

## Troubleshooting Deployment

### Common Issues

1. **Pods not starting**:
   ```bash
   kubectl describe pod <pod-name> -n autocare360
   ```

2. **Database connection issues**:
   - Check secrets are correctly mounted
   - Verify service discovery

3. **Ingress not working**:
   - Check ingress controller installation
   - Verify DNS configuration

### Rollback Strategy

```bash
# Rollback deployment
kubectl rollout undo deployment/autocare360-app -n autocare360

# Check rollout status
kubectl rollout status deployment/autocare360-app -n autocare360
```

## Cost Optimization

- Use spot instances for non-critical workloads
- Implement pod disruption budgets
- Set up resource limits and requests
- Use cluster autoscaling

## Maintenance

### Regular Tasks

- Update base images monthly
- Rotate credentials quarterly
- Review and update dependencies
- Monitor resource usage

### Updates

```bash
# Update application
kubectl set image deployment/autocare360-app \
  autocare360-app=ghcr.io/yourusername/autocare360-backend:v1.1.0 \
  -n autocare360

# Check rollout
kubectl rollout status deployment/autocare360-app -n autocare360
```

For additional support, refer to the [Development Guide](DEVELOPMENT.md) or create an issue in the repository.