# Healthcare Appointment Scheduling System - Enhanced Edition

## 0) Overview
The Healthcare Appointment Scheduling System is a microservices-based application built with Quarkus, designed to efficiently manage healthcare appointments while ensuring scalability, resiliency, and observability. The system now includes **six independently deployable services** with expanded public API endpoints for comprehensive healthcare management including billing and payments.

---

## 1) Microservices Overview

### 1️⃣ Patient Service
**Responsibilities:**
- Manage patient profiles: registration, personal info, contact details, medical history
- Allow patients to view, update, and delete their information
- Retrieve a patient's appointment history via the Appointment Service
- Manage patient insurance information
- Handle patient preferences and communication settings

**Endpoints:**
- `POST /api/patients/register` — Register new patient
- `GET /api/patients/{id}` — Get patient details
- `PUT /api/patients/{id}` — Update patient profile
- `DELETE /api/patients/{id}` — Deactivate patient account
- `GET /api/patients/{id}/appointments` — View appointment history
- `GET /api/patients/{id}/insurance` — Get insurance information
- `PUT /api/patients/{id}/insurance` — Update insurance details
- `GET /api/patients/{id}/medical-history` — Retrieve medical history
- `POST /api/patients/{id}/medical-history` — Add medical record
- `GET /api/patients/search` — Search patients by name or contact
- `GET /api/patients/{id}/preferences` — Get communication preferences
- `PUT /api/patients/{id}/preferences` — Update preferences

**Technologies:**
- Quarkus (RESTEasy Reactive, Panache ORM)
- PostgreSQL database
- OpenAPI for documentation
- Micrometer Prometheus metrics + Grafana visualization

---

### 2️⃣ Doctor Service
**Responsibilities:**
- Manage doctor profiles: name, specialization, experience
- Handle availability slots (working hours, vacations)
- Provide data for Appointment Service to verify slot availability
- Track doctor ratings and reviews
- Manage doctor schedules and time-off requests

**Endpoints:**
- `POST /api/doctors/register` — Register doctor
- `GET /api/doctors/{id}` — Retrieve doctor profile
- `PUT /api/doctors/{id}` — Update doctor details
- `DELETE /api/doctors/{id}` — Deactivate doctor profile
- `GET /api/doctors/{id}/availability` — Get available slots
- `PUT /api/doctors/{id}/availability` — Update doctor's availability
- `GET /api/doctors/search` — Search doctors by specialization or name
- `GET /api/doctors/{id}/reviews` — Get doctor reviews and ratings
- `POST /api/doctors/{id}/reviews` — Add a review for doctor
- `GET /api/doctors/{id}/schedule` — View weekly schedule
- `POST /api/doctors/{id}/time-off` — Request time off
- `GET /api/doctors/{id}/appointments/history` — Doctor's appointment history
- `GET /api/doctors/specializations` — List all available specializations

**Technologies:**
- Quarkus RESTEasy Reactive + Hibernate ORM Panache
- PostgreSQL for persistence
- Kafka producer for emitting `DoctorAvailabilityUpdated` events
- Health check via MicroProfile Health

---

### 3️⃣ Appointment Service
**Responsibilities:**
- Schedule, reschedule, and cancel appointments between patients and doctors
- Verify doctor availability using data from the Doctor Service
- Persist appointment information and emit domain events for notifications and analytics
- Integrate with external calendar APIs (Google Calendar/Outlook)
- Handle appointment reminders and follow-ups
- Manage waiting lists for fully booked slots

**Endpoints:**
- `POST /api/appointments` — Schedule new appointment
- `GET /api/appointments/{id}` — Retrieve appointment details
- `PUT /api/appointments/{id}` — Reschedule appointment
- `DELETE /api/appointments/{id}` — Cancel appointment
- `GET /api/appointments` — List all appointments (with filters)
- `GET /api/appointments/upcoming` — Get upcoming appointments
- `GET /api/appointments/patient/{patientId}` — Get patient's appointments
- `GET /api/appointments/doctor/{doctorId}` — Get doctor's appointments
- `POST /api/appointments/{id}/confirm` — Confirm appointment
- `POST /api/appointments/{id}/check-in` — Patient check-in
- `POST /api/appointments/{id}/complete` — Mark appointment as completed
- `GET /api/appointments/available-slots` — Find available appointment slots
- `POST /api/appointments/waiting-list` — Join waiting list
- `GET /api/appointments/statistics` — Get appointment statistics

**Technologies:**
- Quarkus RESTEasy Reactive + Hibernate ORM Panache
- PostgreSQL for persistence
- Kafka (SmallRye Reactive Messaging)
- OpenAPI + Swagger UI
- Circuit breaker & retries for external calendar APIs (Fault Tolerance)

---

### 4️⃣ Notification Service
**Responsibilities:**
- Listen to Kafka topics and send appointment confirmations, reminders, and cancellation messages
- Support multiple channels (email, SMS, push notification stubs)
- Schedule daily reminders using Quarkus Scheduler
- Track notification delivery status and retry failures
- Manage notification templates

**Endpoints:**
- `GET /api/notifications/recent` — Retrieve recent notifications
- `GET /api/notifications/{id}` — Get notification details
- `GET /api/notifications/patient/{patientId}` — Get patient's notifications
- `POST /api/notifications/send` — Manually send notification
- `GET /api/notifications/templates` — List notification templates
- `POST /api/notifications/templates` — Create notification template
- `PUT /api/notifications/templates/{id}` — Update template
- `GET /api/notifications/delivery-status` — Check delivery statistics
- `POST /api/notifications/{id}/resend` — Resend failed notification

**Technologies:**
- Quarkus Reactive Messaging (Kafka consumer)
- Quarkus Scheduler for reminders
- JavaMail or Twilio integration for email/SMS
- Micrometer Prometheus for notification metrics
- Grafana dashboard to track delivery success/failure rates

---

### 5️⃣ Analytics Service
**Responsibilities:**
- Collect and process operational and business data from all services
- Aggregate metrics such as number of appointments, cancellations, and doctor utilization
- Provide endpoints and dashboards for analytics visualization
- Consume Kafka topics (Appointment events) and store data for Prometheus/Grafana
- Generate reports on service performance and business metrics

**Endpoints:**
- `GET /api/analytics/appointments/daily` — Daily appointment stats
- `GET /api/analytics/appointments/weekly` — Weekly appointment trends
- `GET /api/analytics/appointments/monthly` — Monthly appointment reports
- `GET /api/analytics/doctor/{id}` — Doctor utilization metrics
- `GET /api/analytics/doctors/performance` — All doctors performance comparison
- `GET /api/analytics/patients/demographics` — Patient demographics
- `GET /api/analytics/system/overview` — Overall service health summary
- `GET /api/analytics/cancellations` — Cancellation rate analysis
- `GET /api/analytics/revenue` — Revenue analytics
- `GET /api/analytics/peak-hours` — Peak appointment hours analysis
- `POST /api/analytics/reports/generate` — Generate custom report
- `GET /api/analytics/reports/{reportId}` — Retrieve generated report

**Technologies:**
- Quarkus RESTEasy Reactive + Micrometer metrics
- Kafka consumer for Appointment events
- Prometheus + Grafana for visualization
- Optional: Jaeger tracing integration for end-to-end monitoring

---

### 6️⃣ Billing Service (NEW)
**Responsibilities:**
- Manage billing and payment processing for appointments
- Generate invoices for completed appointments
- Process payments via multiple payment gateways (Stripe, PayPal)
- Handle insurance claims and verification
- Track payment status and send payment reminders
- Generate financial reports and revenue tracking
- Manage refunds and dispute resolution
- Emit billing events for analytics

