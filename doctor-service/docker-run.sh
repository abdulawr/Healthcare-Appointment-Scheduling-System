#!/bin/bash

# Docker Management Script for Doctor Service
# Provides commands to build, run, and manage the Docker containers

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

show_help() {
    cat << EOF
Doctor Service - Docker Management Script

Usage: ./docker-run.sh [COMMAND]

Commands:
    build           Build Docker images
    up              Start all services
    down            Stop all services
    restart         Restart all services
    logs            Show logs from all services
    logs-app        Show logs from doctor-service only
    logs-db         Show logs from database only
    status          Show status of all services
    clean           Remove all containers and volumes
    test            Run integration tests
    psql            Connect to PostgreSQL database
    help            Show this help message

Examples:
    ./docker-run.sh build       # Build images
    ./docker-run.sh up          # Start services
    ./docker-run.sh logs-app    # View application logs
    ./docker-run.sh clean       # Clean everything

EOF
}

build_images() {
    print_header "Building Docker Images"
    docker-compose build
    print_success "Images built successfully!"
}

start_services() {
    print_header "Starting Services"
    docker-compose up -d

    echo ""
    print_info "Waiting for services to be healthy..."
    sleep 10

    echo ""
    print_success "Services started!"
    echo ""
    echo "🌐 Application: http://localhost:8082"
    echo "📖 Swagger UI: http://localhost:8082/q/swagger-ui"
    echo "💚 Health Check: http://localhost:8082/q/health"
    echo "🗄️  pgAdmin: http://localhost:5050 (admin@hospital.com / admin)"
}

stop_services() {
    print_header "Stopping Services"
    docker-compose down
    print_success "Services stopped!"
}

restart_services() {
    print_header "Restarting Services"
    docker-compose restart
    print_success "Services restarted!"
}

show_logs() {
    print_header "Service Logs"
    docker-compose logs -f
}

show_app_logs() {
    print_header "Doctor Service Logs"
    docker-compose logs -f doctor-service
}

show_db_logs() {
    print_header "Database Logs"
    docker-compose logs -f postgres
}

show_status() {
    print_header "Service Status"
    docker-compose ps
}

clean_all() {
    print_header "Cleaning Up"
    print_info "This will remove all containers, networks, and volumes"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose down -v
        print_success "Cleanup complete!"
    else
        print_info "Cleanup cancelled"
    fi
}

run_tests() {
    print_header "Running Integration Tests"
    print_info "Make sure services are running first!"
    mvn test -Dtest=DoctorIntegrationTest
}

connect_psql() {
    print_header "Connecting to PostgreSQL"
    docker exec -it doctor-service-db psql -U doctor_user -d doctordb
}

# Main script logic
case "${1:-help}" in
    build)
        build_images
        ;;
    up)
        start_services
        ;;
    down)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    logs)
        show_logs
        ;;
    logs-app)
        show_app_logs
        ;;
    logs-db)
        show_db_logs
        ;;
    status)
        show_status
        ;;
    clean)
        clean_all
        ;;
    test)
        run_tests
        ;;
    psql)
        connect_psql
        ;;
    help|*)
        show_help
        ;;
esac