# CalendarService

**Status:** `In Development`

**Tier / Criticality Level:** `Tier 1`

---

## Overview

The Calendar Service is responsible for managing calendar events and integration with external calendars.

### Key Responsibilities

- Handle full lifecycle of events (schedule, reschedule, cancel, RSVP, availability)
- Provide a well-documented API to trigger handling of the use-cases.
- Integrate with Outlook or Google Calendar services.
- Implement logging and monitoring to improve maintenance.

---

## Architecture

### High-Level Architecture Diagram

```mermaid
flowchart LR
    Client --> APIGateway
    APIGateway --> CalendarService
    AppointmentService --> CalendarService
    CalendarService --> DB[(Database)]
    CalendarService --> ExternalCalendarAPI
```

### Tech Stack

| Category      | Choice       |
| ------------- |--------------|
| Language      | Java         |
| Framework     | Quark        |
| Data Storage  | PostgreSQL   |
| Messaging     | Kafka        |
| Deployment    | Kubernetes   |
| Observability | Prometheus   |

---

## 🔌 APIs & Contracts

### Public Endpoints (External API)

Document only externally consumed interfaces.

| Method | Endpoint                 | Description                           | Scope                   | Rate Limit |
|--------|--------------------------|---------------------------------------|-------------------------| ---------- |
| GET    | `/calendar/availability` | Validate availability of participants | READ_AVAILABILITY       | 1000/min   |
| POST   | `/calendar/schedule`     | Schedule a calendar event             | SCHEDULE_CALENDAR_EVENT | 1000/min   |
| PATCH  | `/calendar/reschedule`   | Re-schedule a calendar event          | SCHEDULE_CALENDAR_EVENT | 1000/min   |
| POST   | `/calendar/cancel`       | Cancel a calendar event               | CANCEL_CALENDAR_EVENT   | 1000/min   |


### Internal Endpoints (Service-to-Service)

> For internal usage only

empty

---

## 📊 Data Model

### Database Schema

```mermaid
erDiagram
    calendar_event {
        uuid7 calendar_id
        varchar title
        text description
        timestamp starting_at
        integer duration
        text external_calendar_event_id
        timestamp created_at
        bool is_deleted
    }
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