**Endpoints:**
- `POST /api/billing/invoices` — Create invoice for appointment
- `GET /api/billing/invoices/{id}` — Get invoice details
- `GET /api/billing/invoices/patient/{patientId}` — Get patient invoices
- `GET /api/billing/invoices/appointment/{appointmentId}` — Get invoice by appointment
- `PUT /api/billing/invoices/{id}` — Update invoice
- `POST /api/billing/payments` — Process payment
- `GET /api/billing/payments/{id}` — Get payment details
- `GET /api/billing/payments/invoice/{invoiceId}` — Get payments for invoice
- `POST /api/billing/refunds` — Process refund
- `GET /api/billing/refunds/{id}` — Get refund status
- `POST /api/billing/insurance/verify` — Verify insurance coverage
- `POST /api/billing/insurance/claim` — Submit insurance claim
- `GET /api/billing/insurance/claim/{id}` — Get claim status
- `GET /api/billing/patient/{patientId}/balance` — Get patient outstanding balance
- `GET /api/billing/reports/revenue` — Revenue reports
- `GET /api/billing/reports/outstanding` — Outstanding payments report
- `POST /api/billing/reminders/send` — Send payment reminder
- `GET /api/billing/payment-methods/patient/{patientId}` — Get saved payment methods
- `POST /api/billing/payment-methods` — Save payment method
- `DELETE /api/billing/payment-methods/{id}` — Remove payment method

**Technologies:**
- Quarkus RESTEasy Reactive + Hibernate ORM Panache
- PostgreSQL for persistence
- Kafka producer/consumer for billing events
- Stripe/PayPal SDK integration for payment processing
- SmallRye Fault Tolerance for payment gateway resilience
- Micrometer Prometheus for billing metrics
- Quarkus Scheduler for payment reminders
- OpenAPI + Swagger UI for documentation

**Domain Events:**
- `InvoiceCreated`
- `PaymentProcessed`
- `PaymentFailed`
- `RefundIssued`
- `InsuranceClaimSubmitted`
- `PaymentReminderSent`

---

## 2) Architecture Overview

### Microservice Interaction Flow:
1. **Patient Service** handles registration and stores user profiles
2. **Doctor Service** provides real-time availability data
3. **Appointment Service** handles booking, emits `AppointmentCreated` events
4. **Notification Service** consumes those events and sends confirmations
5. **Billing Service** creates invoices for appointments and processes payments
6. **Analytics Service** listens to all event streams and visualizes metrics

### Enhanced Event Flow:
```
Patient Registration → Appointment Booking → Doctor Availability Check
                    ↓
              Appointment Confirmed
                    ↓
    ┌───────────────┼───────────────┐
    ↓               ↓               ↓
Notification    Billing         Analytics
  Service       Service          Service
    ↓               ↓               ↓
Send Email    Create Invoice   Track Metrics
```

---

## 3) Domain-Driven Design (DDD)

### Bounded Contexts:
- **Patient BC**: Registration, medical data, contact info, insurance
- **Doctor BC**: Profiles, specialties, slot availability, reviews
- **Scheduling BC**: Appointment booking, rescheduling, cancellation, waiting lists
- **Notification BC**: Email/SMS delivery, templates, and event triggers
- **Analytics BC**: Data aggregation, dashboards, and service metrics
- **Billing BC** (NEW): Invoicing, payments, insurance claims, refunds

### Domain Events:
- `PatientRegistered`
- `AppointmentCreated`
- `AppointmentCancelled`
- `AppointmentCompleted`
- `DoctorAvailabilityUpdated`
- `NotificationSent`
- `InvoiceCreated`
- `PaymentProcessed`
- `PaymentFailed`
- `RefundIssued`
- `InsuranceClaimSubmitted`
- `MetricsAggregated`

### Aggregates:
- **Patient**: profile + contact info + insurance + medical history
- **Doctor**: profile + availability slots + reviews
- **Appointment**: patientId, doctorId, startTime, status, type
- **Notification**: type, recipient, message, status, channel
- **Invoice**: appointmentId, patientId, amount, items, status
- **Payment**: invoiceId, amount, method, status, transactionId
- **Metrics**: appointmentCount, doctorUtilization, revenue, cancellationRate

