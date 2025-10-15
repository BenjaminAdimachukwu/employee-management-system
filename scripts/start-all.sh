
#!/bin/bash

echo "🚀 Starting ALL Microservices Automatically..."
echo "==============================================="

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "❌ ERROR: Please run this script from the project root directory"
    echo "💡 Current directory: $(pwd)"
    exit 1
fi

# Load environment variables
if [ -f .env ]; then
    echo "📁 Loading environment variables from .env"
    export $(cat .env | grep -v '^#' | xargs)
    echo "✅ Environment variables loaded"
else
    echo "❌ ERROR: .env file not found in $(pwd)"
    echo "💡 Create .env file from .env.example"
    exit 1
fi

# Validate critical variables
if [ -z "$JWT_SECRET" ]; then
    echo "❌ ERROR: JWT_SECRET not set in .env"
    exit 1
fi

echo ""
echo "🔍 Environment Check:"
echo "   - DB_USERNAME: $DB_USERNAME"
echo "   - DB_NAME: $DB_NAME"
echo "   - JWT_SECRET: ${JWT_SECRET:0:10}..."
echo ""

# Create logs directory
mkdir -p logs
echo "📁 Created logs directory"

# Function to check if a service is running
is_service_running() {
    local port=$1
    local service_name=$2
    
    if nc -z localhost $port 2>/dev/null; then
        echo "✅ $service_name is running on port $port"
        return 0
    else
        echo "⏳ $service_name not ready on port $port..."
        return 1
    fi
}

# Function to wait for service to start
wait_for_service() {
    local port=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    echo "⏳ Waiting for $service_name to start..."
    
    while [ $attempt -le $max_attempts ]; do
        if is_service_running $port "$service_name"; then
            echo "✅ $service_name is ready!"
            return 0
        fi
        echo "   Attempt $attempt/$max_attempts - waiting 5 seconds..."
        sleep 5
        ((attempt++))
    done
    
    echo "❌ ERROR: $service_name failed to start within timeout"
    return 1
}

# Function to start a service
start_service() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    
    echo ""
    echo "🔧 Starting $service_name..."
    
    # Check if already running
    if [ -f "logs/$service_name.pid" ]; then
        local pid=$(cat logs/$service_name.pid)
        if ps -p $pid > /dev/null 2>&1; then
            echo "⚠️  $service_name is already running (PID: $pid)"
            return 0
        fi
    fi
    
    cd $service_dir
    
    # Start service in background
    nohup mvn spring-boot:run > ../logs/$service_name.log 2>&1 &
    local pid=$!
    
    echo $pid > ../logs/$service_name.pid
    echo "✅ $service_name started (PID: $pid)"
    
    cd ..
    
    # Wait for service to be ready
    if [ -n "$port" ]; then
        wait_for_service $port "$service_name"
    fi
}

# Function to build a service
build_service() {
    local service_name=$1
    local service_dir=$2
    
    echo "🔨 Building $service_name..."
    cd $service_dir
    if mvn clean compile -q; then
        echo "✅ $service_name built successfully"
    else
        echo "❌ ERROR: Failed to build $service_name"
        exit 1
    fi
    cd ..
}

echo ""
echo "📦 Building all services..."
echo "---------------------------"

# Build all services
build_service "discovery-service" "discovery-service"
build_service "config-server" "config-server"
build_service "api-gateway" "api-gateway" 
build_service "auth-service" "auth-service"
build_service "employee-service" "employee-service"  # ← ADDED THIS LINE

echo ""
echo "🏗️ Starting services in order..."
echo "-------------------------------"

# Start Discovery Service first
start_service "discovery-service" "discovery-service" "8761"

# Start Config Server
start_service "config-server" "config-server" "8888"

# Start API Gateway
start_service "api-gateway" "api-gateway" "8080"

# Start Auth Service
start_service "auth-service" "auth-service" "8081"

# Start Employee Service  # ← ADDED THIS SECTION
start_service "employee-service" "employee-service" "8082"

echo ""
echo "==============================================="
echo "🎉 ALL SERVICES STARTED SUCCESSFULLY!"
echo "==============================================="
echo ""
echo "📊 Service Status:"
echo "   - Discovery Service: http://localhost:8761"
echo "   - Config Server:     http://localhost:8888"
echo "   - API Gateway:       http://localhost:8080"
echo "   - Auth Service:      http://localhost:8081"
echo "   - Employee Service:  http://localhost:8082"  # ← ADDED THIS LINE
echo ""
echo "📝 Logs Directory: logs/"
echo "   - View logs: tail -f logs/discovery-service.log"
echo "   - View all: ls -la logs/"
echo ""
echo "🛑 To stop all services: ./scripts/stop-all.sh"
echo "🔁 To restart: ./scripts/start-all.sh"
echo ""
echo "🔍 Quick Health Check:"
echo "   curl http://localhost:8080/actuator/health"
echo "   curl http://localhost:8081/auth/test/health"
echo "   curl http://localhost:8082/actuator/health"  # ← ADDED THIS LINE
echo ""
