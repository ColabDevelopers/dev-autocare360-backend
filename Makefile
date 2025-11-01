# Makefile for AutoCare360 Backend

.PHONY: help build test clean run docker-build docker-run deploy health-check

# Default target
help:
	@echo "AutoCare360 Backend - Available Commands"
	@echo "=========================================="
	@echo "make build         - Build the application"
	@echo "make test          - Run tests"
	@echo "make clean         - Clean build artifacts"
	@echo "make run           - Run the application locally"
	@echo "make docker-build  - Build Docker image"
	@echo "make docker-run    - Run with Docker Compose"
	@echo "make docker-stop   - Stop Docker containers"
	@echo "make deploy        - Deploy application"
	@echo "make health-check  - Check application health"
	@echo "make k8s-deploy    - Deploy to Kubernetes"
	@echo "make k8s-delete    - Delete from Kubernetes"

# Build application
build:
	@echo "Building application..."
	./mvnw clean package -DskipTests

# Run tests
test:
	@echo "Running tests..."
	./mvnw test

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	./mvnw clean

# Run application locally
run:
	@echo "Running application..."
	./mvnw spring-boot:run

# Build Docker image
docker-build:
	@echo "Building Docker image..."
	docker build -t autocare360-backend:latest .

# Run with Docker Compose
docker-run:
	@echo "Starting services with Docker Compose..."
	docker-compose up -d
	@echo "Waiting for services to be ready..."
	@sleep 30
	@make health-check

# Stop Docker containers
docker-stop:
	@echo "Stopping Docker containers..."
	docker-compose down

# Deploy application (local Docker)
deploy:
	@echo "Deploying application..."
	@if [ -f scripts/deploy.sh ]; then \
		chmod +x scripts/deploy.sh; \
		./scripts/deploy.sh; \
	else \
		echo "Deploy script not found"; \
		exit 1; \
	fi

# Health check
health-check:
	@echo "Checking application health..."
	@curl -f http://localhost:8080/actuator/health || (echo "Health check failed" && exit 1)
	@echo "Application is healthy!"

# Deploy to Kubernetes
k8s-deploy:
	@echo "Deploying to Kubernetes..."
	kubectl apply -f kubernetes/namespace.yaml
	kubectl apply -f kubernetes/secret.yaml
	kubectl apply -f kubernetes/configmap.yaml
	kubectl apply -f kubernetes/deployment.yaml
	kubectl apply -f kubernetes/ingress.yaml
	kubectl apply -f kubernetes/hpa.yaml
	@echo "Deployment complete!"

# Delete from Kubernetes
k8s-delete:
	@echo "Deleting from Kubernetes..."
	kubectl delete -f kubernetes/ || true
	@echo "Deletion complete!"

# View logs
logs:
	@echo "Viewing application logs..."
	docker-compose logs -f app

# Database migration
migrate:
	@echo "Running database migrations..."
	./mvnw flyway:migrate
