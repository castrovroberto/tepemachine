#!/bin/bash

# VeriBoard Monitoring Script
# Real-time monitoring and troubleshooting for minikube deployment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Services to monitor
SERVICES=("customer" "fraud" "notification" "apiwg")

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

# Watch pod status in real-time
watch_pods() {
    print_info "Watching pod status (Press Ctrl+C to stop)"
    watch -n 2 'kubectl get pods -o wide'
}

# Monitor logs for all services
monitor_all_logs() {
    print_header "MONITORING ALL SERVICE LOGS"
    
    # Create a temporary directory for log files
    local log_dir="/tmp/veriboard-logs-$(date +%s)"
    mkdir -p "$log_dir"
    
    print_info "Starting log monitoring for all services..."
    print_info "Log files will be saved in: $log_dir"
    
    # Start monitoring each service in background
    for service in "${SERVICES[@]}"; do
        local pod_name=$(kubectl get pods -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
        
        if [ -n "$pod_name" ]; then
            print_info "Monitoring $service (pod: $pod_name)"
            kubectl logs -f $pod_name > "$log_dir/${service}.log" 2>&1 &
        else
            print_warning "No pod found for $service"
        fi
    done
    
    echo ""
    print_info "All services are being monitored. Logs are being saved to $log_dir"
    print_info "Press Ctrl+C to stop monitoring"
    
    # Wait for user to stop
    trap 'echo ""; print_info "Stopping log monitoring..."; jobs -p | xargs -r kill; exit 0' INT
    wait
}

# Show resource usage
show_resources() {
    print_header "RESOURCE USAGE"
    
    echo "Node resource usage:"
    kubectl top nodes 2>/dev/null || print_warning "Metrics server not available"
    
    echo ""
    echo "Pod resource usage:"
    kubectl top pods 2>/dev/null || print_warning "Metrics server not available"
    
    echo ""
    echo "Storage usage:"
    kubectl get pv,pvc
}

# Tail logs from multiple services
tail_logs() {
    local services_to_tail=("${SERVICES[@]}")
    
    if [ $# -gt 0 ]; then
        services_to_tail=("$@")
    fi
    
    print_header "TAILING LOGS FOR: ${services_to_tail[*]}"
    
    # Check if any services have pods
    local available_services=()
    for service in "${services_to_tail[@]}"; do
        local pod_name=$(kubectl get pods -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
        if [ -n "$pod_name" ]; then
            available_services+=("$service")
        else
            print_warning "No pods found for service: $service"
        fi
    done
    
    if [ ${#available_services[@]} -eq 0 ]; then
        print_error "No pods found for any of the specified services"
        return 1
    fi
    
    # Use multitail if available, otherwise use enhanced fallback
    # Temporarily disable multitail due to terminal size issues
    if false && command -v multitail &> /dev/null; then
        # Check terminal size - multitail needs reasonable dimensions
        local term_cols=$(tput cols 2>/dev/null || echo "80")
        local term_lines=$(tput lines 2>/dev/null || echo "24")
        
        # Need at least 80 columns and 10 lines for multitail to work properly
        if [ "$term_cols" -lt 80 ] || [ "$term_lines" -lt 10 ]; then
            print_warning "Terminal too small for multitail (${term_cols}x${term_lines}), using fallback mode"
        else
            local multitail_args=()
            
            for service in "${available_services[@]}"; do
                local pod_name=$(kubectl get pods -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
                
                # Debug: show what we're adding
                print_info "Adding $service (pod: $pod_name) to multitail"
                
                # Add the command to follow logs for this pod
                multitail_args+=("-l")
                multitail_args+=("kubectl logs -f $pod_name")
                multitail_args+=("-t")
                multitail_args+=("$service")
            done
            
            print_info "Starting multitail for ${#available_services[@]} services..."
            
            # Try multitail and exit if successful
            if multitail "${multitail_args[@]}" 2>/dev/null; then
                return 0  # multitail succeeded, exit function
            else
                print_warning "multitail failed, using fallback mode"
            fi
        fi
    fi
    
    # Enhanced fallback mode (used when multitail not available or failed)
    print_warning "Using enhanced fallback mode"
    print_info "For best experience: install multitail and use a larger terminal"
    
    if [ ${#available_services[@]} -eq 1 ]; then
        # Single service - just follow its logs
        local service="${available_services[0]}"
        local pod_name=$(kubectl get pods -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
        print_info "Following logs for $service (pod: $pod_name)"
        kubectl logs -f $pod_name
    else
        # Multiple services - offer options
        echo ""
        print_info "Multiple services detected. Choose an option:"
        echo "  1. Follow logs for the first service (${available_services[0]})"
        echo "  2. Show recent logs for all services (non-interactive)"
        echo "  3. Choose a specific service to follow"
        echo ""
        read -p "Enter choice [1-3]: " choice
        
        case $choice in
            "1"|"")
                local service="${available_services[0]}"
                local pod_name=$(kubectl get pods -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
                print_info "Following logs for $service (pod: $pod_name)"
                kubectl logs -f $pod_name
                ;;
            "2")
                print_info "Showing recent logs for all services..."
                for service in "${available_services[@]}"; do
                    local pod_name=$(kubectl get pods -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
                    echo ""
                    print_header "RECENT LOGS: $service (pod: $pod_name)"
                    kubectl logs $pod_name --tail=20
                    echo ""
                done
                ;;
            "3")
                echo ""
                print_info "Available services:"
                for i in "${!available_services[@]}"; do
                    echo "  $((i+1)). ${available_services[i]}"
                done
                echo ""
                read -p "Enter service number: " service_num
                
                if [[ "$service_num" =~ ^[0-9]+$ ]] && [ "$service_num" -ge 1 ] && [ "$service_num" -le "${#available_services[@]}" ]; then
                    local service="${available_services[$((service_num-1))]}"
                    local pod_name=$(kubectl get pods -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
                    print_info "Following logs for $service (pod: $pod_name)"
                    kubectl logs -f $pod_name
                else
                    print_error "Invalid selection"
                    return 1
                fi
                ;;
            *)
                print_error "Invalid choice"
                return 1
                ;;
        esac
    fi
}

# Get events for troubleshooting
show_events() {
    print_header "RECENT KUBERNETES EVENTS"
    kubectl get events --sort-by='.lastTimestamp' | tail -20
}

# Describe problematic pods
describe_pods() {
    print_header "DESCRIBING PROBLEMATIC PODS"
    
    for service in "${SERVICES[@]}"; do
        local pod_name=$(kubectl get pods -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
        
        if [ -n "$pod_name" ]; then
            local pod_status=$(kubectl get pod $pod_name -o jsonpath='{.status.phase}')
            local ready_status=$(kubectl get pod $pod_name -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}')
            
            if [ "$pod_status" != "Running" ] || [ "$ready_status" != "True" ]; then
                echo ""
                print_warning "Describing problematic pod: $pod_name ($service)"
                kubectl describe pod $pod_name
                echo ""
                echo "----------------------------------------"
            fi
        fi
    done
}

# Quick health dashboard
dashboard() {
    while true; do
        clear
        print_header "VERIBOARD HEALTH DASHBOARD - $(date)"
        
        echo ""
        echo "Pod Status:"
        kubectl get pods --no-headers | while read line; do
            local name=$(echo $line | awk '{print $1}')
            local ready=$(echo $line | awk '{print $2}')
            local status=$(echo $line | awk '{print $3}')
            local restarts=$(echo $line | awk '{print $4}')
            
            if [[ "$status" == "Running" ]] && [[ "$ready" == "1/1" ]]; then
                echo -e "  ${GREEN}✓${NC} $name - $status ($ready)"
            elif [[ "$status" == "Running" ]]; then
                echo -e "  ${YELLOW}⚠${NC} $name - $status ($ready)"
            else
                echo -e "  ${RED}✗${NC} $name - $status ($ready) - Restarts: $restarts"
            fi
        done
        
        echo ""
        echo "Service Status:"
        for service in "${SERVICES[@]}"; do
            local pod_name=$(kubectl get pods -l app=$service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
            
            if [ -n "$pod_name" ]; then
                local pod_status=$(kubectl get pod $pod_name -o jsonpath='{.status.phase}')
                local ready_status=$(kubectl get pod $pod_name -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}')
                
                if [ "$pod_status" = "Running" ] && [ "$ready_status" = "True" ]; then
                    echo -e "  ${GREEN}✓${NC} $service: Healthy"
                else
                    echo -e "  ${YELLOW}⚠${NC} $service: $pod_status (Ready: $ready_status)"
                fi
            else
                echo -e "  ${RED}✗${NC} $service: No pods found"
            fi
        done
        
        echo ""
        echo "Recent Events (last 3):"
        kubectl get events --sort-by='.lastTimestamp' --no-headers | tail -3 | while read line; do
            echo "  $line"
        done
        
        echo ""
        print_info "Auto-refreshing every 5 seconds... (Press Ctrl+C to stop)"
        sleep 5
    done
}

# Show help
show_help() {
    echo "VeriBoard Monitoring Script"
    echo ""
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  watch              Watch pod status in real-time"
    echo "  monitor-logs       Monitor logs for all services"
    echo "  tail [SERVICES]    Tail logs for specific services (enhanced)"
    echo "  resources          Show resource usage"
    echo "  events             Show recent Kubernetes events"
    echo "  describe           Describe problematic pods"
    echo "  dashboard          Real-time health dashboard"
    echo "  help               Show this help message"
    echo ""
    echo "Tail Command Details:"
    echo "  - With multitail: Shows logs for all specified services simultaneously"
    echo "  - Without multitail: Interactive mode for multiple services with options:"
    echo "    1. Follow logs for first service (live)"
    echo "    2. Show recent logs for all services (snapshot)"
    echo "    3. Choose specific service to follow (live)"
    echo ""
    echo "Examples:"
    echo "  $0 watch                    # Watch pod status updates"
    echo "  $0 tail                     # Tail logs for all services"
    echo "  $0 tail customer fraud      # Tail logs for specific services"
    echo "  $0 tail notification        # Tail logs for single service"
    echo "  $0 dashboard                # Real-time health dashboard"
    echo "  $0 monitor-logs             # Save logs to files"
    echo ""
    echo "Installation tip:"
    echo "  sudo apt-get install multitail  # For enhanced multi-service log viewing"
}

# Main script logic
case "${1:-help}" in
    "watch")
        watch_pods
        ;;
    "monitor-logs")
        monitor_all_logs
        ;;
    "tail")
        shift
        tail_logs "$@"
        ;;
    "resources")
        show_resources
        ;;
    "events")
        show_events
        ;;
    "describe")
        describe_pods
        ;;
    "dashboard")
        dashboard
        ;;
    "help"|*)
        show_help
        ;;
esac 