---

## 4) Monitoring, Testing & Deployment

### Monitoring:
- Prometheus collects metrics from all six services via Micrometer
- Grafana provides dashboards for latency, throughput, event lag, and revenue
- Jaeger tracing captures distributed transaction spans across all services
- Custom dashboards for billing metrics (payment success rate, refund rate)

### Testing:
- `@QuarkusTest` for REST endpoints and Panache repositories
- Testcontainers for Kafka, PostgreSQL, and payment gateway mocks
- Fault tolerance & retry behavior tested under simulated downtime
- Integration tests for payment processing with mock payment gateways
- Contract testing between services

### Deployment:
- Containerized with Docker; each service has its own image
- Deployed via Kubernetes/Minikube for scalability and load balancing
- CI/CD pipeline via Jenkins or GitHub Actions
- Helm charts for easier Kubernetes deployment
- Blue-green deployment strategy for zero-downtime updates

---

## 5) Technology Stack

| Category           | Technology                                        |
|--------------------|---------------------------------------------------|
| Framework          | Quarkus (RESTEasy Reactive, Panache ORM)          |
| Messaging          | Kafka (SmallRye Reactive Messaging)               |
| Database           | PostgreSQL                                        |
| Observability      | Micrometer, Prometheus, Grafana, Jaeger           |
| Testing            | QuarkusTest, RestAssured, Testcontainers          |
| Fault Tolerance    | SmallRye Fault Tolerance (Retry, Circuit Breaker) |
| API Docs           | OpenAPI + Swagger UI                              |
| Authentication     | OIDC/JWT via Keycloak (Optional)                  |
| Containerization   | Docker / Podman                                   |
| Orchestration      | Kubernetes / Minikube                             |
| Scheduler          | Quarkus Scheduler (for reminders)                 |
| Payment Processing | Stripe SDK, PayPal SDK                            |
| Native Build       | GraalVM (Optional)                                |

---

## 6) Reactive & Self-Healing Demo Scenario

- **Responsiveness**: Fast API response under high traffic across all services
- **Resiliency**: Services retry failed connections; circuit breakers protect payment gateways
- **Elasticity**: All services scale horizontally under load
- **Message-Driven**: Kafka ensures asynchronous, decoupled communication
- **Self-Healing**: Circuit breaker opens on payment gateway failures, then auto-resets
- **Billing Resilience**: Payment retries with exponential backoff for transient failures

---

## 7) Summary of Enhancements

### New Service Added:
✅ **Billing Service** - Complete payment processing, invoicing, and insurance management

### Total Services: **6**

### Total Public API Endpoints: **~100+**
- Patient Service: 12 endpoints
- Doctor Service: 13 endpoints
- Appointment Service: 14 endpoints
- Notification Service: 9 endpoints
- Analytics Service: 12 endpoints
- Billing Service: 20 endpoints

### Key Features of Billing Service:
- Invoice generation and management
- Multi-gateway payment processing (Stripe, PayPal)
- Insurance verification and claims
- Refund processing
- Payment reminders via scheduler
- Revenue reporting and analytics integration
- Saved payment methods for patients
- Outstanding balance tracking

---

## 8) Conclusion

This **six-service architecture** provides a comprehensive, production-grade demonstration of microservice design principles using Quarkus. The addition of the **Billing Service** completes the healthcare ecosystem by handling the critical financial aspects of healthcare appointment management. With **100+ public API endpoints**, the system demonstrates extensive functionality while maintaining scalability, observability, and fault tolerance. All services communicate asynchronously via Kafka, ensuring loose coupling and high resilience.

## Contributors
<a href="https://github.com/akhundMurad"><img src="https://avatars.githubusercontent.com/u/73444365?v=4" width="60px" alt="Murad Akhundov"/></a>
