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
- **Service Discovery**: Eureka-based microservice registration and discovery
- **API Gateway**: Centralized routing and load balancing
- **Distributed Tracing**: Request tracking across services with Zipkin
- **Data Persistence**: PostgreSQL databases for each microservice

## Architecture Overview

VeriBoard follows a microservices architecture pattern with the following core components:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │────│ Eureka Server   │────│   Customer      │
│   (Port: 8083)  │    │   (Port: 8761)  │    │ Service (8080)  │
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
- **Spring Cloud 2023.0.5** (Gateway, Netflix Eureka, OpenFeign)
- **Maven** for build management

### Infrastructure
- **PostgreSQL** for data persistence
- **RabbitMQ** for message queuing
- **Zipkin** for distributed tracing
- **Docker & Docker Compose** for containerization
- **Kubernetes** configuration ready

### Development Tools
- **Lombok** for boilerplate code reduction
- **Jib** for container image building
- **pgAdmin** for database administration

## Prerequisites

Before running VeriBoard, ensure you have the following installed:

- **Java 21** (Amazon Corretto recommended)
- **Maven 3.6+**
- **Docker** and **Docker Compose**
- **Git** for version control

## Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/castrovroberto/tepemachine.git
cd tepemachine
```

### 2. Build the Application
```bash
# Build all modules and create Docker images
mvn clean package
mvn compile jib:dockerBuild
```

### 3. Start the Platform
```bash
# Start all services with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f

# Check service status
docker ps
```

### 4. Verify Deployment
Wait for all services to start (typically 2-3 minutes), then verify:

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway Health**: http://localhost:8083/actuator/health
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Zipkin Tracing**: http://localhost:9411
- **pgAdmin**: http://localhost:5050 (admin@admin.com/root)

## Available Services & Ports

| Service | Port | Purpose | Health Check |
|---------|------|---------|--------------|
| API Gateway | 8083 | Main entry point | http://localhost:8083/actuator/health |
| Customer Service | 8080 | Customer management | http://localhost:8080/actuator/health |
| Fraud Service | 8081 | Risk assessment | http://localhost:8081/actuator/health |
| Notification Service | 8082 | Messaging | http://localhost:8082/actuator/health |
| Eureka Server | 8761 | Service discovery | http://localhost:8761 |
| PostgreSQL | 5432 | Database | N/A |
| RabbitMQ | 5672, 15672 | Message broker | http://localhost:15672 |
| Zipkin | 9411 | Distributed tracing | http://localhost:9411 |
| pgAdmin | 5050 | DB administration | http://localhost:5050 |

## API Usage Examples

### Customer Registration

**Register a new customer:**
```bash
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
curl http://localhost:8083/actuator/health
```

**Check specific service health:**
```bash
curl http://localhost:8080/actuator/health  # Customer Service
curl http://localhost:8081/actuator/health  # Fraud Service
curl http://localhost:8082/actuator/health  # Notification Service
```

## Development Workflow

### Local Development
```bash
# Run infrastructure only (for local service development)
docker-compose up postgres rabbitmq zipkin eureka-server -d

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
2. Login: admin@admin.com / root
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

### Service Discovery
- Access Eureka Dashboard at http://localhost:8761
- View registered services and their health status
- Monitor service instances and load balancing

## Project Structure

```
veriboard/
├── amqp/                 # RabbitMQ configuration module
├── apiwg/                # API Gateway service
├── clients/              # Shared client interfaces
├── customer/             # Customer management service
├── eureka-server/        # Service discovery server
├── fraud/                # Fraud detection service
├── notification/         # Notification service
├── k8s/                  # Kubernetes manifests
├── project/              # Project documentation
├── docker-compose.yml    # Local deployment configuration
└── pom.xml              # Parent Maven configuration
```

## Stopping the Application

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clears databases)
docker-compose down -v

# Stop and remove all images
docker-compose down --rmi all
```

## Troubleshooting

### Common Issues

1. **Services not starting**: Check Docker resources (memory/CPU)
2. **Port conflicts**: Ensure ports 8080-8083, 5432, 5672, 9411, 8761 are available
3. **Database connection issues**: Wait for PostgreSQL to fully start before services
4. **Service discovery issues**: Verify Eureka server is healthy before starting other services

### Logs
```bash
# View all logs
docker-compose logs

# View specific service logs
docker-compose logs customer
docker-compose logs fraud
docker-compose logs notification
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

**VeriBoard** - Transforming customer onboarding through intelligent microservices architecture.
