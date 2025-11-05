# Makefile for AutoCare360 Backend

.PHONY: setup build test lint clean deploy

# Setup local development environment
setup:
	docker-compose up --build -d

# Build the application
build:
	mvn clean package

# Run tests
test:
	mvn test

# Lint code (assuming Spotless or similar is configured)
lint:
	mvn spotless:check

# Clean build artifacts
clean:
	mvn clean
	docker-compose down -v

# Deploy to Kubernetes
deploy:
	kubectl apply -f deployment/kubernetes/

# Stop local environment
stop:
	docker-compose down