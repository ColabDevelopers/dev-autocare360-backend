#!/bin/bash
# Build script for AutoCare360 Backend (Linux/Mac)

echo "========================================"
echo "Building AutoCare360 Backend"
echo "========================================"

# Clean and build
echo "[1/3] Cleaning previous builds..."
./mvnw clean

echo "[2/3] Running tests..."
./mvnw test

echo "[3/3] Packaging application..."
./mvnw package -DskipTests

echo "========================================"
echo "Build completed successfully!"
echo "JAR file location: target/autocare360-0.0.1-SNAPSHOT.jar"
echo "========================================"
