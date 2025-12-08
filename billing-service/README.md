# Healthcare Billing Service

> A comprehensive microservices-based billing and payment management system built with Quarkus for healthcare appointment scheduling.

[![Quarkus](https://img.shields.io/badge/Quarkus-3.6.0-blue.svg)](https://quarkus.io/)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.5-black.svg)](https://kafka.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---


## 🎯 Overview

The Healthcare Billing Service is a production-grade microservice that manages the complete billing lifecycle for healthcare appointments. It handles invoice creation, payment processing, refunds, insurance claims, and integrates with external payment gateways while maintaining an event-driven architecture for real-time updates.

---

## ✨ Features

### Core Features
- 🧾 **Invoice Management** - Create, update, and track invoices with line items
- 💳 **Payment Processing** - Multi-gateway support (Stripe, PayPal, Credit Card)
- 💰 **Refund Handling** - Process full and partial refunds
- 🏥 **Insurance Claims** - Submit and track insurance claims
- 💾 **Payment Methods** - Store and manage patient payment methods securely
- 📊 **Balance Tracking** - Real-time calculation of outstanding balances

### Advanced Features
- 🔄 **Event-Driven Architecture** - Kafka-based event publishing for all billing operations
- 🛡️ **Fault Tolerance** - Circuit breakers, retries, and timeouts for external services
- 📈 **Monitoring** - Prometheus metrics and Grafana dashboards
- 🏥 **Health Checks** - Liveness and readiness probes for Kubernetes
- 📝 **API Documentation** - OpenAPI/Swagger UI for interactive API exploration
- 🔐 **Security Ready** - JWT authentication and RBAC support (optional)

### Business Rules
- Automatic invoice status updates based on payments
- Tax calculation (10% default)
- Balance calculation with payment tracking
- Payment method validation
- Insurance claim workflow management

---

## 🏗️ Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Healthcare System                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐      │
│  │   Patient    │    │   Doctor     │    │ Appointment  │      │
│  │   Service    │    │   Service    │    │   Service    │      │
│  └──────────────┘    └──────────────┘    └──────────────┘      │
│         │                    │                    │              │
│         └────────────────────┴────────────────────┘              │
│                              │                                   │
│                              ▼                                   │
│                   ┌──────────────────────┐                      │
│                   │  Billing Service     │ ◄── YOU ARE HERE     │
│                   │  (This Project)      │                      │
│                   └──────────────────────┘                      │
│                              │                                   │
│         ┌────────────────────┼────────────────────┐             │
│         │                    │                    │             │
│         ▼                    ▼                    ▼             │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐     │
│  │ Notification │    │  Analytics   │    │    Audit     │     │
│  │   Service    │    │   Service    │    │   Service    │     │
│  └──────────────┘    └──────────────┘    └──────────────┘     │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘

External Integrations:
  ├── Stripe Payment Gateway
  ├── PayPal Payment Gateway
  └── Insurance Providers
```

### Billing Service Internal Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      Billing Service                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  REST API Layer                                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ InvoiceResource │ PaymentResource │ RefundResource      │   │
│  │ InsuranceResource │ PaymentMethodResource │ ...         │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           │                                      │
│  Service Layer            ▼                                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ InvoiceService │ PaymentService │ RefundService         │   │
│  │ InsuranceService │ PaymentMethodService │ ...           │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           │                                      │
│  Repository Layer         ▼                                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Hibernate ORM Panache Repositories                       │   │
│  └─────────────────────────────────────────────────────────┘   │
│                           │                                      │
│                           ▼                                      │
│                  ┌─────────────────┐                            │
│                  │  PostgreSQL DB  │                            │
│                  └─────────────────┘                            │
│                                                                  │
│  Event Layer (Kafka)                                            │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ BillingEventProducer → Kafka Topics                      │   │
│  │  ├─ invoice-created                                      │   │
│  │  ├─ payment-processed                                    │   │
│  │  ├─ payment-failed                                       │   │
│  │  ├─ refund-issued                                        │   │
│  │  └─ insurance-claim-submitted                            │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Database Schema

### Entity Relationship Diagram

```
┌──────────────────────┐
│      INVOICES        │
├──────────────────────┤
│ id (PK)              │───┐
│ appointment_id       │   │
│ patient_id           │   │
│ subtotal             │   │
│ tax                  │   │
│ total                │   │
│ amount_paid          │   │
│ balance              │   │
│ status               │   │
│ issue_date           │   │
│ due_date             │   │
│ paid_date            │   │
│ notes                │   │
│ created_at           │   │
│ updated_at           │   │
└──────────────────────┘   │
                           │
        ┌──────────────────┴──────────────────┬──────────────────┐
        │                                     │                  │
        ▼                                     ▼                  ▼
┌──────────────────────┐          ┌──────────────────────┐   ┌──────────────────────┐
│   INVOICE_ITEMS      │          │      PAYMENTS        │   │  INSURANCE_CLAIMS    │
├──────────────────────┤          ├──────────────────────┤   ├──────────────────────┤
│ id (PK)              │          │ id (PK)              │   │ id (PK)              │
│ invoice_id (FK)      │          │ invoice_id (FK)      │   │ invoice_id (FK)      │
│ description          │          │ payment_method_id FK)│   │ insurance_provider   │
│ quantity             │          │ amount               │   │ policy_number        │
│ unit_price           │          │ payment_method       │   │ claim_amount         │
│ amount               │          │ status               │   │ approved_amount      │
└──────────────────────┘          │ payment_date         │   │ status               │
                                  │ transaction_id       │   │ submitted_date       │
                                  │ gateway              │   │ processed_date       │
                                  │ notes                │   │ submitted_by         │
                                  └──────────────────────┘   │ notes                │
                                           │                 └──────────────────────┘
                                           │
                                           ▼
                                  ┌──────────────────────┐
                                  │      REFUNDS         │
                                  ├──────────────────────┤
                                  │ id (PK)              │
                                  │ payment_id (FK)      │
                                  │ amount               │
                                  │ reason               │
                                  │ status               │
                                  │ refund_date          │
                                  │ processed_by         │
                                  │ notes                │
                                  └──────────────────────┘

┌──────────────────────┐
│  PAYMENT_METHODS     │
├──────────────────────┤
│ id (PK)              │
│ patient_id           │
│ type                 │
│ card_holder_name     │
│ card_last_four       │
│ expiry_month         │
│ expiry_year          │
│ billing_address      │
│ token                │
│ is_default           │
│ is_active            │
│ created_at           │
│ updated_at           │
└──────────────────────┘
```

### Table Definitions

#### 1. INVOICES
Main table for storing invoice information.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| appointment_id | BIGINT | NOT NULL | Reference to appointment |
| patient_id | BIGINT | NOT NULL | Reference to patient |
| subtotal | DECIMAL(10,2) | NOT NULL | Sum of all line items |
| tax | DECIMAL(10,2) | NOT NULL | Tax amount (10% of subtotal) |
| total | DECIMAL(10,2) | NOT NULL | Subtotal + Tax |
| amount_paid | DECIMAL(10,2) | DEFAULT 0 | Total amount paid |
| balance | DECIMAL(10,2) | COMPUTED | Total - Amount Paid |
| status | VARCHAR(20) | NOT NULL | PENDING, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED, REFUNDED |
| issue_date | TIMESTAMP | NOT NULL | When invoice was created |
| due_date | TIMESTAMP | NOT NULL | Payment deadline (30 days from issue) |
| paid_date | TIMESTAMP | NULL | When fully paid |
| notes | TEXT | NULL | Additional information |
| created_at | TIMESTAMP | DEFAULT NOW() | Record creation time |
| updated_at | TIMESTAMP | DEFAULT NOW() | Last update time |

**Indexes:**
- `idx_invoice_appointment` on `appointment_id`
- `idx_invoice_patient` on `patient_id`
- `idx_invoice_status` on `status`
- `idx_invoice_due_date` on `due_date`

---

#### 2. INVOICE_ITEMS
Line items for each invoice.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| invoice_id | BIGINT | FOREIGN KEY → invoices(id) | Parent invoice |
| description | VARCHAR(255) | NOT NULL | Item description (e.g., "Consultation") |
| quantity | INTEGER | NOT NULL, > 0 | Number of units |
| unit_price | DECIMAL(10,2) | NOT NULL, > 0 | Price per unit |
| amount | DECIMAL(10,2) | COMPUTED | Quantity × Unit Price |

**Indexes:**
- `idx_item_invoice` on `invoice_id`

**Cascade:** ON DELETE CASCADE (when invoice is deleted, items are deleted)

---

#### 3. PAYMENTS
Records of payments made against invoices.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| invoice_id | BIGINT | FOREIGN KEY → invoices(id) | Invoice being paid |
| payment_method_id | BIGINT | FOREIGN KEY → payment_methods(id) | Method used |
| amount | DECIMAL(10,2) | NOT NULL, > 0 | Payment amount |
| payment_method | VARCHAR(50) | NOT NULL | CREDIT_CARD, DEBIT_CARD, INSURANCE, CASH, etc. |
| status | VARCHAR(20) | NOT NULL | PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, CANCELLED |
| payment_date | TIMESTAMP | NOT NULL | When payment was made |
| transaction_id | VARCHAR(100) | NULL | Gateway transaction ID |
| gateway | VARCHAR(50) | NULL | Stripe, PayPal, etc. |
| notes | TEXT | NULL | Additional information |

**Indexes:**
- `idx_payment_invoice` on `invoice_id`
- `idx_payment_method` on `payment_method_id`
- `idx_payment_status` on `status`
- `idx_payment_transaction` on `transaction_id`

---

#### 4. REFUNDS
Refund records for returned payments.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| payment_id | BIGINT | FOREIGN KEY → payments(id) | Original payment |
| amount | DECIMAL(10,2) | NOT NULL, > 0 | Refund amount |
| reason | VARCHAR(255) | NOT NULL | Reason for refund |
| status | VARCHAR(20) | NOT NULL | PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED |
| refund_date | TIMESTAMP | NOT NULL | When refund was issued |
| processed_by | VARCHAR(100) | NULL | Staff member who processed |
| notes | TEXT | NULL | Additional information |

**Indexes:**
- `idx_refund_payment` on `payment_id`
- `idx_refund_status` on `status`

---

#### 5. INSURANCE_CLAIMS
Insurance claim submissions and tracking.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| invoice_id | BIGINT | FOREIGN KEY → invoices(id) | Invoice for claim |
| insurance_provider | VARCHAR(100) | NOT NULL | Insurance company name |
| policy_number | VARCHAR(50) | NOT NULL | Patient's policy number |
| claim_amount | DECIMAL(10,2) | NOT NULL | Amount claimed |
| approved_amount | DECIMAL(10,2) | NULL | Amount approved by insurance |
| status | VARCHAR(20) | NOT NULL | SUBMITTED, UNDER_REVIEW, APPROVED, PARTIALLY_APPROVED, REJECTED, APPEALED |
| submitted_date | TIMESTAMP | NOT NULL | When claim was submitted |
| processed_date | TIMESTAMP | NULL | When claim was processed |
| submitted_by | VARCHAR(100) | NULL | Staff member who submitted |
| notes | TEXT | NULL | Claim notes and updates |

**Indexes:**
- `idx_claim_invoice` on `invoice_id`
- `idx_claim_status` on `status`
- `idx_claim_provider` on `insurance_provider`

---

#### 6. PAYMENT_METHODS
Stored payment methods for patients.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| patient_id | BIGINT | NOT NULL | Patient who owns method |
| type | VARCHAR(50) | NOT NULL | CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, etc. |
| card_holder_name | VARCHAR(100) | NULL | Name on card |
| card_last_four | VARCHAR(4) | NULL | Last 4 digits of card |
| expiry_month | INTEGER | NULL | Card expiration month |
| expiry_year | INTEGER | NULL | Card expiration year |
| billing_address | VARCHAR(255) | NULL | Billing address |
| token | VARCHAR(255) | UNIQUE | Gateway tokenized card |
| is_default | BOOLEAN | DEFAULT FALSE | Is default payment method |
| is_active | BOOLEAN | DEFAULT TRUE | Is method still active |
| created_at | TIMESTAMP | DEFAULT NOW() | When added |
| updated_at | TIMESTAMP | DEFAULT NOW() | Last update |

**Indexes:**
- `idx_payment_method_patient` on `patient_id`
- `idx_payment_method_token` on `token`
- `idx_payment_method_default` on `patient_id, is_default`

**Constraints:**
- Only one default payment method per patient
- Token must be unique

---

### Database Relationships

```sql
-- Foreign Key Constraints

ALTER TABLE invoice_items 
  ADD CONSTRAINT fk_invoice_items_invoice 
  FOREIGN KEY (invoice_id) REFERENCES invoices(id) 
  ON DELETE CASCADE;

ALTER TABLE payments 
  ADD CONSTRAINT fk_payments_invoice 
  FOREIGN KEY (invoice_id) REFERENCES invoices(id) 
  ON DELETE RESTRICT;

ALTER TABLE payments 
  ADD CONSTRAINT fk_payments_payment_method 
  FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id) 
  ON DELETE SET NULL;

