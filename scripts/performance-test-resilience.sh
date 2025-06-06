#!/bin/bash

# Performance Testing Script for Resilience4j Circuit Breaker Configurations
# This script tests various load scenarios to observe circuit breaker behavior

set -e

# Configuration
CUSTOMER_SERVICE_URL="http://localhost:8080"
FRAUD_SERVICE_URL="http://localhost:8081"
NOTIFICATION_SERVICE_URL="http://localhost:8082"
API_GATEWAY_URL="http://localhost:8083"

# Test parameters
CONCURRENT_USERS=(5 10 20 50)
TEST_DURATION="60s"
RAMP_UP_TIME="10s"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check if required tools are installed
check_prerequisites() {
    log "Checking prerequisites..."
    
    if ! command -v curl &> /dev/null; then
        error "curl is required but not installed"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        warning "jq is not installed. JSON parsing will be limited"
    fi
    
    if ! command -v ab &> /dev/null; then
        warning "Apache Bench (ab) is not installed. Some tests will be skipped"
    fi
    
    success "Prerequisites check completed"
}

# Check service health
check_service_health() {
    local service_name=$1
    local service_url=$2
    
    log "Checking health of $service_name at $service_url"
    
    if curl -s -f "$service_url/actuator/health" > /dev/null; then
        success "$service_name is healthy"
        return 0
    else
        error "$service_name is not healthy or not responding"
        return 1
    fi
}

# Test circuit breaker with normal load
test_normal_load() {
    local service_name=$1
    local endpoint=$2
    local concurrent_users=$3
    
    log "Testing $service_name with normal load ($concurrent_users concurrent users)"
    
    # Create test data
    local test_data='{"firstName":"TestUser","lastName":"LoadTest","email":"test@loadtest.com"}'
    
    # Run load test
    if command -v ab &> /dev/null; then
        log "Running Apache Bench test..."
        ab -n 100 -c $concurrent_users -T 'application/json' -p <(echo "$test_data") "$endpoint" > "results/normal_load_${service_name}_${concurrent_users}users.txt" 2>&1
        
        # Extract key metrics
        local success_rate=$(grep "Complete requests:" "results/normal_load_${service_name}_${concurrent_users}users.txt" | awk '{print $3}')
        local failed_requests=$(grep "Failed requests:" "results/normal_load_${service_name}_${concurrent_users}users.txt" | awk '{print $3}')
        
        log "Results: $success_rate successful requests, $failed_requests failed requests"
    else
        # Fallback to curl-based testing
        log "Running curl-based load test..."
        for i in $(seq 1 50); do
            curl -s -X POST -H "Content-Type: application/json" -d "$test_data" "$endpoint" > /dev/null &
        done
        wait
        success "Curl-based load test completed"
    fi
}

# Test circuit breaker with high error rate
test_high_error_rate() {
    local service_name=$1
    local endpoint=$2
    
    log "Testing $service_name circuit breaker with high error rate"
    
    # Send requests to trigger errors (using invalid data)
    local invalid_data='{"firstName":"","lastName":"","email":"invalid-email"}'
    
    for i in $(seq 1 20); do
        response=$(curl -s -w "%{http_code}" -X POST -H "Content-Type: application/json" -d "$invalid_data" "$endpoint" -o /dev/null)
        echo "Request $i: HTTP $response"
        sleep 0.1
    done
    
    log "High error rate test completed. Check circuit breaker metrics."
}

# Test circuit breaker recovery
test_circuit_breaker_recovery() {
    local service_name=$1
    local health_endpoint=$2
    
    log "Testing $service_name circuit breaker recovery"
    
    # Wait for circuit breaker to potentially open
    log "Waiting 30 seconds for circuit breaker state changes..."
    sleep 30
    
    # Check circuit breaker metrics
    if curl -s "$health_endpoint" | jq -r '.components.circuitBreakers' > /dev/null 2>&1; then
        log "Circuit breaker metrics available"
        curl -s "$health_endpoint" | jq '.components.circuitBreakers'
    else
        log "Circuit breaker metrics not available or jq not installed"
    fi
    
    # Test recovery with valid requests
    log "Sending valid requests to test recovery..."
    local valid_data='{"firstName":"Recovery","lastName":"Test","email":"recovery@test.com"}'
    
    for i in $(seq 1 10); do
        response=$(curl -s -w "%{http_code}" -X POST -H "Content-Type: application/json" -d "$valid_data" "$1" -o /dev/null)
        echo "Recovery request $i: HTTP $response"
        sleep 2
    done
}

