#!/bin/bash

set -e

echo "========================================="
echo "AutoCare360 Deployment Script"
echo "========================================="

# Environment check
if [ -z "$ENVIRONMENT" ]; then
    echo "ERROR: ENVIRONMENT variable not set (dev/staging/prod)"
    exit 1
fi

if [ -z "$VERSION" ]; then
    VERSION="latest"
fi

echo "Environment: $ENVIRONMENT"
echo "Version: $VERSION"
echo ""

# Build the application
echo "Step 1: Building application..."
./mvnw clean package -DskipTests
echo "✓ Build completed"
echo ""

# Run database migrations
echo "Step 2: Running database migrations..."
./mvnw flyway:migrate -Dflyway.configFiles=src/main/resources/application-${ENVIRONMENT}.yml
echo "✓ Migrations completed"
echo ""

# Build Docker image
echo "Step 3: Building Docker image..."
docker build -t autocare360:$VERSION .
echo "✓ Docker image built"
echo ""

# Push to registry (if not local)
if [ "$ENVIRONMENT" != "local" ]; then
    if [ -z "$DOCKER_REGISTRY" ]; then
        echo "ERROR: DOCKER_REGISTRY variable not set"
        exit 1
    fi
    
    echo "Step 4: Pushing Docker image to registry..."
    docker tag autocare360:$VERSION $DOCKER_REGISTRY/autocare360:$VERSION
    docker push $DOCKER_REGISTRY/autocare360:$VERSION
    
    if [ "$VERSION" != "latest" ]; then
        docker tag autocare360:$VERSION $DOCKER_REGISTRY/autocare360:latest
        docker push $DOCKER_REGISTRY/autocare360:latest
    fi
    echo "✓ Docker image pushed"
    echo ""
fi

# Deploy to Kubernetes (if production)
if [ "$ENVIRONMENT" == "prod" ]; then
    echo "Step 5: Deploying to Kubernetes..."
    kubectl set image deployment/autocare360 autocare360=$DOCKER_REGISTRY/autocare360:$VERSION -n autocare360-prod
    kubectl rollout status deployment/autocare360 -n autocare360-prod
    echo "✓ Kubernetes deployment completed"
    echo ""
fi

echo "========================================="
echo "Deployment completed successfully!"
echo "========================================="

# Health check
if [ "$ENVIRONMENT" != "local" ]; then
    echo ""
    echo "Running health check..."
    sleep 10
    ./scripts/health-check.sh
fi
