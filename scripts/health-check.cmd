@echo off
setlocal

set URL=%1
set MAX_ATTEMPTS=%2

if "%URL%"=="" set URL=http://localhost:8080/actuator/health
if "%MAX_ATTEMPTS%"=="" set MAX_ATTEMPTS=30

set ATTEMPT=0

echo =========================================
echo Health Check for AutoCare360
echo =========================================
echo URL: %URL%
echo Max attempts: %MAX_ATTEMPTS%
echo.

:loop
if %ATTEMPT% geq %MAX_ATTEMPTS% goto failed

set /a ATTEMPT=%ATTEMPT%+1

REM Make HTTP request using curl
for /f %%i in ('curl -s -o nul -w "%%{http_code}" "%URL%"') do set HTTP_CODE=%%i

if "%HTTP_CODE%"=="200" (
    echo [OK] Application is healthy! (HTTP %HTTP_CODE%)
    echo.
    echo =========================================
    exit /b 0
)

echo Attempt %ATTEMPT%/%MAX_ATTEMPTS%: Application not ready (HTTP %HTTP_CODE%)
timeout /t 10 /nobreak >nul
goto loop

:failed
echo.
echo [FAIL] Health check failed after %MAX_ATTEMPTS% attempts
echo =========================================
exit /b 1

endlocal
