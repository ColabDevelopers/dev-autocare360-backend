@echo off
REM Build script for AutoCare360 Backend

echo ========================================
echo Building AutoCare360 Backend
echo ========================================

REM Clean and build
echo [1/3] Cleaning previous builds...
call mvnw.cmd clean

echo [2/3] Running tests...
call mvnw.cmd test

echo [3/3] Packaging application...
call mvnw.cmd package -DskipTests

echo ========================================
echo Build completed successfully!
echo JAR file location: target\autocare360-0.0.1-SNAPSHOT.jar
echo ========================================
