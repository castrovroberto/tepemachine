# VeriBoard Minikube Deployment Scripts

This directory contains scripts to help you easily deploy and manage the VeriBoard microservices on minikube.

## Scripts Overview

- **`minikube-deploy.sh`** - Main deployment and management script
- **`monitor.sh`** - Real-time monitoring and troubleshooting script
- **`port-forward.sh`** - Easy port forwarding for local access to services

## Prerequisites

- **Minikube**: Make sure minikube is installed and running
- **kubectl**: Kubernetes command-line tool
- **Maven**: For building the Java services
- **Docker**: For container operations

## Quick Start

### 1. Make sure minikube is running
```bash
minikube start
minikube addons enable ingress
```

### 2. Full deployment (first time)
```bash
./scripts/minikube-deploy.sh full-deploy
```

This will:
- Deploy infrastructure (PostgreSQL, RabbitMQ, Zipkin)
- Create databases
- Build Docker images
- Deploy all services
- Show health status and access URLs

### 3. Check status
```bash
./scripts/minikube-deploy.sh status
```

### 4. Monitor in real-time
```bash
./scripts/monitor.sh dashboard
```

### 5. Access services locally
```bash
./scripts/port-forward.sh start-all
```

## Main Deployment Script (`minikube-deploy.sh`)

### Deployment Commands

- **`full-deploy`** - Complete deployment from scratch
- **`deploy-infra`** - Deploy only infrastructure services
- **`deploy-services`** - Deploy only application services
- **`setup-db`** - Create databases in PostgreSQL

### Image Management

- **`build [SERVICES]`** - Build Docker images for specified services (or all)
- **`quick-update [SERVICES]`** - Rebuild images and restart deployments

Examples:
```bash
./scripts/minikube-deploy.sh build customer fraud
./scripts/minikube-deploy.sh quick-update notification
```

### Monitoring and Debugging

- **`status`** - Show current pod, service, and deployment status
- **`logs [SERVICE]`** - View recent logs for a service
- **`follow [SERVICE]`** - Follow logs in real-time
- **`health`** - Check health status of all services
- **`wait [TIMEOUT]`** - Wait for pods to become ready

Examples:
```bash
./scripts/minikube-deploy.sh logs customer
./scripts/minikube-deploy.sh follow fraud
./scripts/minikube-deploy.sh health
```

### Service Management

- **`restart [SERVICES]`** - Restart specific deployments (or all)
- **`urls`** - Show how to access services
- **`cleanup`** - Remove all deployments

Examples:
```bash
./scripts/minikube-deploy.sh restart customer fraud
./scripts/minikube-deploy.sh urls
```

## Monitoring Script (`monitor.sh`)

The monitoring script provides advanced real-time monitoring capabilities:

### Commands

- **`dashboard`** - Real-time health dashboard with auto-refresh
- **`watch`** - Watch pod status in real-time
- **`monitor-logs`** - Monitor logs for all services simultaneously
- **`tail [SERVICES]`** - Tail logs for specific services
- **`resources`** - Show resource usage (requires metrics server)
- **`events`** - Show recent Kubernetes events
- **`describe`** - Describe problematic pods

### Examples

```bash
# Real-time dashboard
./scripts/monitor.sh dashboard

# Watch pods
./scripts/monitor.sh watch

# Monitor logs for specific services
./scripts/monitor.sh tail customer fraud

# Show recent events
./scripts/monitor.sh events
```

## Port Forwarding Script (`port-forward.sh`)

The port forwarding script makes it easy to access services locally:

### Commands

- **`start [SERVICE]`** - Start port forwarding for a specific service
- **`start-all`** - Start port forwarding for all services
- **`stop-all`** - Stop all port forwards
- **`status`** - Show status of current port forwards
- **`cleanup`** - Clean up any leftover processes

### Examples

```bash
# Start port forwarding for all services
./scripts/port-forward.sh start-all

# Start port forwarding for a specific service
./scripts/port-forward.sh start customer

# Check status
./scripts/port-forward.sh status

# Stop all port forwards
./scripts/port-forward.sh stop-all
```

### Service URLs (when port forwarding is active)

- **Customer Service**: http://localhost:8080
- **Fraud Service**: http://localhost:8081
- **Notification Service**: http://localhost:8082
- **API Gateway**: http://localhost:8083
- **Zipkin UI**: http://localhost:9411
- **RabbitMQ Management**: http://localhost:15672

## Typical Workflows

### First Time Setup
```bash
# Start minikube if not running
minikube start

# Full deployment
./scripts/minikube-deploy.sh full-deploy

# Check everything is working
./scripts/minikube-deploy.sh health
./scripts/minikube-deploy.sh urls

# Start monitoring dashboard
./scripts/monitor.sh dashboard

# In another terminal, start port forwarding
./scripts/port-forward.sh start-all
```

