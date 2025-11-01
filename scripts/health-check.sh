#!/bin/bash
# Health check script for AutoCare360 Backend (Linux/Mac)

echo "========================================"
echo "AutoCare360 Backend Health Check"
echo "========================================"

# Check if application is running
if curl -f http://localhost:8080/actuator/health &>/dev/null; then
    echo "Status: HEALTHY"
    echo "Application is running successfully"
    exit 0
else
    echo "Status: UNHEALTHY"
    echo "Application is not responding"
    exit 1
fi

echo "========================================"