# Test timeout scenarios
test_timeout_scenarios() {
    local service_name=$1
    local endpoint=$2
    
    log "Testing $service_name timeout scenarios"
    
    # Send requests with artificial delays (if supported by the service)
    for i in $(seq 1 5); do
        log "Sending request $i with potential timeout..."
        timeout 15s curl -X POST -H "Content-Type: application/json" \
            -d '{"firstName":"Timeout","lastName":"Test","email":"timeout@test.com"}' \
            "$endpoint" || echo "Request $i timed out or failed"
        sleep 1
    done
}

# Generate load test report
generate_report() {
    log "Generating performance test report..."
    
    local report_file="results/resilience_performance_report_$(date +%Y%m%d_%H%M%S).md"
    
    cat > "$report_file" << EOF
# Resilience4j Performance Test Report

**Test Date:** $(date)
**Test Duration:** $TEST_DURATION
**Concurrent Users Tested:** ${CONCURRENT_USERS[*]}

## Test Summary

### Services Tested
- Customer Service: $CUSTOMER_SERVICE_URL
- Fraud Service: $FRAUD_SERVICE_URL
- Notification Service: $NOTIFICATION_SERVICE_URL
- API Gateway: $API_GATEWAY_URL

### Test Scenarios
1. **Normal Load Testing**: Tested with varying concurrent users (${CONCURRENT_USERS[*]})
2. **High Error Rate Testing**: Triggered circuit breaker with invalid requests
3. **Circuit Breaker Recovery**: Tested recovery after circuit breaker activation
4. **Timeout Scenarios**: Tested time limiter behavior

### Key Metrics Observed
- Request success rates under normal load
- Circuit breaker activation thresholds
- Recovery time after circuit breaker opens
- Fallback method execution
- Service degradation handling

### Recommendations
1. Monitor circuit breaker metrics in production
2. Adjust failure rate thresholds based on observed patterns
3. Implement proper alerting for circuit breaker state changes
4. Consider implementing bulkhead patterns for resource isolation

### Detailed Results
See individual test result files in the results/ directory:
EOF

    # Add links to detailed result files
    for file in results/*.txt; do
        if [ -f "$file" ]; then
            echo "- [$(basename "$file")]($file)" >> "$report_file"
        fi
    done
    
    success "Report generated: $report_file"
}

# Main test execution
main() {
    log "Starting Resilience4j Performance Testing"
    
    # Create results directory
    mkdir -p results
    
    # Check prerequisites
    check_prerequisites
    
    # Check service health
    check_service_health "Customer Service" "$CUSTOMER_SERVICE_URL"
    check_service_health "Fraud Service" "$FRAUD_SERVICE_URL"
    check_service_health "Notification Service" "$NOTIFICATION_SERVICE_URL"
    
    # Test scenarios
    log "=== Starting Test Scenarios ==="
    
    # Test 1: Normal load testing
    for users in "${CONCURRENT_USERS[@]}"; do
        test_normal_load "customer" "$CUSTOMER_SERVICE_URL/api/v1/customers" $users
        sleep 5
    done
    
    # Test 2: High error rate testing
    test_high_error_rate "customer" "$CUSTOMER_SERVICE_URL/api/v1/customers"
    
    # Test 3: Circuit breaker recovery
    test_circuit_breaker_recovery "$CUSTOMER_SERVICE_URL/api/v1/customers" "$CUSTOMER_SERVICE_URL/actuator/health"
    
    # Test 4: Timeout scenarios
    test_timeout_scenarios "customer" "$CUSTOMER_SERVICE_URL/api/v1/customers"
    
    # Test fraud service
    log "=== Testing Fraud Service ==="
    for i in $(seq 1 20); do
        response=$(curl -s -w "%{http_code}" "$FRAUD_SERVICE_URL/api/v1/fraud-check/$i" -o /dev/null)
        echo "Fraud check $i: HTTP $response"
        sleep 0.2
    done
    
    # Generate report
    generate_report
    
    success "Performance testing completed. Check the results/ directory for detailed reports."
}

# Script execution
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi 