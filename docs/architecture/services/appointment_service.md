# AppointmentService

**Status:** `In Development`

**Tier / Criticality Level:** `Tier 1`

---

## Overview

The Appointment Service is responsible for (re-)scheduling and appointment cancellation between doctor and patient.

### Key Responsibilities

- Properly implement business logic of the use-cases (schedule, reschedule, and cancel appointment).
- Provide a well-documented API to trigger handling of the use-cases.
- Integrate with other services.
- Implement logging and monitoring to improve maintenance.

---

## Architecture

### High-Level Architecture Diagram

```mermaid
flowchart LR
    Client --> APIGateway
    APIGateway --> AppointmentService
    AppointmentService --> IdentityService
    AppointmentService --> CalendarService
    AppointmentService --> DB[(Database)]
```

### Tech Stack

| Category      | Choice                |
| ------------- |-----------------------|
| Language      | Java                  |
| Framework     | Quark                 |
| Data Storage  | PostgreSQL |
| Messaging     | Kafka                 |
| Deployment    | Kubernetes            |
| Observability | Prometheus            |

---

## 🔌 APIs & Contracts

### Public Endpoints (External API)

Document only externally consumed interfaces.

| Method | Endpoint                              | Description                                | Scope                    | Rate Limit |
|--------|---------------------------------------|--------------------------------------------|--------------------------| ---------- |
| POST   | `/appointment/schedule`               | Schedule an appoinment                     | SCHEDULE_APPOINTMENT     | 1000/min   |
| PATCH  | `/appointment/reschedule`             | Re-schedule an appoinment                  | SCHEDULE_APPOINTMENT     | 1000/min   |
| POST   | `/appointment/cancel`                 | Cancel an appoinment                       | CANCEL_APPOINTMENT       | 1000/min   |
| GET    | `/appointment/history?participantId=` | Get appointment history of the participant | READ_PARTICIPANT_HISTORY | 1000/min   |


### Internal Endpoints (Service-to-Service)

> For internal usage only

empty

---

## 📊 Data Model

### Database Schema

```mermaid
erDiagram
    appointment {
        uuid7 appointment_id
        uuid7 calendar_event_id
        varchar title
        text description
        timestamp created_at
        bool is_deleted
    }
    participant {
        uuid7 participant_id
        uuid7 identity_user_id
        timestamp created_at
        bool is_deleted
    }
    appointment_participant {
        uuid7 participant_id
        uuid7 appointment_id
    }

    appointment_participant ||--o{ participant: participant_id
    appointment_participant ||--o{ appointment: appointment_id
```

---

## 🔁 Message Contracts (If Event-Driven)
> TBD

| Topic / Queue | Producer | Consumer | Schema Link |
|---------------|----------|----------| ----------- |
| ...           | ...      | ...      | `<Schema>`  |

---

### Deployment Process
> TBD

---

## 📈 Observability
> TBD

### Metrics
> TBD

* Key business and system metrics (SLIs)

### Dashboards
> TBD

* Links to Grafana/Datadog dashboards

### Alerts
> TBD

| Alert    | Condition     | Severity | Runbook          |
| -------- | ------------- | -------- | ---------------- |
| High 5xx | >5% for 5 min | High     | `<Runbook Link>` |

---

## 🛡️ Security & Compliance
> TBD

* Authentication & Authorization model
* Data sensitivity classification
* Compliance requirements (e.g., GDPR, PCI, HIPAA)
* Secrets management approach

---

## ⚙️ Scaling & Performance
> TBD

* Expected QPS / Throughput
* Latency budget (SLOs)
* Known scaling strategies (sharding, caching, autoscaling rules)

---

## 🔧 Local Development
> TBD

### Prerequisites

* Tools, language versions, env vars

### Setup Instructions
> TBD

```bash
# Example
make setup
npm install
npm run start-local
```

### Testing
> TBD

* How to run unit, integration, e2e tests

---

## 🧪 Quality & Testing Strategy
> TBD

* Test types and coverage expectations
* Test data management
* Contract testing (e.g., Pact)
