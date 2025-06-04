# VeriBoard - Agile Customer Onboarding & Risk Mitigation Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.5-blue.svg)](https://spring.io/projects/spring-cloud)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/castrovroberto/tepemachine)

## Business Objective

VeriBoard is a modern microservices-based platform designed to streamline customer onboarding processes while implementing robust risk mitigation strategies. The platform automates customer registration, performs real-time fraud detection, and manages notification workflows to ensure secure and efficient customer experiences.

## MVP Features

- **Customer Registration**: Secure customer onboarding with input validation
- **Fraud Detection**: Real-time risk assessment during registration
- **Notification System**: Automated welcome messages and alerts via RabbitMQ
- **API Gateway**: Centralized routing and load balancing
- **Kubernetes-Native Service Discovery**: Cloud-native service registration and discovery
- **Distributed Tracing**: Request tracking across services with Zipkin
- **Data Persistence**: PostgreSQL databases for each microservice

## Architecture Overview

VeriBoard follows a cloud-native microservices architecture pattern optimized for Kubernetes deployment:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │────│   Kubernetes    │────│   Customer      │
│   (Port: 8083)  │    │Service Discovery│    │ Service (8080)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                                              │
         │              ┌─────────────────┐            │
         └──────────────│   Fraud Check   │────────────┘
                        │ Service (8081)  │
                        └─────────────────┘
                                 │
         ┌─────────────────┐     │     ┌─────────────────┐
         │  Notification   │─────┘─────│    RabbitMQ     │
         │ Service (8082)  │           │   (Port: 5672)  │
         └─────────────────┘           └─────────────────┘
                 │
         ┌─────────────────┐
         │   PostgreSQL    │
         │   (Port: 5432)  │
         └─────────────────┘
```

## Technology Stack

### Core Technologies
- **Java 21** (Amazon Corretto)
- **Spring Boot 3.2.6** (Web, Data JPA, AMQP, Validation)
- **Spring Cloud 2023.0.5** (Gateway, Kubernetes, OpenFeign)
- **Maven** for build management

### Infrastructure
- **PostgreSQL** for data persistence
- **RabbitMQ** for message queuing
- **Zipkin** for distributed tracing
- **Kubernetes** for orchestration and service discovery
- **Docker** for containerization

### Development Tools
- **Lombok** for boilerplate code reduction
- **Jib** for container image building
- **pgAdmin** for database administration
- **Testcontainers** for integration testing

## Prerequisites

Before running VeriBoard, ensure you have the following installed:

- **Java 21** (Amazon Corretto recommended)
- **Maven 3.6+**
- **Docker** and **Docker Compose**
- **Kubernetes cluster** (for production deployment)
- **kubectl** configured for your cluster
- **Git** for version control

## Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/castrovroberto/tepemachine.git
cd tepemachine
```

### 2. Start Infrastructure Services
```bash
# Start supporting infrastructure (PostgreSQL, RabbitMQ, Zipkin)
docker-compose up -d

# Verify infrastructure is running
docker-compose ps
```

### 3. Build the Application
```bash
# Build all modules and create Docker images
mvn clean package
mvn compile jib:dockerBuild
```

### 4. Deploy to Kubernetes
```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods
kubectl get services
```

### 5. Verify Deployment
Wait for all services to start (typically 2-3 minutes), then verify:

- **API Gateway Health**: `kubectl port-forward svc/apiwg 8083:8083` then http://localhost:8083/actuator/health
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Zipkin Tracing**: http://localhost:9411
- **pgAdmin**: http://localhost:5050 (pgadmin4@pgadmin.org/admin)

## Available Services & Ports

| Service | Port | Purpose | Health Check |
|---------|------|---------|--------------|
| API Gateway | 8083 | Main entry point | /actuator/health |
| Customer Service | 8080 | Customer management | /actuator/health |
| Fraud Service | 8081 | Risk assessment | /actuator/health |
| Notification Service | 8082 | Messaging | /actuator/health |
| PostgreSQL | 5432 | Database | N/A |
| RabbitMQ | 5672, 15672 | Message broker | http://localhost:15672 |
| Zipkin | 9411 | Distributed tracing | http://localhost:9411 |
| pgAdmin | 5050 | DB administration | http://localhost:5050 |

## API Usage Examples

### Customer Registration

