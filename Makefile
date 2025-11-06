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

# Deploy to local development environment
deploy-dev:
	docker-compose up --build -d

# Deploy to production using GitHub (pushes current branch to main, triggering CD workflow)
deploy-prod:
	git push origin HEAD:main