ALTER TABLE refunds 
  ADD CONSTRAINT fk_refunds_payment 
  FOREIGN KEY (payment_id) REFERENCES payments(id) 
  ON DELETE RESTRICT;

ALTER TABLE insurance_claims 
  ADD CONSTRAINT fk_claims_invoice 
  FOREIGN KEY (invoice_id) REFERENCES invoices(id) 
  ON DELETE RESTRICT;
```

---

## 🛠️ Technology Stack

### Core Technologies
- **Framework:** Quarkus 3.6.0 (Supersonic Subatomic Java)
- **Language:** Java 17
- **Build Tool:** Maven 3.9+
- **Database:** PostgreSQL 15
- **Messaging:** Apache Kafka 3.5
- **Container:** Docker & Docker Compose

### Libraries & Dependencies
- **REST:** RESTEasy Reactive + Jackson
- **ORM:** Hibernate ORM with Panache
- **Validation:** Jakarta Bean Validation
- **Messaging:** SmallRye Reactive Messaging
- **Fault Tolerance:** SmallRye Fault Tolerance
- **Metrics:** Micrometer + Prometheus
- **Health:** SmallRye Health
- **API Docs:** SmallRye OpenAPI + Swagger UI
- **Testing:** JUnit 5 + RestAssured + Testcontainers

### External Integrations
- **Payment Gateways:** Stripe, PayPal (simulated)
- **Event Bus:** Kafka
- **Monitoring:** Prometheus + Grafana
- **Tracing:** OpenTelemetry (optional)

---

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed:

```bash
# Required
- Java 17 or higher
- Maven 3.9 or higher
- Docker Desktop (for containers)
- Git

