

# **Employee Management System - Microservices Architecture**

A complete microservices-based Employee Management System built with Spring Boot, featuring service discovery, centralized configuration, API gateway, and PostgreSQL database.

## 🚀 Current Status
- ✅ **Foundation Complete**: Microservices architecture, authentication, security
- ✅ **Core Features**: User registration, login, JWT tokens, service discovery
- 🔄 **In Progress**: Employee CRUD operations, additional role-based endpoints
- 🔄 **Planned**: Docker containerization, event-driven architecture

## 🏗️ Architecture Overview

## 📋 Features

- **✅ Service Discovery** - Automatic service registration with Eureka
- **✅ Centralized Configuration** - Configuration managed via GitHub repository
- **✅ API Gateway** - Single entry point with dynamic routing
- **✅ Microservices Architecture** - Independently deployable services
- **✅ Database Integration** - PostgreSQL with Spring Data JPA
- **✅ Health Monitoring** - Spring Boot Actuator endpoints
- **✅ Load Balancing** - Client-side load balancing with Spring Cloud LoadBalancer
- **✅ Automated Startup** - Bash scripts for easy service management

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.2.5**
- **Spring Cloud 2023.0.2**
- **Spring Cloud Gateway** - API Gateway
- **Eureka Server** - Service Discovery
- **Spring Cloud Config** - Centralized Configuration
- **PostgreSQL** - Database
- **Spring Data JPA** - Data Access
- **Spring Security** - Authentication & Authorization
- **Maven** - Build Tool

## 📁 Project Structure
```
employee-management-system/
├── discovery-service/     # Eureka Service Discovery Server
├── config-server/         # Spring Cloud Config Server  
├── api-gateway/           # Spring Cloud Gateway
├── auth-service/          # Authentication & Authorization Service
├── employee-service/      # Employee Management Service
├── scripts/               # Automation scripts for service management
│   ├── start-all.sh       # Start all services automatically
│   ├── stop-all.sh        # Stop all services
│   └── status.sh          # Check service status
├── pom.xml                # Parent POM (Maven Multi-module)
└── README.md              # This file
```

## 🚀 Quick Start

### **Prerequisites**
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 15+
- Git
- Netcat (for service health checks)

### **One-Command Startup** 🚀

```bash
# Start ALL microservices automatically (recommended)
./scripts/start-all.sh

# Check service status
./scripts/status.sh

# Stop ALL services
./scripts/stop-all.sh
```

### **What the automation script does:**
- ✅ Loads environment variables from `.env` file
- ✅ Builds all services in correct order
- ✅ Starts services with proper dependency management
- ✅ Waits for each service to be healthy before starting the next
- ✅ Provides real-time status monitoring

## 🛠️ Manual Setup (Alternative)

### 1. Clone Repository
```bash
git clone https://github.com/BenjaminAdimachukwu/employee-management-system.git
cd employee-management-system
```

### 2. Environment Setup
```bash
# Copy environment template and configure
cp .env.example .env
# Edit .env with your database credentials and JWT secret
```

### 3. Start PostgreSQL
```bash
brew services start postgresql@15
createdb employee_db
```

### 4. Start Services (Manual - Multiple Terminals)
```bash
# Terminal 1 - Discovery Service
mvn spring-boot:run -pl discovery-service

# Terminal 2 - Config Server  
mvn spring-boot:run -pl config-server

# Terminal 3 - API Gateway
mvn spring-boot:run -pl api-gateway

# Terminal 4 - Auth Service
mvn spring-boot:run -pl auth-service

# Terminal 5 - Employee Service
mvn spring-boot:run -pl employee-service
```

## 🌐 Access Points

| Service | URL | Port | Purpose |
|---------|-----|------|---------|
| **Eureka Dashboard** | http://localhost:8761 | 8761 | Service Discovery |
| **Config Server** | http://localhost:8888 | 8888 | Configuration Management |
| **API Gateway** | http://localhost:8080 | 8080 | Single Entry Point |
| **Auth Service** | http://localhost:8081 | 8081 | Authentication |
| **Employee Service** | http://localhost:8082 | 8082 | Employee Management |

## ⚙️ Configuration

Configuration is managed centrally via GitHub:
- **Config Repository**: https://github.com/BenjaminAdimachukwu/employee-management-config.git

Each service fetches its configuration from the Config Server, which pulls from the GitHub repository.

## 🔌 API Endpoints

**Via API Gateway** (http://localhost:8080)
```
GET/POST  /auth/**          → Auth Service (Authentication)
GET/POST  /api/employees/** → Employee Service (Business Logic)  
GET       /eureka/web/**    → Eureka Dashboard
GET       /config/**        → Config Server
```

## 🧪 Testing

### **Service Health Checks**
```bash
# Test API Gateway health
curl http://localhost:8080/actuator/health

# Test individual services
curl http://localhost:8081/actuator/health  # Auth Service
curl http://localhost:8082/actuator/health  # Employee Service

# Test Eureka registration
curl http://localhost:8761/eureka/apps

# Test configuration server
curl http://localhost:8888/api-gateway/default
```

### **Authentication Testing**
```bash
# Register a new user
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser", "password":"password123"}'

# Login with credentials  
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser", "password":"password123"}'
```

## 👨‍💻 Author
Benjamin Adimachukwu

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

## 🔗 GitHub
https://github.com/BenjaminAdimachukwu

---