### Development Workflow
```bash
# After making code changes to a service
./scripts/minikube-deploy.sh quick-update customer

# Monitor the deployment
./scripts/monitor.sh tail customer

# Check logs
./scripts/minikube-deploy.sh logs customer

# Access the service locally
./scripts/port-forward.sh start customer
```

### Debugging Issues
```bash
# Check overall status
./scripts/minikube-deploy.sh status

# Use the monitoring dashboard
./scripts/monitor.sh dashboard

# Check health of services
./scripts/minikube-deploy.sh health

# View detailed information about problematic pods
./scripts/monitor.sh describe

# Show recent events
./scripts/monitor.sh events

# View logs for problematic service
./scripts/minikube-deploy.sh logs fraud 100

# Restart a specific service
./scripts/minikube-deploy.sh restart fraud
```

### Clean Start
```bash
# Remove everything and start fresh
./scripts/minikube-deploy.sh cleanup
./scripts/minikube-deploy.sh full-deploy

# Monitor the deployment progress
./scripts/monitor.sh dashboard
```

## Service Ports

- **Customer Service**: 8080
- **Fraud Service**: 8081  
- **Notification Service**: 8082
- **API Gateway**: 8083
- **Zipkin UI**: 9411
- **RabbitMQ Management**: 15672

## Accessing Services

### Option 1: Using the port forwarding script (recommended)
```bash
# Start all services
./scripts/port-forward.sh start-all

# Or start individual services
./scripts/port-forward.sh start customer
```

### Option 2: Manual port forwarding
```bash
# Access customer service
kubectl port-forward service/customer 8080:8080

# Access API gateway
kubectl port-forward service/apiwg 8083:8083

# Access Zipkin UI
kubectl port-forward service/zipkin 9411:9411
```

Then navigate to `http://localhost:PORT` in your browser.

## Script Features

### Main Deployment Script
- **Colored output** for better readability
- **Error handling** with clear messages
- **Flexible service selection** - work with specific services or all
- **Automatic environment setup** for minikube Docker
- **Health checks** and status monitoring
- **Interactive prompts** when service names are not provided

### Monitoring Script
- **Real-time dashboard** with auto-refresh
- **Multi-service log monitoring**
- **Resource usage tracking**
- **Event monitoring** for troubleshooting
- **Problematic pod analysis**

### Port Forwarding Script
- **Automatic port management** - checks for conflicts
- **Background process tracking** for easy cleanup
- **Status monitoring** of active port forwards
- **One-command access** to all services

## Troubleshooting

### Services in CrashLoopBackOff
```bash
# Use the monitoring dashboard to see real-time status
./scripts/monitor.sh dashboard

# Check logs to identify the issue
./scripts/minikube-deploy.sh logs <service-name>

# Describe the problematic pods
./scripts/monitor.sh describe

# Often caused by missing databases - run:
./scripts/minikube-deploy.sh setup-db
./scripts/minikube-deploy.sh restart
```

### Images not found
```bash
# Rebuild images
./scripts/minikube-deploy.sh build

# Or do a quick update
./scripts/minikube-deploy.sh quick-update
```

### Services not ready
```bash
# Use the dashboard to monitor progress
./scripts/monitor.sh dashboard

# Wait for services to become ready
./scripts/minikube-deploy.sh wait 300

# Check health status
./scripts/minikube-deploy.sh health
```

### Can't access services
```bash
# Check if port forwarding is active
./scripts/port-forward.sh status

# Start port forwarding if needed
./scripts/port-forward.sh start-all

# Or manually forward specific ports
kubectl port-forward service/customer 8080:8080
```

### Fresh start
```bash
# Complete cleanup and redeploy
./scripts/minikube-deploy.sh cleanup
./scripts/minikube-deploy.sh full-deploy

# Monitor the deployment progress
./scripts/monitor.sh dashboard
```

## Advanced Tips

### For better log monitoring
Install `multitail` for enhanced multi-service log viewing:
```bash
# Ubuntu/Debian
sudo apt-get install multitail

# Then use the monitoring script for better log tailing
./scripts/monitor.sh tail customer fraud notification
```

### Enable metrics server for resource monitoring
```bash
minikube addons enable metrics-server

# Then use the resource monitoring command
./scripts/monitor.sh resources
```

### Background log monitoring
```bash
# Monitor all services and save logs to files
./scripts/monitor.sh monitor-logs

# This will save logs to /tmp/veriboard-logs-<timestamp>/
```

### Using multiple terminals for complete monitoring
```bash
# Terminal 1: Real-time dashboard
./scripts/monitor.sh dashboard

# Terminal 2: Port forwarding
./scripts/port-forward.sh start-all

# Terminal 3: Log monitoring
./scripts/monitor.sh tail customer fraud

# Terminal 4: Available for commands
./scripts/minikube-deploy.sh status
``` 