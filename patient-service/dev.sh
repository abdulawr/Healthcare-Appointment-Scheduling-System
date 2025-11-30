#!/bin/bash

###############################################################################
# Patient Service - Local Development Script
###############################################################################

set -e

echo "=========================================="
echo "Patient Service - Local Development"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Parse command line arguments
COMMAND="${1:-dev}"

case $COMMAND in
    dev)
        log_info "Starting in development mode..."
        log_warning "This will start the application with live reload"
        echo ""
        mvn quarkus:dev
        ;;

    test)
        log_info "Running all tests..."
        mvn clean test
        ;;

    test-coverage)
        log_info "Running tests with coverage..."
        mvn clean verify
        ;;

    docker)
        log_info "Starting with Docker Compose..."
        docker-compose up -d
        log_success "Services started!"
        echo ""
        echo "Available at:"
        echo "  Application:  http://localhost:8081"
        echo "  Swagger UI:   http://localhost:8081/swagger-ui"
        echo "  Database UI:  http://localhost:8082"
        echo ""
        echo "View logs with: docker-compose logs -f"
        ;;

    docker-logs)
        log_info "Showing Docker logs..."
        docker-compose logs -f
        ;;

    docker-stop)
        log_info "Stopping Docker containers..."
        docker-compose down
        log_success "Containers stopped"
        ;;

    docker-clean)
        log_info "Cleaning Docker containers and volumes..."
        docker-compose down -v
        log_success "Containers and volumes removed"
        ;;

    build)
        log_info "Building application..."
        mvn clean package -DskipTests
        log_success "Build completed!"
        ;;

    *)
        echo "Usage: ./dev.sh [command]"
        echo ""
        echo "Commands:"
        echo "  dev              Start in development mode (default)"
        echo "  test             Run all tests"
        echo "  test-coverage    Run tests with coverage report"
        echo "  docker           Start with Docker Compose"
        echo "  docker-logs      Show Docker logs"
        echo "  docker-stop      Stop Docker containers"
        echo "  docker-clean     Stop and remove containers/volumes"
        echo "  build            Build the application"
        echo ""
        exit 1
        ;;
esac