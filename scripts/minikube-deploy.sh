#!/bin/bash

# VeriBoard Minikube Deployment Script
# This script provides user-friendly commands for managing the VeriBoard microservices deployment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Configuration
SERVICES=("customer" "fraud" "notification" "apiwg")
INFRASTRUCTURE=("postgres" "rabbitmq" "zipkin")
NAMESPACE="default"

# Helper functions
print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${PURPLE}ℹ $1${NC}"
}

# Check if minikube is running
check_minikube() {
    if ! minikube status > /dev/null 2>&1; then
        print_error "Minikube is not running. Please start minikube first."
        exit 1
    fi
    print_success "Minikube is running"
}

# Check if kubectl is available
check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is not installed or not in PATH"
        exit 1
    fi
    print_success "kubectl is available"
}

# Setup minikube docker environment
setup_docker_env() {
    print_info "Setting up minikube docker environment..."
    eval $(minikube docker-env)
    print_success "Docker environment configured for minikube"
}

# Check pod status
check_status() {
    print_header "CURRENT POD STATUS"
    kubectl get pods -o wide
    echo ""
    
    print_header "SERVICE STATUS"
    kubectl get services
    echo ""
    
    print_header "DEPLOYMENT STATUS"
    kubectl get deployments
}

# View logs for a specific service
view_logs() {
    local service=$1
    local lines=${2:-50}
    
    if [ -z "$service" ]; then
        echo "Available services:"
        for svc in "${SERVICES[@]}"; do
            echo "  - $svc"
        done
        for infra in "${INFRASTRUCTURE[@]}"; do
            echo "  - $infra"
        done
        echo ""
        read -p "Enter service name: " service
    fi
    
    print_header "LOGS FOR $service"
    
    # Get the pod name
    local pod_name=$(kubectl get pods -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
    
    if [ -z "$pod_name" ]; then
        print_error "No pods found for service: $service"
        return 1
    fi
    
    print_info "Showing last $lines lines for pod: $pod_name"
    kubectl logs $pod_name --tail=$lines
}

# Follow logs for a service
follow_logs() {
    local service=$1
    
    if [ -z "$service" ]; then
        echo "Available services:"
        for svc in "${SERVICES[@]}"; do
            echo "  - $svc"
        done
        read -p "Enter service name: " service
    fi
    
    local pod_name=$(kubectl get pods -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
    
    if [ -z "$pod_name" ]; then
        print_error "No pods found for service: $service"
        return 1
    fi
    
    print_info "Following logs for pod: $pod_name (Press Ctrl+C to stop)"
    kubectl logs -f $pod_name
}

# Build images
build_images() {
    local services_to_build=("${SERVICES[@]}")
    
    if [ $# -gt 0 ]; then
        services_to_build=("$@")
    fi
    
    print_header "BUILDING DOCKER IMAGES"
    setup_docker_env
    
    print_info "Building Maven packages..."
    mvn clean package -DskipTests -pl $(IFS=,; echo "${services_to_build[*]}")
    
    print_info "Building Docker images with Jib..."
    mvn jib:dockerBuild -pl $(IFS=,; echo "${services_to_build[*]}") -Djib.from.platforms=linux/amd64
    
    print_success "Images built successfully"
    
    # Show built images
    echo ""
    print_info "Built images:"
    eval $(minikube docker-env) && docker images | grep castrovroberto | grep -E "($(IFS=\|; echo "${services_to_build[*]}"))"
}

# Deploy infrastructure
deploy_infrastructure() {
    print_header "DEPLOYING INFRASTRUCTURE"
    
    print_info "Deploying PostgreSQL..."
    kubectl apply -f k8s/minikube/bootstrap/postgres/
    
    print_info "Deploying RabbitMQ..."
    kubectl apply -f k8s/minikube/bootstrap/rabbitmq/
    
    print_info "Deploying Zipkin..."
    kubectl apply -f k8s/minikube/bootstrap/zipkin/
    
    print_info "Applying RBAC..."
    kubectl apply -f k8s/minikube/rbac/rbac.yml
    
    print_info "Applying secrets..."
    kubectl apply -f k8s/minikube/secrets/secrets.yml
    
    print_success "Infrastructure deployed"
}

# Setup databases
setup_databases() {
    print_header "SETTING UP DATABASES"
    
    # Wait for postgres to be ready
    print_info "Waiting for PostgreSQL to be ready..."
    kubectl wait --for=condition=Ready pod/postgres-0 --timeout=300s
    
    # Create databases
    for service in "${SERVICES[@]}"; do
        if [ "$service" != "apiwg" ]; then  # API Gateway doesn't need a database
            print_info "Creating database: $service"
            kubectl exec -it postgres-0 -- psql -U yumptech -d postgres -c "CREATE DATABASE $service;" 2>/dev/null || print_warning "Database $service might already exist"
        fi
    done
    
    print_success "Databases setup completed"
}

# Deploy services
deploy_services() {
    print_header "DEPLOYING SERVICES"
    
    print_info "Applying service manifests..."
    kubectl apply -f k8s/minikube/services/
    
    print_success "Services deployed"
}

# Restart deployments
restart_deployments() {
    local services_to_restart=("${SERVICES[@]}")
    
    if [ $# -gt 0 ]; then
        services_to_restart=("$@")
    fi
    
    print_header "RESTARTING DEPLOYMENTS"
    
    for service in "${services_to_restart[@]}"; do
        print_info "Restarting $service..."
        kubectl rollout restart deployment $service
    done
    
    print_success "Deployments restarted"
}

# Wait for pods to be ready
wait_for_ready() {
    local timeout=${1:-300}
    
    print_header "WAITING FOR PODS TO BE READY"
    
    for service in "${SERVICES[@]}"; do
        print_info "Waiting for $service to be ready..."
        kubectl wait --for=condition=Ready pod -l app=$service --timeout=${timeout}s || print_warning "$service might not be ready yet"
    done
}

# Health check
health_check() {
    print_header "HEALTH CHECK"
    
    for service in "${SERVICES[@]}"; do
        local pod_name=$(kubectl get pods -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
        
        if [ -z "$pod_name" ]; then
            print_error "$service: No pods found"
            continue
        fi
        
        local pod_status=$(kubectl get pod $pod_name -o jsonpath='{.status.phase}')
        local ready_status=$(kubectl get pod $pod_name -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}')
        
        if [ "$pod_status" = "Running" ] && [ "$ready_status" = "True" ]; then
            print_success "$service: Healthy"
        else
            print_warning "$service: $pod_status (Ready: $ready_status)"
        fi
    done
}

# Get service URLs
get_urls() {
    print_header "SERVICE URLS"
    
    local minikube_ip=$(minikube ip)
    
    echo "To access services, use port-forwarding:"
    echo ""
    
    for service in "${SERVICES[@]}"; do
        local port=""
        case $service in
            "customer") port="8080" ;;
            "fraud") port="8081" ;;
            "notification") port="8082" ;;
            "apiwg") port="8083" ;;
        esac
        
        if [ -n "$port" ]; then
            echo "  kubectl port-forward service/$service $port:$port"
            echo "  Then access: http://localhost:$port"
            echo ""
        fi
    done
    
    echo "For infrastructure services:"
    echo "  kubectl port-forward service/zipkin 9411:9411"
    echo "  Then access Zipkin UI: http://localhost:9411"
}

# Full deployment
full_deploy() {
    print_header "FULL DEPLOYMENT STARTING"
    
    check_minikube
    check_kubectl
    
    deploy_infrastructure
    sleep 10
    
    setup_databases
    sleep 5
    
    build_images
    deploy_services
    
    print_info "Waiting for services to start..."
    sleep 30
    
    health_check
    get_urls
    
    print_success "Full deployment completed!"
}

# Quick update (rebuild images and restart)
quick_update() {
    local services_to_update=("${SERVICES[@]}")
    
    if [ $# -gt 0 ]; then
        services_to_update=("$@")
    fi
    
    print_header "QUICK UPDATE"
    
    build_images "${services_to_update[@]}"
    restart_deployments "${services_to_update[@]}"
    
    print_info "Waiting for services to restart..."
    sleep 20
    
    health_check
    
    print_success "Quick update completed!"
}

# Cleanup
cleanup() {
    print_header "CLEANING UP"
    
    print_info "Deleting services..."
    kubectl delete -f k8s/minikube/services/ --ignore-not-found=true
    
    print_info "Deleting infrastructure..."
    kubectl delete -f k8s/minikube/bootstrap/ --recursive --ignore-not-found=true
    
    print_info "Deleting RBAC and secrets..."
    kubectl delete -f k8s/minikube/rbac/rbac.yml --ignore-not-found=true
    kubectl delete -f k8s/minikube/secrets/secrets.yml --ignore-not-found=true
    
    print_success "Cleanup completed"
}

# Show help
show_help() {
    echo "VeriBoard Minikube Deployment Script"
    echo ""
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  status              Show current pod and service status"
    echo "  logs [SERVICE]      View logs for a service"
    echo "  follow [SERVICE]    Follow logs for a service"
    echo "  build [SERVICES]    Build Docker images for services"
    echo "  deploy-infra        Deploy infrastructure (postgres, rabbitmq, zipkin)"
    echo "  setup-db            Setup databases"
    echo "  deploy-services     Deploy application services"
    echo "  restart [SERVICES]  Restart deployments"
    echo "  wait [TIMEOUT]      Wait for pods to be ready"
    echo "  health              Check health of all services"
    echo "  urls                Show service URLs"
    echo "  full-deploy         Complete deployment from scratch"
    echo "  quick-update [SERVICES] Rebuild images and restart services"
    echo "  cleanup             Remove all deployments"
    echo "  help                Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 status"
    echo "  $0 logs customer"
    echo "  $0 build customer fraud"
    echo "  $0 quick-update notification"
    echo "  $0 full-deploy"
}

# Main script logic
case "${1:-help}" in
    "status")
        check_status
        ;;
    "logs")
        view_logs "$2" "$3"
        ;;
    "follow")
        follow_logs "$2"
        ;;
    "build")
        shift
        build_images "$@"
        ;;
    "deploy-infra")
        deploy_infrastructure
        ;;
    "setup-db")
        setup_databases
        ;;
    "deploy-services")
        deploy_services
        ;;
    "restart")
        shift
        restart_deployments "$@"
        ;;
    "wait")
        wait_for_ready "$2"
        ;;
    "health")
        health_check
        ;;
    "urls")
        get_urls
        ;;
    "full-deploy")
        full_deploy
        ;;
    "quick-update")
        shift
        quick_update "$@"
        ;;
    "cleanup")
        cleanup
        ;;
    "help"|*)
        show_help
        ;;
esac 