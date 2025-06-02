# VeriBoard Kubernetes Deployment Guide

This directory contains Kubernetes manifests for deploying the VeriBoard application to Minikube.

## 🎯 **Migration Complete: Eureka → Kubernetes Service Discovery**

The VeriBoard application has been successfully migrated from Eureka service discovery to Kubernetes native service discovery.

## 📁 **Directory Structure**

```
k8s/minikube/
├── bootstrap/          # Infrastructure components (Postgres, RabbitMQ, Zipkin)
├── rbac/              # RBAC configuration for service discovery
├── secrets/           # Application secrets
├── services/          # Application service manifests
└── README.md          # This file
```

## 🚀 **Deployment Steps**

### 1. Prerequisites

Ensure you have:
- Minikube running
- kubectl configured
- NGINX Ingress Controller enabled in Minikube

```bash
# Enable NGINX Ingress
minikube addons enable ingress

# Verify Minikube is running
kubectl cluster-info
```

### 2. Deploy Infrastructure Components

```bash
# Deploy PostgreSQL
kubectl apply -f bootstrap/postgres/

# Deploy RabbitMQ
kubectl apply -f bootstrap/rabbitmq/

# Deploy Zipkin
kubectl apply -f bootstrap/zipkin/
```

### 3. Deploy Application Components

```bash
# Deploy RBAC (required for service discovery)
kubectl apply -f rbac/rbac.yml

# Deploy secrets
kubectl apply -f secrets/secrets.yml

# Deploy application services
kubectl apply -f services/
```

### 4. Verify Deployment

```bash
# Check all pods are running
kubectl get pods

# Check services
kubectl get services

# Check ingress
kubectl get ingress
```

### 5. Access the Application

```bash
# Get Minikube IP
minikube ip

# Add to /etc/hosts (replace <MINIKUBE_IP> with actual IP)
echo "<MINIKUBE_IP> veriboard.local" | sudo tee -a /etc/hosts

# Access the application
curl http://veriboard.local/api/v1/customers
```

## 🔍 **Service Discovery Validation**

To verify that Kubernetes service discovery is working:

1. **Check Service Registration:**
   ```bash
   # View customer service endpoints
   kubectl get endpoints customer
   
   # View fraud service endpoints  
   kubectl get endpoints fraud
   ```

2. **Test Inter-Service Communication:**
   ```bash
   # Get a customer pod
   CUSTOMER_POD=$(kubectl get pods -l app=customer -o jsonpath='{.items[0].metadata.name}')
   
   # Test fraud service discovery from customer pod
   kubectl exec $CUSTOMER_POD -- curl -s http://fraud:8081/actuator/health
   ```

3. **Check API Gateway Routing:**
   ```bash
   # Test via ingress
   curl http://veriboard.local/api/v1/customers
   ```

## 🎛️ **Configuration Details**

### Service Discovery Configuration

Each service now uses Spring Cloud Kubernetes for service discovery:

```yaml
spring:
  cloud:
    kubernetes:
      discovery:
        all-namespaces: false # Security: limit to current namespace
        enabled: true
      loadbalancer:
        mode: SERVICE # Use Kubernetes Service DNS names
```

### RBAC Permissions

The `veriboard-service-account` has permissions to:
- Get, list, and watch Services and Endpoints
- Get, list, and watch Pods (for load balancing)

### Health Checks

All services are configured with:
- **Liveness Probe:** `/actuator/health/liveness`
- **Readiness Probe:** `/actuator/health/readiness`

## 🔧 **Troubleshooting**

### Service Discovery Issues

1. **Check RBAC permissions:**
   ```bash
   kubectl auth can-i get services --as=system:serviceaccount:default:veriboard-service-account
   ```

2. **Check service registration:**
   ```bash
   kubectl get endpoints
   ```

3. **Check pod logs for discovery issues:**
   ```bash
   kubectl logs -l app=customer | grep -i discovery
   ```

### Common Issues

- **DNS Resolution:** Ensure CoreDNS is running (`kubectl get pods -n kube-system`)
- **Network Policies:** Check if any network policies are blocking communication
- **Service Names:** Verify service names match between manifests and configuration

## ✅ **Migration Verification Checklist**

- [ ] No Eureka server deployed
- [ ] No Eureka client dependencies in POMs  
- [ ] Kubernetes discovery client dependencies added
- [ ] Service discovery configuration updated
- [ ] Kubernetes manifests created
- [ ] RBAC permissions configured
- [ ] Services can discover each other
- [ ] API Gateway routes work with `lb://` URIs
- [ ] Health checks respond correctly
- [ ] Distributed tracing still works

## 📊 **Monitoring**

Access monitoring endpoints:

```bash
# Prometheus metrics (if enabled)
kubectl port-forward svc/customer 8080:8080
curl http://localhost:8080/actuator/prometheus

# Health check
curl http://veriboard.local/actuator/health

# Zipkin tracing
kubectl port-forward svc/zipkin 9411:9411
# Open http://localhost:9411 in browser
```

---

🎉 **Congratulations!** The VeriBoard application is now running with Kubernetes-native service discovery, fully migrated from Eureka. 