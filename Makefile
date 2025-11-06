# Makefile for AutoCare360 Backend

ROOT_DIR := $(dir $(realpath $(firstword $(MAKEFILE_LIST))))

.PHONY: setup build test lint format clean deploy-dev deploy-down deploy-prod

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

# Format code using Spotless
format:
	mvn spotless:apply

# Clean build artifacts
clean:
	mvn clean
	docker-compose down -v

# Deploy to local development environment using Kubernetes
deploy-dev:
	cd $(ROOT_DIR) && \
	docker build -t ghcr.io/colabdevelopers/dev-autocare360-backend:latest . && \
	kubectl create namespace autocare360 --dry-run=client -o yaml | kubectl apply -f - && \
	kubectl create secret generic autocare360-secrets --from-env-file=.env -n autocare360 --dry-run=client -o yaml | kubectl apply -f - && \
	cd deployment/kubernetes && \
	kubectl apply -k overlays/dev/

# Stop local development deployment
deploy-down:
	cd $(ROOT_DIR)deployment/kubernetes && \
	kubectl delete -k overlays/dev/ && \
	kubectl delete secret autocare360-secrets -n autocare360 --ignore-not-found=true && \
	kubectl delete namespace autocare360 --ignore-not-found=true

# Deploy to production using GitHub (pushes current branch to main, triggering CD workflow)
deploy-prod:
	cd $(ROOT_DIR) && \
	git push origin HEAD:main