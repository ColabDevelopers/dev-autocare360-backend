@echo off
REM Deployment script for AutoCare360 Backend

setlocal enabledelayedexpansion

set ENV=%1
if "%ENV%"=="" set ENV=dev

echo ========================================
echo Deploying AutoCare360 Backend to %ENV%
echo ========================================

REM Load environment variables
if exist .env.%ENV% (
    echo Loading environment variables from .env.%ENV%
    for /f "tokens=*" %%a in (.env.%ENV%) do (
        set %%a
    )
) else (
    echo Warning: .env.%ENV% not found, using default values
)

REM Build Docker image
echo [1/4] Building Docker image...
docker build -t autocare360-backend:%ENV% .

REM Stop existing container
echo [2/4] Stopping existing container...
docker stop autocare360-backend-%ENV% 2>nul
docker rm autocare360-backend-%ENV% 2>nul

REM Run new container
echo [3/4] Starting new container...
docker run -d ^
    --name autocare360-backend-%ENV% ^
    -p 8080:8080 ^
    --env-file .env.%ENV% ^
    autocare360-backend:%ENV%

REM Wait for health check
echo [4/4] Waiting for application to start...
timeout /t 30 /nobreak >nul

REM Check health
curl -f http://localhost:8080/actuator/health 2>nul
if %errorlevel% equ 0 (
    echo ========================================
    echo Deployment successful!
    echo Application is running at http://localhost:8080
    echo ========================================
) else (
    echo ========================================
    echo Deployment may have failed!
    echo Check logs: docker logs autocare360-backend-%ENV%
    echo ========================================
)

endlocal
