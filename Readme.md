
🚀 Microservices Banking Application 
📌 Overview
This project is a Microservices-based Banking Application consisting of multiple services such as:

Account Service

Transaction Service

Notification Service

API Gateway

Service Registry (Eureka)

Each service runs independently and communicates through REST APIs registered to Eureka Discovery Server.

🏗️ Architecture Diagram (High-Level)
                      ┌────────────────────┐
                      │    API Gateway     │
                      │  (Spring Cloud)    │
                      └─────────┬──────────┘
                                │
                  ┌────────────┴──────────────┐
                  │                           │
        ┌─────────▼─────────┐       ┌─────────▼─────────┐
        │   Account Service  │       │ TransactionService │
        └─────────┬─────────┘       └─────────┬─────────┘
                  │                           │
                  │                           │
                  └──────────┬────────────────┘
                             │
                  ┌──────────▼───────────┐
                  │ Notification Service │
                  └──────────────────────┘

All services are registered to:

        ┌─────────────────┐
        │ Eureka Server   │
        └─────────────────┘



🛠️ Tech Stack
Component	Technology
Backend	Spring Boot
Gateway	Spring Cloud Gateway
Service Discovery	Eureka Server
Database	MySQL / PostgreSQL / H2
Build Tool	Maven
Containerization	Docker + Docker Compose



📁 Project Structure
/project-root
│
├── account-service/
│   ├── src/
│   └── Dockerfile
│
├── transaction-service/
│   ├── src/
│   └── Dockerfile
│
├── notification-service/
│   ├── src/
│   └── Dockerfile
│
├── api-gateway/
│   ├── src/
│   └── Dockerfile
│
├── discovery-server/
│   ├── src/
│   └── Dockerfile
│
└── docker-compose.yml
🐳 Dockerization Guide



Each microservice requires:

✔ 1. A Dockerfile
Example (Spring Boot):

FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
Place this file in:
account-service/Dockerfile,
transaction-service/Dockerfile, etc.


networks:
  microservices:
    driver: bridge
▶️ Running the Application
1️⃣ Build all microservices
From project root:

mvn clean package -DskipTests
2️⃣ Run using Docker Compose
docker-compose up --build
3️⃣ Access Services
Service	URL
Eureka Server	http://localhost:8761
API Gateway	http://localhost:8080
Account Service	Routed via Gateway
Transaction Service	Routed via Gateway
Notification Service	Routed via Gateway
