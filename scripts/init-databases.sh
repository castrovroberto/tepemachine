#!/bin/bash

# Database Initialization Script for VeriBoard
# This script creates databases and runs initialization scripts

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SERVICES=("customer" "fraud" "notification")
POSTGRES_USER="yumptech"
POSTGRES_DB="yumptech"
POSTGRES_POD="postgres-0"

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
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Wait for PostgreSQL to be ready
wait_for_postgres() {
    print_info "Waiting for PostgreSQL to be ready..."
    kubectl wait --for=condition=Ready pod/$POSTGRES_POD --timeout=300s
    
    # Additional check to ensure PostgreSQL is accepting connections
    local retries=0
    local max_retries=30
    
    while [ $retries -lt $max_retries ]; do
        if kubectl exec $POSTGRES_POD -- pg_isready -U $POSTGRES_USER > /dev/null 2>&1; then
            print_success "PostgreSQL is ready and accepting connections"
            return 0
        fi
        
        print_info "Waiting for PostgreSQL to accept connections... (attempt $((retries + 1))/$max_retries)"
        sleep 2
        retries=$((retries + 1))
    done
    
    print_error "PostgreSQL did not become ready within the timeout period"
    return 1
}

# Create databases
create_databases() {
    print_header "CREATING DATABASES"
    
    for service in "${SERVICES[@]}"; do
        print_info "Creating database: $service"
        
        # Try to create the database, ignore if it already exists
        if kubectl exec $POSTGRES_POD -- psql -U $POSTGRES_USER -d $POSTGRES_DB -c "CREATE DATABASE $service;" 2>/dev/null; then
            print_success "Database '$service' created successfully"
        else
            print_warning "Database '$service' might already exist or there was an error"
        fi
        
        # Grant permissions
        kubectl exec $POSTGRES_POD -- psql -U $POSTGRES_USER -d $POSTGRES_DB -c "GRANT ALL PRIVILEGES ON DATABASE $service TO $POSTGRES_USER;" 2>/dev/null || true
    done
}

# List existing databases
list_databases() {
    print_header "EXISTING DATABASES"
    kubectl exec $POSTGRES_POD -- psql -U $POSTGRES_USER -d $POSTGRES_DB -c "\l"
}

# Run SQL script on a specific database
run_sql_script() {
    local database=$1
    local script_file=$2
    
    if [ -z "$database" ] || [ -z "$script_file" ]; then
        print_error "Usage: run_sql_script <database> <script_file>"
        return 1
    fi
    
    if [ ! -f "$script_file" ]; then
        print_error "Script file not found: $script_file"
        return 1
    fi
    
    print_info "Running SQL script '$script_file' on database '$database'"
    
    # Copy the script to the pod and execute it
    kubectl cp "$script_file" $POSTGRES_POD:/tmp/script.sql
    kubectl exec $POSTGRES_POD -- psql -U $POSTGRES_USER -d $database -f /tmp/script.sql
    kubectl exec $POSTGRES_POD -- rm /tmp/script.sql
    
    print_success "SQL script executed successfully"
}

# Create sample data
create_sample_data() {
    print_header "CREATING SAMPLE DATA"
    
    # Customer sample data
    print_info "Creating sample customers..."
    kubectl exec $POSTGRES_POD -- psql -U $POSTGRES_USER -d customer -c "
        CREATE TABLE IF NOT EXISTS customers (
            id BIGSERIAL PRIMARY KEY,
            first_name VARCHAR(255) NOT NULL,
            last_name VARCHAR(255) NOT NULL,
            email VARCHAR(255) UNIQUE NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        
        INSERT INTO customers (first_name, last_name, email) VALUES 
        ('John', 'Doe', 'john.doe@example.com'),
        ('Jane', 'Smith', 'jane.smith@example.com'),
        ('Bob', 'Johnson', 'bob.johnson@example.com')
        ON CONFLICT (email) DO NOTHING;
    " 2>/dev/null || print_warning "Could not create customer sample data"
    
    # Check if data was created
    local customer_count=$(kubectl exec $POSTGRES_POD -- psql -U $POSTGRES_USER -d customer -t -c "SELECT COUNT(*) FROM customers;" 2>/dev/null | tr -d ' \n' || echo "0")
    print_success "Created customer database with $customer_count customers"
}

# Check database connections
check_connections() {
    print_header "CHECKING DATABASE CONNECTIONS"
    
    for service in "${SERVICES[@]}"; do
        print_info "Checking connection to $service database..."
        
        if kubectl exec $POSTGRES_POD -- psql -U $POSTGRES_USER -d $service -c "SELECT 1;" > /dev/null 2>&1; then
            print_success "$service database: Connection OK"
        else
            print_error "$service database: Connection FAILED"
        fi
    done
}

# Reset databases (WARNING: This will delete all data)
reset_databases() {
    print_header "RESETTING DATABASES (WARNING: ALL DATA WILL BE LOST)"
    
    echo -e "${RED}This will delete ALL data in the service databases!${NC}"
    read -p "Are you sure you want to continue? (yes/no): " confirm
    
    if [ "$confirm" != "yes" ]; then
        print_info "Database reset cancelled"
        return 0
    fi
    
    for service in "${SERVICES[@]}"; do
        print_warning "Dropping database: $service"
        kubectl exec $POSTGRES_POD -- psql -U $POSTGRES_USER -d $POSTGRES_DB -c "DROP DATABASE IF EXISTS $service;" 2>/dev/null || true
    done
    
    print_info "Recreating databases..."
    create_databases
    
    print_success "Database reset completed"
}

# Show help
show_help() {
    echo "Database Initialization Script for VeriBoard"
    echo ""
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  wait                Wait for PostgreSQL to be ready"
    echo "  create              Create all service databases"
    echo "  list                List all existing databases"
    echo "  sample              Create sample data for testing"
    echo "  check               Check database connections"
    echo "  reset               Reset all databases (WARNING: deletes all data)"
    echo "  sql <db> <file>     Run SQL script on specific database"
    echo "  help                Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 create           # Create all service databases"
    echo "  $0 sample           # Create sample data"
    echo "  $0 sql customer ./scripts/customer-schema.sql"
    echo ""
    echo "Database Info:"
    echo "  PostgreSQL Pod: $POSTGRES_POD"
    echo "  PostgreSQL User: $POSTGRES_USER"
    echo "  Main Database: $POSTGRES_DB"
    echo "  Service Databases: ${SERVICES[*]}"
}

# Main script logic
case "${1:-help}" in
    "wait")
        wait_for_postgres
        ;;
    "create")
        wait_for_postgres
        create_databases
        ;;
    "list")
        list_databases
        ;;
    "sample")
        wait_for_postgres
        create_sample_data
        ;;
    "check")
        check_connections
        ;;
    "reset")
        reset_databases
        ;;
    "sql")
        if [ $# -lt 3 ]; then
            print_error "Usage: $0 sql <database> <script_file>"
            exit 1
        fi
        run_sql_script "$2" "$3"
        ;;
    "help"|*)
        show_help
        ;;
esac 