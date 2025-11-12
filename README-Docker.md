# Dockerizing the microservices

This repo contains several Spring Boot microservices. This document explains how to build and run them with Docker and Docker Compose.

Prerequisites
- Docker Desktop (Windows)
- At least 4GB free memory for multiple containers

Build and run
1. From the repository root (where `docker-compose.yml` lives), build and start all services:

```
docker compose up --build
```

2. Services:
- Eureka: http://localhost:8761
- API Gateway: http://localhost:8888
- Patient service: http://localhost:8085
- Doctor service: http://localhost:8086
- Appointment service: http://localhost:8087

Notes
- The Dockerfiles use OpenJDK 25 (matching the project's `java.version`). Ensure your environment supports it.
- MySQL containers expose internal 3306 to host ports 3307/3308/3309 to avoid conflicts if you run a local MySQL.
- If you have existing databases, adjust compose env variables or remove the mysql services and point services to your DBs.

Troubleshooting
- If services fail to register with Eureka, check logs: `docker compose logs -f eureka-server` and individual services.
- To rebuild a single service: `docker compose build patient-service` then `docker compose up -d patient-service`.