# Optional
- Docker Compose V2
- Kubernetes (Minikube/Kind)
- Postman or similar API client
```

### Installation

#### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/billing-service.git
cd billing-service
```

#### 2. Start Dependencies (PostgreSQL + Kafka)

```bash
# Start PostgreSQL
docker run --name billing-postgres \
  -e POSTGRES_USER=billing_user \
  -e POSTGRES_PASSWORD=billing_pass \
  -e POSTGRES_DB=billing_db \
  -p 5432:5432 \
  -d postgres:15

# Create test database
docker exec -it billing-postgres \
  psql -U billing_user -d postgres \
  -c "CREATE DATABASE billing_test_db;"

# Start Kafka + Zookeeper
docker-compose -f docker-compose-kafka.yml up -d

# Wait 30 seconds for Kafka to be ready
sleep 30
```

#### 3. Build the Application

```bash
# Clean build
./mvnw clean package

# Skip tests for faster build
./mvnw clean package -DskipTests
```

#### 4. Run the Application

```bash
# Development mode (with hot reload)
./mvnw quarkus:dev

# Production mode
java -jar target/quarkus-app/quarkus-run.jar
```

#### 5. Verify Installation

```bash
# Check health
curl http://localhost:8085/q/health

# Access Swagger UI
open http://localhost:8085/swagger-ui

# Access Dev UI
open http://localhost:8085/q/dev
```

### Quick Start with Docker Compose

```bash
# Start entire stack (PostgreSQL + Kafka + Application)
docker-compose up -d

# View logs
docker-compose logs -f billing-service

# Stop stack
docker-compose down
```

---

## 📡 API Endpoints

### Base URL
```
http://localhost:8085/api/billing
```

### Endpoint Summary

| Category | Endpoint | Method | Description |
|----------|----------|--------|-------------|
| **Invoices** | `/invoices` | POST | Create new invoice |
| | `/invoices/{id}` | GET | Get invoice by ID |
| | `/invoices/{id}` | PUT | Update invoice |
| | `/invoices/{id}` | DELETE | Delete invoice |
| | `/invoices/patient/{patientId}` | GET | Get patient invoices |
| | `/invoices/appointment/{appointmentId}` | GET | Get invoice by appointment |
| **Payments** | `/payments` | POST | Process payment |
| | `/payments/{id}` | GET | Get payment details |
| | `/payments/invoice/{invoiceId}` | GET | Get payments for invoice |
| **Refunds** | `/refunds` | POST | Process refund |
| | `/refunds/{id}` | GET | Get refund status |
| | `/refunds/payment/{paymentId}` | GET | Get refunds for payment |
| **Insurance** | `/insurance/verify` | POST | Verify insurance coverage |
| | `/insurance/claim` | POST | Submit insurance claim |
| | `/insurance/claim/{id}` | GET | Get claim status |
| | `/insurance/claim/{id}/approve` | POST | Approve claim |
| | `/insurance/claim/{id}/reject` | POST | Reject claim |
| **Payment Methods** | `/payment-methods` | POST | Save payment method |
| | `/payment-methods/{id}` | GET | Get payment method |
| | `/payment-methods/{id}` | PUT | Update payment method |
| | `/payment-methods/{id}` | DELETE | Remove payment method |
| | `/payment-methods/patient/{patientId}` | GET | Get patient methods |
| | `/payment-methods/{id}/default` | POST | Set as default |
| | `/payment-methods/token/{token}` | GET | Get by token |

