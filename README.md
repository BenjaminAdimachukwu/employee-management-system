# Employee Management System - Microservices Architecture

A complete microservices-based Employee Management System built with Spring Boot, featuring service discovery, centralized configuration, API gateway, and PostgreSQL database.

## 🏗️ Architecture Overview


##  Features

- **✅ Service Discovery** - Automatic service registration with Eureka
- **✅ Centralized Configuration** - Configuration managed via GitHub repository
- **✅ API Gateway** - Single entry point with dynamic routing
- **✅ Microservices Architecture** - Independently deployable services
- **✅ Database Integration** - PostgreSQL with Spring Data JPA
- **✅ Health Monitoring** - Spring Boot Actuator endpoints
- **✅ Load Balancing** - Client-side load balancing with Spring Cloud LoadBalancer

##  Tech Stack

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

##  Project Structure
employee-management-system/

├── discovery-service/ # Eureka Service Discovery Server

├── config-server/ # Spring Cloud Config Server

├── api-gateway/ # Spring Cloud Gateway

├── auth-service/ # Authentication & Authorization Service

├── employee-service/ # Employee Management Service

├── pom.xml # Parent POM (Maven Multi-module)

└── README.md # This file


## 🚦 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 15+
- Git

# Installation & Setup

 ## 1. clone repository
   https://github.com/BenjaminAdimachukwu/employee-management-system.git
   
## 2. Start PostgreSQL

brew services start postgresql@15

createdb employee_db

## 3. Start services in order:

## 1. Service Discovery
mvn spring-boot:run -pl discovery-service

## 2. Config Server
mvn spring-boot:run -pl config-server

## 3. API Gateway
mvn spring-boot:run -pl api-gateway

## 4. Auth Service
mvn spring-boot:run -pl auth-service

## 5. Employee Service
mvn spring-boot:run -pl employee-service

Access Points
Eureka Dashboard: http://localhost:8761

API Gateway: http://localhost:8080

Config Server: http://localhost:8888

Auth Service Health: http://localhost:8081/actuator/health

Employee Service Health: http://localhost:8082/actuator/health


## Configuration

Configuration is managed centrally via GitHub:

Config Repository: https://github.com/BenjaminAdimachukwu/employee-management-config.git

Each service fetches its configuration from the Config Server, which pulls from the GitHub repository.


## API Endpoints
Via API Gateway (http://localhost:8080)

GET  /auth/**          → Auth Service
GET  /api/employees/** → Employee Service
GET  /eureka/web/**    → Eureka Dashboard
GET  /config/**        → Config Server

## Testing:
## Test service health
curl http://localhost:8080/actuator/health

## Test Eureka registration
curl http://localhost:8761/eureka/apps

## Test configuration
curl http://localhost:8888/api-gateway/default


## Author
Benjamin Adimachukwu

## License
This project is licensed under the MIT License - see the LICENSE file for details.

GitHub: https://github.com/BenjaminAdimachukwu