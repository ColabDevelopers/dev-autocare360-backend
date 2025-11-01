#!/bin/bash
# Deployment script for AutoCare360 Backend (Linux/Mac)

ENV=${1:-dev}

echo "========================================"
echo "Deploying AutoCare360 Backend to $ENV"
echo "========================================"

# Load environment variables
if [ -f ".env.$ENV" ]; then
    echo "Loading environment variables from .env.$ENV"
    export $(cat .env.$ENV | grep -v '^#' | xargs)
else
    echo "Warning: .env.$ENV not found, using default values"
fi

# Build Docker image
echo "[1/4] Building Docker image..."
docker build -t autocare360-backend:$ENV .

# Stop existing container
echo "[2/4] Stopping existing container..."
docker stop autocare360-backend-$ENV 2>/dev/null || true
docker rm autocare360-backend-$ENV 2>/dev/null || true

# Run new container
echo "[3/4] Starting new container..."
docker run -d \
    --name autocare360-backend-$ENV \
    -p 8080:8080 \
    --env-file .env.$ENV \
    autocare360-backend:$ENV

# Wait for health check
echo "[4/4] Waiting for application to start..."
sleep 30

# Check health
if curl -f http://localhost:8080/actuator/health &>/dev/null; then
    echo "========================================"
    echo "Deployment successful!"
    echo "Application is running at http://localhost:8080"
    echo "========================================"
else
    echo "========================================"
    echo "Deployment may have failed!"
    echo "Check logs: docker logs autocare360-backend-$ENV"
    echo "========================================"
fi
