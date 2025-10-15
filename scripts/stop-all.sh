#!/bin/bash

echo "🛑 Stopping ALL Microservices..."
echo "================================="

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "❌ ERROR: Please run this script from the project root directory"
    exit 1
fi

# Create logs directory if it doesn't exist
mkdir -p logs

echo "🔍 Looking for running services..."

services_stopped=0

# Stop services in reverse order
for service in "employee-service" "auth-service" "api-gateway" "config-server" "discovery-service"; do
    pid_file="logs/$service.pid"
    
    if [ -f "$pid_file" ]; then
        pid=$(cat $pid_file)
        if ps -p $pid > /dev/null 2>&1; then
            echo "⏹️  Stopping $service (PID: $pid)..."
            kill $pid
            sleep 2
            
            # Force kill if still running
            if ps -p $pid > /dev/null 2>&1; then
                echo "⚠️  Force stopping $service..."
                kill -9 $pid
            fi
            
            rm $pid_file
            echo "✅ $service stopped"
            ((services_stopped++))
        else
            echo "🧹 Cleaning up stale PID file for $service"
            rm $pid_file
        fi
    else
        echo "ℹ️  $service not running (no PID file)"
    fi
done

# Clean up log files
echo ""
echo "🧹 Cleaning up logs..."
if [ -d "logs" ]; then
    rm -f logs/*.log
    echo "✅ Log files cleaned"
fi

echo ""
echo "================================="
if [ $services_stopped -gt 0 ]; then
    echo "✅ Stopped $services_stopped services"
else
    echo "ℹ️  No services were running"
fi
echo "================================="
