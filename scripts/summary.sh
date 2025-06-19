#!/bin/bash

# VeriBoard Summary Script
# Quick overview of the entire VeriBoard deployment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Services to check
SERVICES=("customer" "fraud" "notification" "apiwg")
INFRASTRUCTURE=("postgres" "rabbitmq" "zipkin")

print_header() {
    echo -e "${CYAN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║                                 VERIBOARD                                    ║${NC}"
    echo -e "${CYAN}║                           Deployment Summary                                 ║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
    echo -e "${CYAN}$(date)${NC}"
    echo ""
}

print_section() {
    echo -e "${BLUE}▼ $1${NC}"
    echo -e "${BLUE}────────────────────────────────────────────────────────────────────────────${NC}"
}

print_success() {
    echo -e "${GREEN}  ✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}  ⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}  ✗ $1${NC}"
}

print_info() {
    echo -e "${PURPLE}  ℹ $1${NC}"
}

# Check minikube status
check_minikube() {
    print_section "MINIKUBE STATUS"
    
    if minikube status > /dev/null 2>&1; then
        local ip=$(minikube ip 2>/dev/null)
        print_success "Minikube is running (IP: $ip)"
        
        # Check addons
        local ingress_status=$(minikube addons list | grep ingress | awk '{print $3}')
        if [ "$ingress_status" = "enabled" ]; then
            print_success "Ingress addon enabled"
        else
            print_warning "Ingress addon disabled"
        fi
        
        local metrics_status=$(minikube addons list | grep metrics-server | awk '{print $3}')
        if [ "$metrics_status" = "enabled" ]; then
            print_success "Metrics server enabled"
        else
            print_warning "Metrics server disabled"
        fi
        
    else
        print_error "Minikube is not running"
        return 1
    fi
    echo ""
}

