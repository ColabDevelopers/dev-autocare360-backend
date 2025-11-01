@echo off
REM Health check script for AutoCare360 Backend

echo ========================================
echo AutoCare360 Backend Health Check
echo ========================================

REM Check if application is running
curl -f http://localhost:8080/actuator/health 2>nul
if %errorlevel% equ 0 (
    echo Status: HEALTHY
    echo Application is running successfully
) else (
    echo Status: UNHEALTHY
    echo Application is not responding
    exit /b 1
)

echo ========================================
