#!/bin/bash

# Manual Kubernetes deployment script for AutoCare360

set -e

# Configuration (update these)
IMAGE_NAME="autocare360-backend"
TAG="latest"
REGISTRY="ghcr.io/your-username/dev-autocare360-backend"  # Replace with your repo
NAMESPACE="autocare360"

echo "Building and deploying AutoCare360 to Kubernetes..."

# Check prerequisites
if ! kubectl cluster-info >/dev/null 2>&1; then
  echo "Error: kubectl not configured or cluster not accessible."
  exit 1
fi

# Create namespace if it doesn't exist
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Create secrets and configmaps (from .env.example values)
kubectl create secret generic autocare360-secrets \
  --from-literal=mysql-root-password=your_mysql_root_password_here \
  --from-literal=mysql-password=your_mysql_password_here \
  --from-literal=jwt-secret=your_jwt_secret_key_here_minimum_256_bits_long \
  -n $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

kubectl create configmap autocare360-config \
  --from-literal=db-url=jdbc:mysql://autocare360-mysql:3306/autocare360 \
  --from-literal=cors-origins=https://autocare360.vercel.app,http://localhost:3000 \
  -n $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Build and push image
docker build -t $IMAGE_NAME:$TAG .
docker tag $IMAGE_NAME:$TAG $REGISTRY:$TAG
docker push $REGISTRY:$TAG

# Deploy to k8s (use Kustomize for overlays if available)
kubectl apply -k deployment/kubernetes/overlays/production/ -n $NAMESPACE
# Fallback: kubectl apply -f deployment/kubernetes/base/ -n $NAMESPACE

echo "Deployment complete. Check status with: kubectl get pods -n $NAMESPACE"