# Check infrastructure status
check_infrastructure() {
    print_section "INFRASTRUCTURE SERVICES"
    
    for service in "${INFRASTRUCTURE[@]}"; do
        local pod_name=$(kubectl get pods -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
        
        if [ -n "$pod_name" ]; then
            local pod_status=$(kubectl get pod $pod_name -o jsonpath='{.status.phase}')
            local ready_status=$(kubectl get pod $pod_name -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}')
            local restarts=$(kubectl get pod $pod_name -o jsonpath='{.status.containerStatuses[0].restartCount}')
            
            if [ "$pod_status" = "Running" ] && [ "$ready_status" = "True" ]; then
                print_success "$service: Healthy (restarts: $restarts)"
            else
                print_warning "$service: $pod_status (Ready: $ready_status, restarts: $restarts)"
            fi
        else
            print_error "$service: Not deployed"
        fi
    done
    echo ""
}

# Check application services
check_services() {
    print_section "APPLICATION SERVICES"
    
    for service in "${SERVICES[@]}"; do
        local pod_name=$(kubectl get pods -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
        
        if [ -n "$pod_name" ]; then
            local pod_status=$(kubectl get pod $pod_name -o jsonpath='{.status.phase}')
            local ready_status=$(kubectl get pod $pod_name -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}')
            local restarts=$(kubectl get pod $pod_name -o jsonpath='{.status.containerStatuses[0].restartCount}')
            local image=$(kubectl get pod $pod_name -o jsonpath='{.status.containerStatuses[0].image}')
            
            if [ "$pod_status" = "Running" ] && [ "$ready_status" = "True" ]; then
                print_success "$service: Healthy (restarts: $restarts)"
                print_info "  Image: $image"
            else
                print_warning "$service: $pod_status (Ready: $ready_status, restarts: $restarts)"
                print_info "  Image: $image"
            fi
        else
            print_error "$service: Not deployed"
        fi
    done
    echo ""
}

# Check databases
check_databases() {
    print_section "DATABASES"
    
    # Check if postgres is available
    local postgres_pod=$(kubectl get pods -l app=postgres -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
    
    if [ -n "$postgres_pod" ]; then
        # List databases
        local databases=$(kubectl exec -it $postgres_pod -- psql -U yumptech -d postgres -t -c "SELECT datname FROM pg_database WHERE datistemplate = false;" 2>/dev/null | grep -v "^\s*$" | tr -d '\r' | xargs)
        
        print_info "PostgreSQL databases:"
        for db in $databases; do
            if [[ " customer fraud notification " =~ " $db " ]]; then
                print_success "  $db"
            else
                print_info "  $db (system)"
            fi
        done
    else
        print_error "PostgreSQL not available"
    fi
    echo ""
}

# Check resource usage
check_resources() {
    print_section "RESOURCE USAGE"
    
    # Node resources
    if kubectl top nodes > /dev/null 2>&1; then
        local node_info=$(kubectl top nodes --no-headers | head -1)
        print_info "Node: $node_info"
        
        # Pod resources (top 5)
        print_info "Top resource consumers:"
        kubectl top pods --no-headers | sort -k3 -nr | head -5 | while read line; do
            print_info "  $line"
        done
    else
        print_warning "Metrics server not available"
    fi
    echo ""
}

# Check networking
check_networking() {
    print_section "NETWORKING"
    
    # Services
    print_info "Services:"
    kubectl get services --no-headers | while read line; do
        local name=$(echo $line | awk '{print $1}')
        local type=$(echo $line | awk '{print $2}')
        local cluster_ip=$(echo $line | awk '{print $3}')
        local ports=$(echo $line | awk '{print $5}')
        
        if [ "$name" != "kubernetes" ]; then
            print_info "  $name ($type): $cluster_ip - $ports"
        fi
    done
    
    echo ""
    
    # Port forwarding status
    print_info "Port forwarding status:"
    local port_status=$(./scripts/port-forward.sh status 2>/dev/null | grep -E "(customer|fraud|notification|apiwg|zipkin|rabbitmq)" | wc -l)
    local active_forwards=$(./scripts/port-forward.sh status 2>/dev/null | grep "Active" | wc -l)
    
    if [ "$active_forwards" -gt 0 ]; then
        print_success "  $active_forwards services forwarded locally"
    else
        print_warning "  No services forwarded locally"
        print_info "  Run: ./scripts/port-forward.sh start-all"
    fi
    echo ""
}

# Check recent events
check_events() {
    print_section "RECENT EVENTS (Last 5)"
    
    kubectl get events --sort-by='.lastTimestamp' --no-headers | tail -5 | while read line; do
        local event_type=$(echo $line | awk '{print $3}')
        local message=$(echo $line | cut -d' ' -f7-)
        
        if [ "$event_type" = "Warning" ]; then
            print_warning "$message"
        else
            print_info "$message"
        fi
    done
    echo ""
}

# Generate recommendations
generate_recommendations() {
    print_section "RECOMMENDATIONS"
    
    local recommendations=()
    
    # Check if any services are not ready
    local not_ready_count=0
    for service in "${SERVICES[@]}"; do
        local pod_name=$(kubectl get pods -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
        if [ -n "$pod_name" ]; then
            local ready_status=$(kubectl get pod $pod_name -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}')
            if [ "$ready_status" != "True" ]; then
                ((not_ready_count++))
            fi
        fi
    done
    
    if [ $not_ready_count -gt 0 ]; then
        recommendations+=("Fix $not_ready_count unhealthy services: ./scripts/minikube-deploy.sh health")
        recommendations+=("Check logs: ./scripts/minikube-deploy.sh logs <service-name>")
    fi
    
    # Check if port forwarding is not active
    local active_forwards=$(./scripts/port-forward.sh status 2>/dev/null | grep "Active" | wc -l)
    if [ "$active_forwards" -eq 0 ]; then
        recommendations+=("Enable local access: ./scripts/port-forward.sh start-all")
    fi
    
    # Check if metrics server is disabled
    local metrics_status=$(minikube addons list | grep metrics-server | awk '{print $3}')
    if [ "$metrics_status" != "enabled" ]; then
        recommendations+=("Enable resource monitoring: minikube addons enable metrics-server")
    fi
    
    # Display recommendations
    if [ ${#recommendations[@]} -eq 0 ]; then
        print_success "All systems are running optimally!"
        print_info "Try the monitoring dashboard: ./scripts/monitor.sh dashboard"
    else
        for rec in "${recommendations[@]}"; do
            print_warning "$rec"
        done
    fi
    echo ""
}

# Show quick access commands
show_quick_commands() {
    print_section "QUICK COMMANDS"
    
    echo -e "${CYAN}Monitoring:${NC}"
    print_info "./scripts/monitor.sh dashboard       - Real-time dashboard"
    print_info "./scripts/minikube-deploy.sh status  - Current status"
    print_info "./scripts/minikube-deploy.sh health  - Health check"
    
    echo ""
    echo -e "${CYAN}Management:${NC}"
    print_info "./scripts/minikube-deploy.sh quick-update <service>  - Update service"
    print_info "./scripts/minikube-deploy.sh restart <service>       - Restart service"
    print_info "./scripts/minikube-deploy.sh logs <service>          - View logs"
    
    echo ""
    echo -e "${CYAN}Access:${NC}"
    print_info "./scripts/port-forward.sh start-all  - Access all services locally"
    print_info "./scripts/port-forward.sh start <service>  - Access specific service"
    
    echo ""
}

# Main execution
main() {
    clear
    print_header
    
    check_minikube || exit 1
    check_infrastructure
    check_services
    #check_databases
    check_resources
    check_networking
    check_events
    generate_recommendations
    show_quick_commands
    
    echo -e "${CYAN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║                            Summary Complete                                   ║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
}

# Run the summary
main 