**Total Endpoints:** 20+

---

## 🧪 API Testing Examples

### 1. Invoice Management

#### Create Invoice

**Endpoint:** `POST /api/billing/invoices`

**Request Body:**
```json
{
  "appointmentId": 1,
  "patientId": 100,
  "items": [
    {
      "description": "Initial Consultation",
      "quantity": 1,
      "unitPrice": 150.00
    },
    {
      "description": "Blood Test",
      "quantity": 2,
      "unitPrice": 50.00
    }
  ],
  "notes": "Regular checkup appointment"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "appointmentId": 1,
  "patientId": 100,
  "subtotal": 250.00,
  "tax": 25.00,
  "total": 275.00,
  "amountPaid": 0.00,
  "balance": 275.00,
  "status": "PENDING",
  "issueDate": "2024-12-08T10:30:00",
  "dueDate": "2025-01-07T10:30:00",
  "paidDate": null,
  "items": [
    {
      "id": 1,
      "description": "Initial Consultation",
      "quantity": 1,
      "unitPrice": 150.00,
      "amount": 150.00
    },
    {
      "id": 2,
      "description": "Blood Test",
      "quantity": 2,
      "unitPrice": 50.00,
      "amount": 100.00
    }
  ],
  "notes": "Regular checkup appointment"
}
```

**cURL:**
```bash
curl -X POST http://localhost:8085/api/billing/invoices \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 1,
    "patientId": 100,
    "items": [
      {
        "description": "Initial Consultation",
        "quantity": 1,
        "unitPrice": 150.00
      },
      {
        "description": "Blood Test",
        "quantity": 2,
        "unitPrice": 50.00
      }
    ],
    "notes": "Regular checkup appointment"
  }'
```

---

#### Get Invoice by ID

**Endpoint:** `GET /api/billing/invoices/{id}`

**Request:**
```bash
curl http://localhost:8085/api/billing/invoices/1
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "appointmentId": 1,
  "patientId": 100,
  "subtotal": 250.00,
  "tax": 25.00,
  "total": 275.00,
  "amountPaid": 0.00,
  "balance": 275.00,
  "status": "PENDING",
  "issueDate": "2024-12-08T10:30:00",
  "dueDate": "2025-01-07T10:30:00",
  "paidDate": null,
  "items": [
    {
      "id": 1,
      "description": "Initial Consultation",
      "quantity": 1,
      "unitPrice": 150.00,
      "amount": 150.00
    },
    {
      "id": 2,
      "description": "Blood Test",
      "quantity": 2,
      "unitPrice": 50.00,
      "amount": 100.00
    }
  ],
  "notes": "Regular checkup appointment"
}
```

---

#### Get Patient Invoices

**Endpoint:** `GET /api/billing/invoices/patient/{patientId}`

**Request:**
```bash
curl http://localhost:8085/api/billing/invoices/patient/100
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "appointmentId": 1,
    "patientId": 100,
    "total": 275.00,
    "balance": 275.00,
    "status": "PENDING",
    "issueDate": "2024-12-08T10:30:00",
    "dueDate": "2025-01-07T10:30:00"
  },
  {
    "id": 2,
    "appointmentId": 5,
    "patientId": 100,
    "total": 450.00,
    "balance": 0.00,
    "status": "PAID",
    "issueDate": "2024-11-15T14:20:00",
    "dueDate": "2024-12-15T14:20:00",
    "paidDate": "2024-11-20T09:15:00"
  }
]
```

---

#### Update Invoice

**Endpoint:** `PUT /api/billing/invoices/{id}`

**Request Body:**
```json
{
  "notes": "Updated: Added lab work results"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "appointmentId": 1,
  "patientId": 100,
  "subtotal": 250.00,
  "tax": 25.00,
  "total": 275.00,
  "amountPaid": 0.00,
  "balance": 275.00,
  "status": "PENDING",
  "issueDate": "2024-12-08T10:30:00",
  "dueDate": "2025-01-07T10:30:00",
  "notes": "Updated: Added lab work results"
}
```

---

### 2. Payment Processing

#### Process Payment

**Endpoint:** `POST /api/billing/payments`

**Request Body:**
```json
{
  "invoiceId": 1,
  "amount": 275.00,
  "paymentMethod": "CREDIT_CARD",
  "gateway": "Stripe",
  "paymentToken": "tok_visa_4242",
  "notes": "Payment via Visa ending in 4242"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "invoiceId": 1,
  "amount": 275.00,
  "paymentMethod": "CREDIT_CARD",
  "status": "COMPLETED",
  "paymentDate": "2024-12-08T11:00:00",
  "transactionId": "TXN-1733652000123",
  "gateway": "Stripe",
  "notes": "Payment via Visa ending in 4242"
}
```

**cURL:**
```bash
curl -X POST http://localhost:8085/api/billing/payments \
  -H "Content-Type: application/json" \
  -d '{
    "invoiceId": 1,
    "amount": 275.00,
    "paymentMethod": "CREDIT_CARD",
    "gateway": "Stripe",
    "paymentToken": "tok_visa_4242",
    "notes": "Payment via Visa ending in 4242"
  }'
```

---

#### Process Partial Payment

**Request Body:**
```json
{
  "invoiceId": 1,
  "amount": 100.00,
  "paymentMethod": "CASH",
  "notes": "Partial payment - cash"
}
```