**Register a new customer:**
```bash
# Port forward the API Gateway service
kubectl port-forward svc/apiwg 8083:8083

# Register customer
curl -X POST http://localhost:8083/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe", 
    "email": "john.doe@example.com"
  }'
```

**Expected Response (201 Created):**
```json
{
  "message": "Customer registered successfully"
}
```

**Invalid Request Example:**
```bash
curl -X POST http://localhost:8083/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "",
    "lastName": "Doe",
    "email": "invalid-email"
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Validation failed",
  "details": ["firstName cannot be blank", "email must be valid"]
}
```

### Health Checks

**Check API Gateway status:**
```bash
kubectl port-forward svc/apiwg 8083:8083
curl http://localhost:8083/actuator/health
```

**Check specific service health:**
```bash
kubectl port-forward svc/customer 8080:8080
curl http://localhost:8080/actuator/health

kubectl port-forward svc/fraud 8081:8081
curl http://localhost:8081/actuator/health

kubectl port-forward svc/notification 8082:8082
curl http://localhost:8082/actuator/health
```

## Development Workflow

### Local Development with Infrastructure
```bash
# Start only infrastructure services locally
docker-compose up postgres rabbitmq zipkin pgadmin -d

# Run individual services locally with IDE or:
mvn spring-boot:run -pl customer
mvn spring-boot:run -pl fraud  
mvn spring-boot:run -pl notification
mvn spring-boot:run -pl apiwg
```

### Testing
```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl customer
```

### Database Access

**Via pgAdmin** (recommended):
1. Go to http://localhost:5050
2. Login: pgadmin4@pgadmin.org / admin
3. Add servers for customer, fraud, and notification databases

**Via command line:**
```bash
# Connect to customer database
docker exec -it postgres psql -U yumptech -d customer

# Connect to fraud database
docker exec -it postgres psql -U yumptech -d fraud

# Connect to notification database
docker exec -it postgres psql -U yumptech -d notification
```

## Monitoring & Observability

### Distributed Tracing
- Access Zipkin at http://localhost:9411
- Search for traces by service name or time range
- Monitor request flows across microservices

### Message Queue Monitoring
- Access RabbitMQ Management at http://localhost:15672
- Monitor queue depths, message rates, and connections
- Default credentials: guest/guest

### Kubernetes Monitoring
```bash
# View service status
kubectl get services

# View pod logs
kubectl logs -f deployment/customer
kubectl logs -f deployment/fraud
kubectl logs -f deployment/notification
kubectl logs -f deployment/apiwg

# View pod status
kubectl get pods
kubectl describe pod <pod-name>
```

## Project Structure

```
veriboard/
├── amqp/                 # RabbitMQ configuration module
├── apiwg/                # API Gateway service
├── clients/              # Shared client interfaces
├── customer/             # Customer management service
├── fraud/                # Fraud detection service
├── notification/         # Notification service
├── k8s/                  # Kubernetes manifests
├── project/              # Project documentation
├── docker-compose.yml    # Infrastructure services only
└── pom.xml              # Parent Maven configuration
```

## Deployment Options

### Local Development
```bash
# Infrastructure only
docker-compose up -d
# Run services locally via IDE or Maven
```

### Kubernetes Production
```bash
# Deploy all services to Kubernetes
kubectl apply -f k8s/
```

### Stopping the Application

**Infrastructure services:**
```bash
# Stop infrastructure services
docker-compose down

# Stop and remove volumes (clears databases)
docker-compose down -v
```

**Kubernetes services:**
```bash
# Remove Kubernetes deployments
kubectl delete -f k8s/
```

## Troubleshooting

### Common Issues

1. **Services not starting in Kubernetes**: Check pod logs with `kubectl logs`
2. **Database connection issues**: Ensure PostgreSQL is running via docker-compose
3. **Service discovery issues**: Verify Kubernetes DNS and service configuration
4. **Port conflicts**: Ensure infrastructure ports are available

### Logs
```bash
# Infrastructure logs
docker-compose logs

# Kubernetes service logs
kubectl logs -f deployment/<service-name>
kubectl logs -f deployment/customer
kubectl logs -f deployment/fraud
kubectl logs -f deployment/notification
kubectl logs -f deployment/apiwg
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For questions or support, please open an issue in the GitHub repository or contact the development team.

---

**VeriBoard** - Transforming customer onboarding through cloud-native microservices architecture.
