#!/bin/bash

# VeriBoard Port Forward Script
# Easy access to services via port forwarding

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

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

# Service configurations
declare -A SERVICES
SERVICES[customer]="8080"
SERVICES[fraud]="8081"
SERVICES[notification]="8082"
SERVICES[apiwg]="8083"
SERVICES[zipkin]="9411"
SERVICES[rabbitmq]="15672"

# Start port forwarding for a specific service
start_port_forward() {
    local service=$1
    local port=${SERVICES[$service]}
    
    if [ -z "$port" ]; then
        print_error "Unknown service: $service"
        echo "Available services: ${!SERVICES[@]}"
        return 1
    fi
    
    print_info "Starting port forward for $service on port $port"
    print_info "Access at: http://localhost:$port"
    print_info "Press Ctrl+C to stop"
    
    kubectl port-forward service/$service $port:$port
}

# Start port forwarding for all services
start_all() {
    print_info "Starting port forwarding for all services..."
    
    # Start each service in background
    for service in "${!SERVICES[@]}"; do
        local port=${SERVICES[$service]}
        
        # Check if port is already in use
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            print_warning "Port $port is already in use, skipping $service"
            continue
        fi
        
        print_info "Starting $service on port $port"
        kubectl port-forward service/$service $port:$port > /dev/null 2>&1 &
        
        # Store the PID for cleanup
        echo $! >> /tmp/veriboard-port-forwards.pids
    done
    
    echo ""
    print_success "Port forwarding started for all available services"
    echo ""
    echo "Service URLs:"
    echo "  Customer Service:    http://localhost:8080"
    echo "  Fraud Service:       http://localhost:8081"
    echo "  Notification Service: http://localhost:8082"
    echo "  API Gateway:         http://localhost:8083"
    echo "  Zipkin UI:           http://localhost:9411"
    echo "  RabbitMQ Management: http://localhost:15672"
    echo ""
    print_info "To stop all port forwards, run: $0 stop-all"
    print_info "Or press Ctrl+C and then run: $0 cleanup"
    
    # Wait for user interrupt
    trap 'print_info "Stopping port forwards..."; stop_all; exit 0' INT
    
    # Wait indefinitely
    while true; do
        sleep 1
    done
}

# Stop all port forwards
stop_all() {
    print_info "Stopping all port forwards..."
    
    if [ -f /tmp/veriboard-port-forwards.pids ]; then
        while read -r pid; do
            if kill "$pid" 2>/dev/null; then
                print_success "Stopped process $pid"
            fi
        done < /tmp/veriboard-port-forwards.pids
        
        rm -f /tmp/veriboard-port-forwards.pids
    fi
    
    # Also kill any kubectl port-forward processes for our services
    for service in "${!SERVICES[@]}"; do
        local port=${SERVICES[$service]}
        local pid=$(lsof -ti:$port 2>/dev/null | head -1)
        if [ -n "$pid" ]; then
            if kill "$pid" 2>/dev/null; then
                print_success "Stopped port forward for $service (port $port)"
            fi
        fi
    done
    
    print_success "All port forwards stopped"
}

# Cleanup any leftover processes
cleanup() {
    print_info "Cleaning up any leftover port forward processes..."
    
    # Kill kubectl port-forward processes
    pkill -f "kubectl port-forward" 2>/dev/null && print_success "Stopped kubectl port-forward processes" || true
    
    # Remove PID file
    rm -f /tmp/veriboard-port-forwards.pids
    
    print_success "Cleanup completed"
}

# Show status of port forwards
show_status() {
    echo "Port Forward Status:"
    echo ""
    
    for service in "${!SERVICES[@]}"; do
        local port=${SERVICES[$service]}
        
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            local pid=$(lsof -ti:$port 2>/dev/null | head -1)
            print_success "$service: Active on port $port (PID: $pid)"
        else
            print_warning "$service: Not forwarded"
        fi
    done
}

# Show help
show_help() {
    echo "VeriBoard Port Forward Script"
    echo ""
    echo "Usage: $0 [COMMAND] [SERVICE]"
    echo ""
    echo "Commands:"
    echo "  start [SERVICE]     Start port forwarding for a specific service"
    echo "  start-all           Start port forwarding for all services"
    echo "  stop-all            Stop all port forwards"
    echo "  status              Show status of port forwards"
    echo "  cleanup             Clean up any leftover processes"
    echo "  help                Show this help message"
    echo ""
    echo "Available services:"
    for service in "${!SERVICES[@]}"; do
        local port=${SERVICES[$service]}
        echo "  $service (port $port)"
    done
    echo ""
    echo "Examples:"
    echo "  $0 start customer"
    echo "  $0 start-all"
    echo "  $0 status"
    echo "  $0 stop-all"
}

# Main script logic
case "${1:-help}" in
    "start")
        if [ -z "$2" ]; then
            echo "Available services: ${!SERVICES[@]}"
            read -p "Enter service name: " service
        else
            service="$2"
        fi
        start_port_forward "$service"
        ;;
    "start-all")
        start_all
        ;;
    "stop-all")
        stop_all
        ;;
    "status")
        show_status
        ;;
    "cleanup")
        cleanup
        ;;
    "help"|*)
        show_help
        ;;
esac 