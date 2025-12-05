# 🏥 Healthcare Appointment Scheduling Service

A production-grade microservice for managing healthcare appointments, built with **Quarkus** and following **Domain-Driven Design (DDD)** and **Event-Driven Architecture (EDA)** principles.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.6.4-blue.svg)](https://quarkus.io/)
[![Tests](https://img.shields.io/badge/Tests-64%20Passing-brightgreen.svg)](/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](/)

---



## 🎯 Overview

This microservice is part of a larger **Healthcare Management System** and handles all appointment-related operations. It demonstrates modern software engineering practices including:

- **Domain-Driven Design (DDD)** - Clear bounded contexts and domain modeling
- **Event-Driven Architecture** - Kafka integration for asynchronous communication
- **RESTful API** - 14 well-documented endpoints
- **Comprehensive Testing** - 64 tests with 100% coverage
- **Reactive Programming** - Non-blocking I/O with SmallRye Reactive Messaging
- **Production-Ready** - Health checks, metrics, and OpenAPI documentation

---

## 🏗️ Architecture

### Layered Architecture

```
┌─────────────────────────────────────────────┐
│          REST API Layer (Resource)          │
│    - AppointmentResource                    │
│    - Exception Mappers                      │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│         Service Layer (Business Logic)      │
│    - AppointmentService                     │
│    - AppointmentServiceImpl                 │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│         Domain Layer (Entities & DTOs)      │
│    - Appointment (Entity)                   │
│    - AppointmentStatus, AppointmentType     │
│    - Request/Response DTOs                  │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│            Event Layer (Kafka)              │
│    - AppointmentEventProducer               │
│    - 5 Event Types                          │
└─────────────────────────────────────────────┘
```

### Event-Driven Architecture

```
┌──────────────┐      Events      ┌──────────────┐
│ Appointment  │ ════════════════► │    Kafka     │
│   Service    │                   │    Broker    │
└──────────────┘                   └──────┬───────┘
                                          │
                     ┌────────────────────┼────────────────────┐
                     │                    │                    │
                     ▼                    ▼                    ▼
              ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
              │Notification │     │  Analytics  │     │   Billing   │
              │   Service   │     │   Service   │     │   Service   │
              └─────────────┘     └─────────────┘     └─────────────┘
```

---

## 🛠️ Technology Stack

### Core Framework
- **Quarkus 3.6.4** - Supersonic Subatomic Java Framework
- **Java 17** - LTS version

### Persistence
- **Hibernate ORM with Panache** - Simplified JPA
- **PostgreSQL** - Production database
- **H2** - In-memory database for testing

### Messaging
- **Apache Kafka** - Event streaming platform
- **SmallRye Reactive Messaging** - Reactive Kafka client

### API & Documentation
- **RESTEasy Reactive** - JAX-RS implementation
- **Jackson** - JSON serialization
- **SmallRye OpenAPI** - OpenAPI 3.0 spec generation
- **Swagger UI** - Interactive API documentation

### Observability
- **SmallRye Health** - Health checks
- **Micrometer + Prometheus** - Metrics

### Testing
- **JUnit 5** - Testing framework
- **RestAssured** - REST API testing
- **AssertJ** - Fluent assertions
- **Testcontainers** - Container-based testing (optional)

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** - [Download](https://adoptium.net/)
- **Maven 3.8+** - [Download](https://maven.apache.org/)
- **Docker** (optional) - [Download](https://www.docker.com/)
- **PostgreSQL** (optional for local dev) - [Download](https://www.postgresql.org/)

### Quick Start

#### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/appointment-service.git
cd appointment-service
```

#### 2. Run with Dev Services (Easiest)

Quarkus Dev Services automatically starts PostgreSQL and Kafka in Docker:

```bash
mvn quarkus:dev
```

The service will be available at: `http://localhost:8083`

#### 3. Access Swagger UI

Open your browser: `http://localhost:8083/swagger-ui`

#### 4. Run Tests

```bash
mvn test
```

Expected output:
```
Tests run: 64, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 📚 API Documentation

### Base URL
```
http://localhost:8083/api/appointments
```

### Endpoints Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/appointments` | Create new appointment |
| GET | `/api/appointments/{id}` | Get appointment by ID |
| PUT | `/api/appointments/{id}` | Reschedule appointment |
| DELETE | `/api/appointments/{id}` | Cancel appointment |
| GET | `/api/appointments` | List all appointments |
| GET | `/api/appointments/upcoming` | Get upcoming appointments |
| GET | `/api/appointments/patient/{id}` | Get patient's appointments |
| GET | `/api/appointments/doctor/{id}` | Get doctor's appointments |
| POST | `/api/appointments/{id}/confirm` | Confirm appointment |
| POST | `/api/appointments/{id}/check-in` | Check-in patient |
| POST | `/api/appointments/{id}/complete` | Mark as completed |
| GET | `/api/appointments/available-slots` | Find available slots |
| POST | `/api/appointments/waiting-list` | Join waiting list |
| GET | `/api/appointments/statistics` | Get statistics |

### Example: Create Appointment

**Request:**
```bash
curl -X POST http://localhost:8083/api/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 100,
    "doctorId": 200,
    "startTime": "2025-12-10T10:00:00",
    "endTime": "2025-12-10T11:00:00",
    "type": "CONSULTATION",
    "reason": "Annual checkup"
  }'
```

**Response:**
```json
{
  "id": 1,
  "patientId": 100,
  "doctorId": 200,
  "startTime": "2025-12-10T10:00:00",
  "endTime": "2025-12-10T11:00:00",
  "type": "CONSULTATION",
  "status": "SCHEDULED",
  "reason": "Annual checkup",
  "confirmationSent": false
}
```

For detailed API documentation, see [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

---

## 🧪 Testing

### Test Coverage

```
Stage 1: Domain Layer     - 12 tests ✅
Stage 2: Service Layer    - 20 tests ✅
Stage 3: REST API Layer   - 22 tests ✅
Stage 4: Event System     - 10 tests ✅
─────────────────────────────────────
Total:                    - 64 tests ✅
Coverage:                 - 100%
```

### Run Specific Test Suites

```bash
# Domain tests
mvn test -Dtest=AppointmentTest

# Service tests
mvn test -Dtest=AppointmentServiceTest

# REST API tests
mvn test -Dtest=AppointmentResourceTest

# Event tests
mvn test -Dtest=AppointmentEventTest

# All tests
mvn clean test
```

### Test Configuration

Tests use:
- **H2 in-memory database** - No PostgreSQL needed
- **In-memory Kafka** - No Kafka broker needed
- **Automatic test isolation** - Clean state for each test

---

## 📡 Event System

### Event Types

The service emits 5 types of events to Kafka:

| Event | Trigger | Topic |
|-------|---------|-------|
| `AppointmentCreatedEvent` | New appointment created | appointment-events |
| `AppointmentConfirmedEvent` | Appointment confirmed | appointment-events |
| `AppointmentCancelledEvent` | Appointment cancelled | appointment-events |
| `AppointmentRescheduledEvent` | Time changed | appointment-events |
| `AppointmentCompletedEvent` | Appointment completed | appointment-events |

### Event Structure

All events extend `AppointmentEvent` and include:

```json
{
  "eventId": "uuid-here",
  "eventType": "APPOINTMENT_CREATED",
  "timestamp": "2025-12-06T10:00:00",
  "appointmentId": 1,
  "patientId": 100,
  "doctorId": 200,
  // Event-specific fields...
}
```

### Consuming Events

Other services can consume these events:
- **Notification Service** - Send email/SMS confirmations
- **Analytics Service** - Track metrics and patterns
- **Billing Service** - Generate invoices

---

## 🐳 Deployment

### Docker Compose (Recommended for Local Development)

```bash
# Start all services (app + PostgreSQL + Kafka)
docker-compose up

# Stop all services
docker-compose down
```

### Docker (Standalone)

```bash
# Build image
docker build -t appointment-service .

# Run container
docker run -p 8083:8083 appointment-service
```

### Kubernetes

```bash
# Apply manifests
kubectl apply -f k8s/

# Check status
kubectl get pods
kubectl get services
```

For detailed deployment instructions, see [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)

---

## 📂 Project Structure

```
appointment-service/
├── src/
│   ├── main/
│   │   ├── java/cz/muni/fi/healthcare/appointment/
│   │   │   ├── domain/          # Entities, DTOs, Enums
│   │   │   ├── service/         # Business logic
│   │   │   ├── resource/        # REST endpoints
│   │   │   └── event/           # Kafka events
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/                # Test classes
│       └── resources/
│           └── application.properties
├── docs/                        # Additional documentation
├── k8s/                         # Kubernetes manifests
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 🎓 Academic Context

This project was developed as part of the **Software System Development** course at **Masaryk University**. It demonstrates:

- ✅ Modern software architecture patterns
- ✅ Microservices design principles
- ✅ Event-driven system integration
- ✅ Test-driven development (TDD)
- ✅ DevOps practices
- ✅ Production-ready code quality