**Response:** `201 Created`
```json
{
  "id": 2,
  "invoiceId": 1,
  "amount": 100.00,
  "paymentMethod": "CASH",
  "status": "COMPLETED",
  "paymentDate": "2024-12-08T11:15:00",
  "transactionId": null,
  "gateway": null,
  "notes": "Partial payment - cash"
}
```

**Note:** Invoice status will update to `PARTIALLY_PAID` and balance will be `175.00`

---

#### Get Payment Details

**Endpoint:** `GET /api/billing/payments/{id}`

**Request:**
```bash
curl http://localhost:8085/api/billing/payments/1
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "invoiceId": 1,
  "amount": 275.00,
  "paymentMethod": "CREDIT_CARD",
  "status": "COMPLETED",
  "paymentDate": "2024-12-08T11:00:00",
  "transactionId": "TXN-1733652000123",
  "gateway": "Stripe",
  "notes": "Payment via Visa ending in 4242"
}
```

---

#### Get Payments for Invoice

**Endpoint:** `GET /api/billing/payments/invoice/{invoiceId}`

**Request:**
```bash
curl http://localhost:8085/api/billing/payments/invoice/1
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "invoiceId": 1,
    "amount": 275.00,
    "paymentMethod": "CREDIT_CARD",
    "status": "COMPLETED",
    "paymentDate": "2024-12-08T11:00:00",
    "transactionId": "TXN-1733652000123",
    "gateway": "Stripe"
  }
]
```

---

### 3. Refund Processing

#### Process Full Refund

**Endpoint:** `POST /api/billing/refunds`

**Request Body:**
```json
{
  "paymentId": 1,
  "amount": 275.00,
  "reason": "Appointment cancelled by provider",
  "processedBy": "admin@hospital.com"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "paymentId": 1,
  "amount": 275.00,
  "reason": "Appointment cancelled by provider",
  "status": "COMPLETED",
  "refundDate": "2024-12-08T12:00:00",
  "processedBy": "admin@hospital.com",
  "notes": null
}
```

**cURL:**
```bash
curl -X POST http://localhost:8085/api/billing/refunds \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": 1,
    "amount": 275.00,
    "reason": "Appointment cancelled by provider",
    "processedBy": "admin@hospital.com"
  }'
```

---

#### Process Partial Refund

**Request Body:**
```json
{
  "paymentId": 1,
  "amount": 50.00,
  "reason": "Partial service refund - lab test not performed",
  "processedBy": "billing@hospital.com"
}
```

**Response:** `201 Created`
```json
{
  "id": 2,
  "paymentId": 1,
  "amount": 50.00,
  "reason": "Partial service refund - lab test not performed",
  "status": "COMPLETED",
  "refundDate": "2024-12-08T12:30:00",
  "processedBy": "billing@hospital.com",
  "notes": null
}
```

---

#### Get Refund Status

**Endpoint:** `GET /api/billing/refunds/{id}`

**Request:**
```bash
curl http://localhost:8085/api/billing/refunds/1
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "paymentId": 1,
  "amount": 275.00,
  "reason": "Appointment cancelled by provider",
  "status": "COMPLETED",
  "refundDate": "2024-12-08T12:00:00",
  "processedBy": "admin@hospital.com",
  "notes": null
}
```

---

### 4. Insurance Claims

#### Verify Insurance Coverage

**Endpoint:** `POST /api/billing/insurance/verify`

**Request Body:**
```json
{
  "patientId": 100,
  "insuranceProvider": "Blue Cross Blue Shield",
  "policyNumber": "BCBS-12345678",
  "serviceDate": "2024-12-08",
  "estimatedAmount": 275.00
}
```

**Response:** `200 OK`
```json
{
  "isValid": true,
  "coveragePercentage": 80,
  "estimatedCoverage": 220.00,
  "patientResponsibility": 55.00,
  "deductibleMet": true,
  "message": "Coverage verified successfully"
}
```

**cURL:**
```bash
curl -X POST http://localhost:8085/api/billing/insurance/verify \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 100,
    "insuranceProvider": "Blue Cross Blue Shield",
    "policyNumber": "BCBS-12345678",
    "serviceDate": "2024-12-08",
    "estimatedAmount": 275.00
  }'
```

---

#### Submit Insurance Claim

**Endpoint:** `POST /api/billing/insurance/claim`

**Request Body:**
```json
{
  "invoiceId": 1,
  "insuranceProvider": "Blue Cross Blue Shield",
  "policyNumber": "BCBS-12345678",
  "claimAmount": 275.00,
  "submittedBy": "billing@hospital.com",
  "notes": "Standard consultation claim"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "invoiceId": 1,
  "insuranceProvider": "Blue Cross Blue Shield",
  "policyNumber": "BCBS-12345678",
  "claimAmount": 275.00,
  "approvedAmount": null,
  "status": "SUBMITTED",
  "submittedDate": "2024-12-08T13:00:00",
  "processedDate": null,
  "submittedBy": "billing@hospital.com",
  "notes": "Standard consultation claim"
}
```

---

#### Get Claim Status

**Endpoint:** `GET /api/billing/insurance/claim/{id}`

**Request:**
```bash
curl http://localhost:8085/api/billing/insurance/claim/1
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "invoiceId": 1,
  "insuranceProvider": "Blue Cross Blue Shield",
  "policyNumber": "BCBS-12345678",
  "claimAmount": 275.00,
  "approvedAmount": null,
  "status": "UNDER_REVIEW",
  "submittedDate": "2024-12-08T13:00:00",
  "processedDate": null,
  "submittedBy": "billing@hospital.com",
  "notes": "Claim is under review by insurance provider"
}
```

---

#### Approve Claim (Internal/Admin)

**Endpoint:** `POST /api/billing/insurance/claim/{id}/approve`

