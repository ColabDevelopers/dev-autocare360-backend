# Kubernetes Deployment Guide

This guide covers deploying the AutoCare360 backend to Kubernetes clusters.

## Prerequisites

- Kubernetes cluster (v1.24+)
- kubectl configured
- Helm (optional, for advanced deployments)
- Docker registry access

## Cluster Setup

### Local Development

#### Minikube

```bash
# Start Minikube
minikube start --kubernetes-version=v1.28.0

# Enable ingress
minikube addons enable ingress

# Get cluster info
kubectl cluster-info
```

#### k3s

```bash
# Install k3s
curl -sfL https://get.k3s.io | sh -

# Get kubeconfig
sudo cat /etc/rancher/k3s/k3s.yaml
```

### Cloud Providers

#### AWS EKS

```bash
# Install eksctl
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin

# Create cluster
eksctl create cluster -f cluster.yaml

# Update kubeconfig
aws eks update-kubeconfig --region region --name cluster-name
```

#### Google GKE

```bash
# Install gcloud SDK
# Create cluster
gcloud container clusters create autocare360-cluster \
  --num-nodes=3 \
  --zone=us-central1-a

# Get credentials
gcloud container clusters get-credentials autocare360-cluster
```

#### Azure AKS

```bash
# Create resource group
az group create --name autocare360-rg --location eastus

# Create cluster
az aks create --resource-group autocare360-rg \
  --name autocare360-cluster \
  --node-count 3 \
  --enable-addons monitoring \
  --generate-ssh-keys

# Get credentials
az aks get-credentials --resource-group autocare360-rg --name autocare360-cluster
```

## Namespace Setup

```bash
# Create namespace
kubectl create namespace autocare360

# Set as default
kubectl config set-context --current --namespace=autocare360
```

## Secrets Management

### Create Secrets

```bash
# Database secrets
kubectl create secret generic autocare360-secrets \
  --from-literal=root-password='your-root-password' \
  --from-literal=user='autocare' \
  --from-literal=password='your-password' \
  --from-literal=database='autocare360'

# JWT secret
kubectl create secret generic autocare360-secrets \
  --from-literal=jwt-secret='your-256-bit-jwt-secret'

# Database URL
kubectl create secret generic autocare360-secrets \
  --from-literal=db-url='jdbc:mysql://autocare360-mysql:3306/autocare360'
```

### Using External Secret Managers

#### AWS Secrets Manager

```yaml
apiVersion: external-secrets.io/v1beta1
kind: SecretStore
metadata:
  name: aws-secretsmanager
spec:
  provider:
    aws:
      service: SecretsManager
      region: us-east-1
      auth:
        jwt:
          serviceAccountRef:
            name: external-secrets-sa
```

#### HashiCorp Vault

```yaml
apiVersion: external-secrets.io/v1beta1
kind: SecretStore
metadata:
  name: vault-backend
spec:
  provider:
    vault:
      server: "http://vault.example.com:8200"
      path: "secret"
      auth:
        kubernetes:
          mountPath: "/v1/auth/kubernetes"
          role: "demo"
```

## Application Deployment

### Base Deployment

```bash
# Apply base manifests
kubectl apply -f deployment/kubernetes/base/

# Verify deployment
kubectl get pods
kubectl get services
kubectl get ingress
```

### Using Kustomize

```bash
# Apply base configuration
kubectl apply -k deployment/kubernetes/base/

# Apply development overlay
kubectl apply -k deployment/kubernetes/overlays/dev/

# Apply production overlay
kubectl apply -k deployment/kubernetes/overlays/prod/
```

## Database Deployment

### MySQL StatefulSet

The project uses a MySQL deployment defined in `deployment/kubernetes/base/db-deployment.yaml`.

### Persistent Volume

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
  storageClassName: standard
```

## Networking

### Services

```yaml
# ClusterIP service (internal)
apiVersion: v1
kind: Service
metadata:
  name: autocare360-backend
spec:
  selector:
    app: autocare360-backend
  ports:
  - port: 8080
    targetPort: 8080
  type: ClusterIP

# LoadBalancer service (external)
apiVersion: v1
kind: Service
metadata:
  name: autocare360-backend-lb
spec:
  selector:
    app: autocare360-backend
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

### Ingress

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: autocare360-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - api.autocare360.com
    secretName: autocare360-tls
  rules:
  - host: api.autocare360.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: autocare360-backend
            port:
              number: 8080
```

## Configuration Management

### ConfigMaps

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: autocare360-config
data:
  SPRING_PROFILES_ACTIVE: "prod"
```

### Environment Variables

```yaml
env:
- name: SPRING_PROFILES_ACTIVE
  value: "prod"
- name: DB_URL
  valueFrom:
    secretKeyRef:
      name: autocare360-secrets
      key: db-url
- name: JWT_SECRET
  valueFrom:
    secretKeyRef:
      name: autocare360-secrets
      key: jwt-secret
```

## RBAC and Security

### Service Account

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: autocare360-sa
  namespace: autocare360
