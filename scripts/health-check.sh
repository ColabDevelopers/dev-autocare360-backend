#!/bin/bash

URL=${1:-http://localhost:8080/actuator/health}
MAX_ATTEMPTS=${2:-30}
ATTEMPT=0

echo "========================================="
echo "Health Check for AutoCare360"
echo "========================================="
echo "URL: $URL"
echo "Max attempts: $MAX_ATTEMPTS"
echo ""

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    ATTEMPT=$((ATTEMPT + 1))
    
    # Make HTTP request and capture status code
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$URL" 2>/dev/null)
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo "✓ Application is healthy! (HTTP $HTTP_CODE)"
        
        # Get detailed health info
        HEALTH_RESPONSE=$(curl -s "$URL" 2>/dev/null)
        echo ""
        echo "Health Details:"
        echo "$HEALTH_RESPONSE" | grep -o '"status":"[^"]*"' || echo "$HEALTH_RESPONSE"
        echo ""
        echo "========================================="
        exit 0
    fi
    
    echo "Attempt $ATTEMPT/$MAX_ATTEMPTS: Application not ready (HTTP $HTTP_CODE)"
    
    # Wait before next attempt
    sleep 10
done

echo ""
echo "✗ Health check failed after $MAX_ATTEMPTS attempts"
echo "========================================="
exit 1