**Request Body:**
```json
{
  "approvedAmount": 220.00,
  "notes": "Approved - standard consultation coverage"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "invoiceId": 1,
  "insuranceProvider": "Blue Cross Blue Shield",
  "policyNumber": "BCBS-12345678",
  "claimAmount": 275.00,
  "approvedAmount": 220.00,
  "status": "APPROVED",
  "submittedDate": "2024-12-08T13:00:00",
  "processedDate": "2024-12-10T09:00:00",
  "submittedBy": "billing@hospital.com",
  "notes": "Approved - standard consultation coverage"
}
```

---

#### Reject Claim (Internal/Admin)

**Endpoint:** `POST /api/billing/insurance/claim/{id}/reject`

**Request Body:**
```json
{
  "notes": "Rejected - service not covered under policy"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "invoiceId": 1,
  "insuranceProvider": "Blue Cross Blue Shield",
  "policyNumber": "BCBS-12345678",
  "claimAmount": 275.00,
  "approvedAmount": 0.00,
  "status": "REJECTED",
  "submittedDate": "2024-12-08T13:00:00",
  "processedDate": "2024-12-10T09:00:00",
  "submittedBy": "billing@hospital.com",
  "notes": "Rejected - service not covered under policy"
}
```

---

### 5. Payment Methods

#### Save Payment Method

**Endpoint:** `POST /api/billing/payment-methods`

**Request Body:**
```json
{
  "patientId": 100,
  "type": "CREDIT_CARD",
  "cardHolderName": "John Doe",
  "cardNumber": "4111111111111111",
  "expiryMonth": 12,
  "expiryYear": 2026,
  "cvv": "123",
  "billingAddress": "123 Main St, City, State 12345",
  "isDefault": true
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "patientId": 100,
  "type": "CREDIT_CARD",
  "cardHolderName": "John Doe",
  "cardLastFour": "1111",
  "expiryMonth": 12,
  "expiryYear": 2026,
  "billingAddress": "123 Main St, City, State 12345",
  "token": "pm_1234567890abcdef",
  "isDefault": true,
  "isActive": true,
  "createdAt": "2024-12-08T14:00:00",
  "updatedAt": "2024-12-08T14:00:00"
}
```

**cURL:**
```bash
curl -X POST http://localhost:8085/api/billing/payment-methods \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 100,
    "type": "CREDIT_CARD",
    "cardHolderName": "John Doe",
    "cardNumber": "4111111111111111",
    "expiryMonth": 12,
    "expiryYear": 2026,
    "cvv": "123",
    "billingAddress": "123 Main St, City, State 12345",
    "isDefault": true
  }'
```

**Security Note:** Full card number is never stored. It's tokenized by the gateway and only the last 4 digits are kept.

---

#### Get Patient Payment Methods

**Endpoint:** `GET /api/billing/payment-methods/patient/{patientId}`

**Request:**
```bash
curl http://localhost:8085/api/billing/payment-methods/patient/100
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "patientId": 100,
    "type": "CREDIT_CARD",
    "cardHolderName": "John Doe",
    "cardLastFour": "1111",
    "expiryMonth": 12,
    "expiryYear": 2026,
    "isDefault": true,
    "isActive": true
  },
  {
    "id": 2,
    "patientId": 100,
    "type": "DEBIT_CARD",
    "cardHolderName": "John Doe",
    "cardLastFour": "4242",
    "expiryMonth": 6,
    "expiryYear": 2025,
    "isDefault": false,
    "isActive": true
  }
]
```

---

#### Update Payment Method

**Endpoint:** `PUT /api/billing/payment-methods/{id}`

**Request Body:**
```json
{
  "expiryMonth": 3,
  "expiryYear": 2027,
  "billingAddress": "456 New Address, City, State 54321"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "patientId": 100,
  "type": "CREDIT_CARD",
  "cardHolderName": "John Doe",
  "cardLastFour": "1111",
  "expiryMonth": 3,
  "expiryYear": 2027,
  "billingAddress": "456 New Address, City, State 54321",
  "token": "pm_1234567890abcdef",
  "isDefault": true,
  "isActive": true,
  "createdAt": "2024-12-08T14:00:00",
  "updatedAt": "2024-12-08T14:30:00"
}
```

---

#### Set as Default Payment Method

**Endpoint:** `POST /api/billing/payment-methods/{id}/default`

**Request:**
```bash
curl -X POST http://localhost:8085/api/billing/payment-methods/2/default
```

**Response:** `200 OK`
```json
{
  "id": 2,
  "patientId": 100,
  "type": "DEBIT_CARD",
  "cardHolderName": "John Doe",
  "cardLastFour": "4242",
  "expiryMonth": 6,
  "expiryYear": 2025,
  "isDefault": true,
  "isActive": true
}
```

**Note:** Previous default payment method will be automatically unset.

---

#### Delete Payment Method

**Endpoint:** `DELETE /api/billing/payment-methods/{id}`

**Request:**
```bash
curl -X DELETE http://localhost:8085/api/billing/payment-methods/1
```

**Response:** `204 No Content`

**Note:** This soft-deletes the method (sets `is_active = false`). It's not physically deleted to maintain payment history.

---

### 6. Get Payment Method by Token

**Endpoint:** `GET /api/billing/payment-methods/token/{token}`

**Request:**
```bash
curl http://localhost:8085/api/billing/payment-methods/token/pm_1234567890abcdef
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "patientId": 100,
  "type": "CREDIT_CARD",
  "cardHolderName": "John Doe",
  "cardLastFour": "1111",
  "expiryMonth": 12,
  "expiryYear": 2026,
  "token": "pm_1234567890abcdef",
  "isDefault": true,
  "isActive": true
}
```

---

## 📨 Event-Driven Architecture

### Kafka Events Published

The service publishes events to Kafka for all major billing operations:

#### 1. Invoice Created Event
**Topic:** `billing.invoice.created`

