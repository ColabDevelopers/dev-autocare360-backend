@echo off
setlocal enabledelayedexpansion

echo =========================================
echo AutoCare360 Deployment Script (Windows)
echo =========================================

REM Environment check
if "%ENVIRONMENT%"=="" (
    echo ERROR: ENVIRONMENT variable not set (dev/staging/prod)
    exit /b 1
)

if "%VERSION%"=="" (
    set VERSION=latest
)

echo Environment: %ENVIRONMENT%
echo Version: %VERSION%
echo.

REM Build the application
echo Step 1: Building application...
call mvnw.cmd clean package -DskipTests
if errorlevel 1 (
    echo ERROR: Build failed
    exit /b 1
)
echo [OK] Build completed
echo.

REM Build Docker image
echo Step 2: Building Docker image...
docker build -t autocare360:%VERSION% .
if errorlevel 1 (
    echo ERROR: Docker build failed
    exit /b 1
)
echo [OK] Docker image built
echo.

REM Push to registry (if not local)
if not "%ENVIRONMENT%"=="local" (
    if "%DOCKER_REGISTRY%"=="" (
        echo ERROR: DOCKER_REGISTRY variable not set
        exit /b 1
    )
    
    echo Step 3: Pushing Docker image to registry...
    docker tag autocare360:%VERSION% %DOCKER_REGISTRY%/autocare360:%VERSION%
    docker push %DOCKER_REGISTRY%/autocare360:%VERSION%
    
    if not "%VERSION%"=="latest" (
        docker tag autocare360:%VERSION% %DOCKER_REGISTRY%/autocare360:latest
        docker push %DOCKER_REGISTRY%/autocare360:latest
    )
    echo [OK] Docker image pushed
    echo.
)

echo =========================================
echo Deployment completed successfully!
echo =========================================

endlocal
