#!/bin/bash

# Build script for AutoCare360 Backend

set -e

echo "Building AutoCare360 Backend..."

# Check prerequisites
if ! command -v mvn >/dev/null 2>&1; then
  echo "Error: Maven not installed."
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "Error: Docker not installed."
  exit 1
fi

# Build with Maven
mvn clean package

# Build Docker image
docker-compose --env-file .env up --build -d

echo "Build complete."