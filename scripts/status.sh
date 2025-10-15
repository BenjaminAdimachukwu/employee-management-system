#!/bin/bash

echo "📊 Microservices Status"
echo "======================"

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "❌ ERROR: Please run this script from the project root directory"
    exit 1
fi

mkdir -p logs

echo ""
echo "🔍 Checking services..."
echo ""

services_running=0

check_service() {
    local service_name=$1
    local port=$2
    local url=$3
    
    pid_file="logs/$service_name.pid"
    
    if [ -f "$pid_file" ]; then
        pid=$(cat $pid_file)
        if ps -p $pid > /dev/null 2>&1; then
            if nc -z localhost $port 2>/dev/null; then
                echo "✅ $service_name: RUNNING (PID: $pid, Port: $port)"
                echo "   🔗 $url"
                ((services_running++))
            else
                echo "⚠️  $service_name: PROCESS RUNNING but port $port not responding"
            fi
        else
            echo "❌ $service_name: PID FILE EXISTS but process not running"
        fi
    else
        echo "❌ $service_name: NOT RUNNING"
    fi
}

check_service "discovery-service" "8761" "http://localhost:8761"
check_service "config-server" "8888" "http://localhost:8888"
check_service "api-gateway" "8080" "http://localhost:8080"
check_service "auth-service" "8081" "http://localhost:8081"
check_service "employee-service" "8082" "http://localhost:8082"

echo ""
echo "======================"
echo "📈 Summary: $services_running/4 services running"
echo ""
echo "💡 Commands:"
echo "   ./scripts/start-all.sh - Start all services"
echo "   ./scripts/stop-all.sh  - Stop all services"
echo "   ./scripts/status.sh    - Show this status"
echo ""