```

### Role and RoleBinding

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: autocare360-role
  namespace: autocare360
rules:
- apiGroups: [""]
  resources: ["pods", "services", "configmaps"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]

---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: autocare360-rolebinding
  namespace: autocare360
subjects:
- kind: ServiceAccount
  name: autocare360-sa
roleRef:
  kind: Role
  name: autocare360-role
  apiGroup: rbac.authorization.k8s.io
```

## Monitoring and Logging

### Prometheus Metrics

The application exposes metrics at `/actuator/prometheus`.

### Logging with Fluent Bit

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: fluent-bit-config
data:
  fluent-bit.conf: |
    [SERVICE]
        Flush         5
        Log_Level     info
        Daemon        off

    [INPUT]
        Name              tail
        Path              /var/log/containers/*autocare360*.log
        Parser            docker
        Tag               app.*
        Refresh_Interval  5

    [OUTPUT]
        Name  stdout
        Match *
        Format json_lines
```

## Scaling

### Horizontal Pod Autoscaling

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: autocare360-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: autocare360-backend
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### Cluster Autoscaling

```yaml
apiVersion: autoscaling/v1
kind: ClusterAutoscaler
metadata:
  name: cluster-autoscaler
  namespace: kube-system
spec:
  scaleDownDelayAfterAdd: 10m
  scaleDownDelayAfterDelete: 10s
  scaleDownDelayAfterFailure: 3m
  scaleDownDelayAfterFailure: 3m
  scaleDownUnneededTime: 10m
  scaleDownUtilizationThreshold: 0.5
  scanInterval: 10s
  skipNodesWithLocalStorage: true
  skipNodesWithSystemPods: true
```

## Backup and Recovery

### Database Backup

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: mysql-backup
spec:
  schedule: "0 2 * * *"  # Daily at 2 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: mysql:8.0
            command:
            - /bin/bash
            - -c
            - |
              mysqldump -h autocare360-mysql -u root -p$MYSQL_ROOT_PASSWORD autocare360 > /backup/backup.sql
            env:
            - name: MYSQL_ROOT_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: autocare360-secrets
                  key: root-password
            volumeMounts:
            - name: backup-storage
              mountPath: /backup
          volumes:
          - name: backup-storage
            persistentVolumeClaim:
              claimName: backup-pvc
          restartPolicy: OnFailure
```

### Disaster Recovery

```bash
# Scale down application
kubectl scale deployment autocare360-backend --replicas=0

# Restore database
kubectl exec -it deployment/mysql-backup -- \
  mysql -h autocare360-mysql -u root -p$MYSQL_ROOT_PASSWORD autocare360 < /backup/backup.sql

# Scale up application
kubectl scale deployment autocare360-backend --replicas=3
```

## Troubleshooting

### Common Issues

1. **Pods not starting**:
   ```bash
   kubectl describe pod <pod-name>
   kubectl logs <pod-name>
   ```

2. **Service not accessible**:
   ```bash
   kubectl get endpoints
   kubectl describe service autocare360-backend
   ```

3. **Ingress not working**:
   ```bash
   kubectl get ingress
   kubectl describe ingress autocare360-ingress
   ```

4. **Resource constraints**:
   ```bash
   kubectl top pods
   kubectl top nodes
   ```

### Debugging Commands

```bash
# Get cluster status
kubectl cluster-info

# Check node status
kubectl get nodes

# View events
kubectl get events --sort-by=.metadata.creationTimestamp

# Debug pod
kubectl exec -it <pod-name> -- /bin/bash

# Port forward for local access
kubectl port-forward svc/autocare360-backend 8080:8080
```

### Logs

```bash
# Application logs
kubectl logs -f deployment/autocare360-backend

# Previous container logs
kubectl logs -f deployment/autocare360-backend --previous

# All pods logs
kubectl logs -f -l app=autocare360-backend
```

## Performance Optimization

### Resource Limits

```yaml
resources:
  requests:
    memory: "256Mi"
    cpu: "250m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

### JVM Tuning

```yaml
env:
- name: JAVA_OPTS
  value: "-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
```

### Database Optimization

```yaml
env:
- name: MYSQL_INNODB_BUFFER_POOL_SIZE
  value: "128M"
- name: MYSQL_INNODB_LOG_FILE_SIZE
  value: "32M"
```

## CI/CD Integration

### GitHub Actions

The project uses GitHub Actions for automated deployment. The CD workflow:

1. Builds the application with Maven
2. Builds and pushes Docker image to GHCR
3. Deploys to Kubernetes using the provided manifests
4. Creates secrets dynamically in the cluster

### ArgoCD (GitOps)

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: autocare360
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/ColabDevelopers/dev-autocare360-backend
    path: deployment/kubernetes
    targetRevision: HEAD
  destination:
    server: https://kubernetes.default.svc
    namespace: autocare360
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
```

For more information, see the [Deployment Guide](DEPLOYMENT.md) and [Docker Guide](DOCKER.md).