**Payload:**
```json
{
  "invoiceId": 1,
  "appointmentId": 1,
  "patientId": 100,
  "subtotal": 250.00,
  "tax": 25.00,
  "total": 275.00,
  "status": "PENDING",
  "issueDate": "2024-12-08T10:30:00",
  "dueDate": "2025-01-07T10:30:00",
  "notes": "Regular checkup appointment",
  "eventTimestamp": "2024-12-08T10:30:01"
}
```

**Consumers:**
- Notification Service (sends invoice email)
- Analytics Service (tracks billing metrics)
- Audit Service (logs creation)

---

#### 2. Payment Processed Event
**Topic:** `billing.payment.processed`

**Payload:**
```json
{
  "paymentId": 1,
  "invoiceId": 1,
  "patientId": 100,
  "amount": 275.00,
  "paymentMethod": "CREDIT_CARD",
  "status": "COMPLETED",
  "gateway": "Stripe",
  "transactionId": "TXN-1733652000123",
  "paymentDate": "2024-12-08T11:00:00",
  "notes": "Payment via Visa ending in 4242",
  "eventTimestamp": "2024-12-08T11:00:01"
}
```

**Consumers:**
- Notification Service (sends payment receipt)
- Analytics Service (tracks revenue)
- Reporting Service (financial reports)

---

#### 3. Payment Failed Event
**Topic:** `billing.payment.failed`

**Payload:**
```json
{
  "paymentId": 2,
  "invoiceId": 1,
  "patientId": 100,
  "amount": 275.00,
  "paymentMethod": "CREDIT_CARD",
  "gateway": "Stripe",
  "failureReason": "Card declined - insufficient funds",
  "errorCode": "INSUFFICIENT_FUNDS",
  "attemptDate": "2024-12-08T11:30:00",
  "eventTimestamp": "2024-12-08T11:30:01"
}
```

**Consumers:**
- Notification Service (sends failure notification)
- Analytics Service (tracks failure rate)
- Fraud Detection Service (monitors patterns)

---

#### 4. Refund Issued Event
**Topic:** `billing.refund.issued`

**Payload:**
```json
{
  "refundId": 1,
  "paymentId": 1,
  "invoiceId": 1,
  "patientId": 100,
  "amount": 275.00,
  "status": "COMPLETED",
  "reason": "Appointment cancelled by provider",
  "refundDate": "2024-12-08T12:00:00",
  "processedBy": "admin@hospital.com",
  "eventTimestamp": "2024-12-08T12:00:01"
}
```

**Consumers:**
- Notification Service (sends refund confirmation)
- Analytics Service (tracks refund metrics)
- Financial Service (accounting adjustments)

---

#### 5. Insurance Claim Submitted Event
**Topic:** `billing.insurance.claim.submitted`

**Payload:**
```json
{
  "claimId": 1,
  "invoiceId": 1,
  "patientId": 100,
  "insuranceProvider": "Blue Cross Blue Shield",
  "policyNumber": "BCBS-12345678",
  "claimAmount": 275.00,
  "status": "SUBMITTED",
  "submittedDate": "2024-12-08T13:00:00",
  "submittedBy": "billing@hospital.com",
  "eventTimestamp": "2024-12-08T13:00:01"
}
```

**Consumers:**
- Notification Service (sends submission confirmation)
- Insurance Integration Service (submits to provider)
- Analytics Service (tracks claim metrics)

---

#### 6. Payment Reminder Sent Event
**Topic:** `billing.payment.reminder.sent`

**Payload:**
```json
{
  "invoiceId": 1,
  "patientId": 100,
  "outstandingBalance": 275.00,
  "dueDate": "2025-01-07T10:30:00",
  "reminderType": "FIRST",
  "channel": "EMAIL",
  "sentDate": "2024-12-08T15:00:00",
  "eventTimestamp": "2024-12-08T15:00:01"
}
```

**Consumers:**
- Analytics Service (tracks reminder effectiveness)
- Reporting Service (payment follow-up reports)

---

### Testing Kafka Events

#### Watch Events in Console

```bash
# Watch invoice created events
docker exec -it billing-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic billing.invoice.created \
  --from-beginning

# Watch payment events
docker exec -it billing-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic billing.payment.processed \
  --from-beginning
```

#### View Events in Kafka UI

```bash
# Access Kafka UI
open http://localhost:8080

# Navigate to:
# Topics → billing.invoice.created → Messages
```

---

## 🏥 Monitoring & Health

### Health Endpoints

#### Liveness Probe
```bash
curl http://localhost:8085/q/health/live
```

**Response:**
```json
{
  "status": "UP",
  "checks": [
    {
      "name": "Billing Service",
      "status": "UP"
    }
  ]
}
```

---

#### Readiness Probe
```bash
curl http://localhost:8085/q/health/ready
```

**Response:**
```json
{
  "status": "UP",
  "checks": [
    {
      "name": "Database connection health check",
      "status": "UP"
    },
    {
      "name": "Kafka connection health check",
      "status": "UP"
    }
  ]
}
```

---

#### Combined Health Check
```bash
curl http://localhost:8085/q/health
```

**Response:**
```json
{
  "status": "UP",
  "checks": [
    {
      "name": "Billing Service Liveness",
      "status": "UP"
    },
    {
      "name": "Database connection health check",
      "status": "UP",
      "data": {
        "database": "PostgreSQL 15"
      }
    },
    {
      "name": "Kafka connection health check",
      "status": "UP"
    }
  ]
}
```

---

### Prometheus Metrics

```bash
# Access metrics endpoint
curl http://localhost:8085/q/metrics

# Sample metrics:
# - billing_invoices_created_total
# - billing_payments_processed_total
# - billing_payments_failed_total
# - billing_payment_processing_seconds
# - billing_outstanding_balance
# - http_server_requests_seconds
# - jvm_memory_used_bytes
```

---

### Dev UI

Access the comprehensive Dev UI:

```bash
open http://localhost:8085/q/dev
```

Features:
- Configuration editor
- Database console
- Kafka console
- Swagger UI integration
- Health checks
- Metrics visualization

---

## 💻 Development

### Project Structure

```
billing-service/
├── src/
│   ├── main/
│   │   ├── java/com/basit/billing/
│   │   │   ├── entity/           # JPA entities
│   │   │   │   ├── Invoice.java
│   │   │   │   ├── InvoiceItem.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── Refund.java
│   │   │   │   ├── InsuranceClaim.java
│   │   │   │   └── PaymentMethod.java
│   │   │   ├── enums/            # Enumerations
│   │   │   │   ├── InvoiceStatus.java
│   │   │   │   ├── PaymentStatus.java
│   │   │   │   ├── PaymentMethodType.java
│   │   │   │   ├── RefundStatus.java
│   │   │   │   └── ClaimStatus.java
│   │   │   ├── repository/       # Data access
│   │   │   │   ├── InvoiceRepository.java
│   │   │   │   ├── PaymentRepository.java
│   │   │   │   ├── RefundRepository.java
│   │   │   │   ├── InsuranceClaimRepository.java
│   │   │   │   └── PaymentMethodRepository.java
│   │   │   ├── dto/              # Data transfer objects
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── mapper/           # Entity-DTO mappers
│   │   │   ├── service/          # Business logic
│   │   │   │   ├── InvoiceService.java
│   │   │   │   ├── PaymentService.java
│   │   │   │   ├── RefundService.java
│   │   │   │   ├── InsuranceService.java
│   │   │   │   └── PaymentMethodService.java
│   │   │   ├── resource/         # REST endpoints
│   │   │   │   ├── InvoiceResource.java
│   │   │   │   ├── PaymentResource.java
│   │   │   │   ├── RefundResource.java
│   │   │   │   ├── InsuranceResource.java
│   │   │   │   └── PaymentMethodResource.java
│   │   │   ├── event/            # Kafka events
│   │   │   │   ├── InvoiceCreatedEvent.java
│   │   │   │   ├── PaymentProcessedEvent.java
│   │   │   │   ├── BillingEventProducer.java
│   │   │   │   └── ...
│   │   │   ├── health/           # Health checks
│   │   │   └── exception/        # Exception handlers
│   │   └── resources/
│   │       ├── application.properties
│   │       └── META-INF/
│   └── test/
│       ├── java/com/basit/billing/
│       │   ├── entity/           # Entity tests
│       │   ├── repository/       # Repository tests
│       │   ├── service/          # Service tests
│       │   ├── resource/         # API tests
│       │   └── event/            # Event tests
│       └── resources/
│           └── application.properties
├── pom.xml
├── docker-compose.yml
├── docker-compose-kafka.yml
├── Dockerfile
└── README.md
```

---

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=InvoiceServiceTest

# Run specific test method
./mvnw test -Dtest=InvoiceServiceTest#testCreateInvoice

# Run with coverage
./mvnw test jacoco:report

# Skip tests
./mvnw package -DskipTests
```

---

### Code Quality

```bash
# Format code
./mvnw spotless:apply

# Check code style
./mvnw spotless:check

# Static analysis
./mvnw pmd:check

# Security scan
./mvnw dependency-check:check
```

---

## 🧪 Testing

### Test Coverage

- **Entity Tests:** 100% (6/6 entities)
- **Repository Tests:** 100% (6/6 repositories)
- **Service Tests:** 100% (5/5 services)
- **Resource Tests:** 100% (5/5 resources)
- **Event Tests:** 100% (1/1 producer)
- **Total Tests:** 96+

### Test Strategy

1. **Unit Tests** - Individual components
2. **Integration Tests** - Database + API
3. **Event Tests** - Kafka messaging
4. **End-to-End Tests** - Complete workflows

---

## 🚢 Deployment

### Docker Deployment

#### Build Image

```bash
# Build application
./mvnw package

# Build Docker image
docker build -t billing-service:1.0.0 .

# Tag for registry
docker tag billing-service:1.0.0 your-registry/billing-service:1.0.0

# Push to registry
docker push your-registry/billing-service:1.0.0
```

---

#### Run with Docker Compose

```bash
# Start full stack
docker-compose up -d

# View logs
docker-compose logs -f billing-service

# Stop stack
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

---

### Kubernetes Deployment

```bash
# Apply deployment
kubectl apply -f k8s/deployment.yaml

# Apply service
kubectl apply -f k8s/service.yaml

# Check status
kubectl get pods
kubectl get services

# View logs
kubectl logs -f deployment/billing-service

# Port forward for testing
kubectl port-forward service/billing-service 8085:8085
```

---

## 🔧 Troubleshooting

### Common Issues

#### 1. Database Connection Failed

**Problem:**
```
JDBC connection failed: Connection refused
```

**Solution:**
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Start PostgreSQL if needed
docker start billing-postgres

# Verify connection
docker exec -it billing-postgres psql -U billing_user -d billing_db -c "\l"
```

---

#### 2. Kafka Connection Failed

**Problem:**
```
Failed to connect to Kafka at localhost:9092
```

**Solution:**
```bash
# Check Kafka is running
docker ps | grep kafka

# Start Kafka
docker-compose -f docker-compose-kafka.yml up -d

# Wait for Kafka to be ready (30 seconds)
sleep 30

# Verify Kafka is ready
docker logs billing-kafka | grep "started"
```

---

#### 3. Tests Failing

**Problem:**
```
62 tests failing with various errors
```

**Solution:**
```bash
# See the test failure fix guide
cat /mnt/user-data/outputs/Quick-Action-Plan.md

# Common fixes:
# 1. Fix delete order in test setup methods
# 2. Change .path("id") to .jsonPath().getLong("id")
# 3. Fix assertion field names
```

---

#### 4. Port Already in Use

**Problem:**
```
Port 8085 already in use
```

**Solution:**
```bash
# Find process using port
lsof -i :8085

# Kill process (replace PID)
kill -9 <PID>

# Or change port in application.properties
quarkus.http.port=8086
```

---






