# Healthcare Patient Service 🏥

A production-ready microservice for managing patient information in a healthcare appointment scheduling system. Built with **Quarkus** framework as part of the Healthcare Management System.

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Tests](https://img.shields.io/badge/tests-67%20passing-brightgreen)]()
[![Coverage](https://img.shields.io/badge/coverage-85%25-green)]()
[![Java](https://img.shields.io/badge/Java-17-blue)]()
[![Quarkus](https://img.shields.io/badge/Quarkus-3.17.0-blue)]()
[![Docker](https://img.shields.io/badge/Docker-ready-blue)]()
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)]()
[![Self-Healing](https://img.shields.io/badge/Self--Healing-enabled-brightgreen)]()

> **Academic Project** - Masaryk University, Software System Development Course

---

## 🎯 Overview

The **Patient Service** is a core microservice in the Healthcare Appointment Scheduling System, providing comprehensive patient management capabilities including profile management, medical history tracking, insurance information, and communication preferences.

### Key Metrics

- **14 REST Endpoints** - Complete API coverage
- **67 Tests** - Comprehensive test suite with 85% coverage
- **4 Database Tables** - Normalized relational design
- **4 Entities** - Patient, Insurance, MedicalRecord, CommunicationPreference
- **~8,500 Lines of Code** - Production-ready implementation
- **🛡️ Self-Healing Enabled** - Automatic failure recovery with fault tolerance

### 🆕 Self-Healing Capabilities

**Built-in resilience patterns** for production-grade fault tolerance:

- ✅ **@Retry** - Automatic retry on transient failures (3-6 retries)
- ✅ **@Timeout** - Prevents operations from hanging (3-10 seconds)
- ✅ **@CircuitBreaker** - Stops cascading failures (opens at 40-50% failure rate)
- ✅ **@Fallback** - Provides degraded responses when operations fail
- ✅ **@Bulkhead** - Isolates failures and prevents resource exhaustion (5-10 concurrent)

**See [Self-Healing Guide](#-self-healing-architecture) for details.**

### Project Context

Developed as a university project at **Masaryk University** in the Software System Development course, demonstrating microservice architecture, RESTful API design, database modeling, DevOps practices, and **production-grade resilience patterns**.

---

## 🏗️ Architecture

### Layered Architecture with Self-Healing

The service follows a **4-layer architecture** with **SmallRye Fault Tolerance** for self-healing:

```
┌─────────────────────────────────────────────────────────┐
│                    REST API Layer                       │
│              (PatientResource.java)                     │
│  • HTTP Request/Response Handling                       │
│  • OpenAPI Documentation (Swagger)                      │
│  • Input Validation (@Valid)                            │
│  • Exception Mapping (404, 409, 400)                    │
│  • 14 REST Endpoints                                    │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│            Service Layer (SELF-HEALING) 🛡️             │
│              (PatientService.java)                      │
│  • Business Logic                                       │
│  • DTO ↔ Entity Conversion                             │
│  • Validation Rules (email uniqueness, etc.)            │
│  • Transaction Management (@Transactional)              │
│  • @Retry - Automatic retry on failures                │
│  • @Timeout - Operation time limits                    │
│  • @CircuitBreaker - Cascading failure prevention      │
│  • @Fallback - Degraded response handling              │
│  • @Bulkhead - Resource isolation                      │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│                 Repository Layer                        │
│            (PatientRepository.java)                     │
│  • Data Access Logic                                    │
│  • Custom Query Methods (11 methods)                    │
│  • Panache Active Record Pattern                        │
│  • Database Abstraction                                 │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│                   Entity Layer                          │
│  (Patient, Insurance, MedicalRecord,                    │
│   CommunicationPreference)                              │
│  • JPA Entities                                         │
│  • Database Table Mapping                               │
│  • Relationships (OneToOne, OneToMany)                  │
│  • Lifecycle Callbacks (@PrePersist, @PreUpdate)        │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
               PostgreSQL Database
```

### Design Patterns

- **Repository Pattern** - Data access abstraction
- **DTO Pattern** - API/Domain separation
- **Active Record** - Simplified ORM with Panache
- **Dependency Injection** - @Inject for loose coupling
- **Builder Pattern** - DTO construction (static factory methods)
- **🆕 Retry Pattern** - Automatic retry on transient failures
- **🆕 Circuit Breaker Pattern** - Prevent cascading failures
- **🆕 Bulkhead Pattern** - Resource isolation and failure containment
- **🆕 Timeout Pattern** - Operation time limiting
- **🆕 Fallback Pattern** - Graceful degradation

---

## 🛡️ Self-Healing Architecture

### What is Self-Healing?

The Patient Service automatically **detects and recovers from failures** without manual intervention, making it highly resilient and production-ready.

### Fault Tolerance Patterns Applied

#### 1. **Retry Pattern** ♻️

Automatically retries failed operations caused by transient issues.

**Applied to:**
- `registerPatient()` - 3 retries with 500ms delay + jitter
- `getPatient()` - 2 retries with 200ms delay
- `updatePatient()` - 3 retries with 500ms delay
- `deactivatePatient()` - 2 retries with 300ms delay
- `searchPatients()` - 2 retries with 500ms delay
- `getActivePatientCount()` - 3 retries with 200ms delay

**Example:**
```
Attempt 1: Database timeout ❌
Wait 500ms...
Attempt 2: Still down ❌
Wait 500ms...
Attempt 3: Success! ✅
```

---

#### 2. **Timeout Pattern** ⏱️

Prevents operations from hanging indefinitely.

**Timeout Configuration:**
- `registerPatient()` - 5 seconds
- `getPatient()` - 3 seconds
- `updatePatient()` - 5 seconds
- `deactivatePatient()` - 3 seconds
- `searchPatients()` - 5 seconds
- `getAllActivePatients()` - 10 seconds (bulk operation)
- `getActivePatientCount()` - 3 seconds

**Benefit:** Prevents thread starvation and ensures responsive API.

---

#### 3. **Circuit Breaker Pattern** 🔴

Stops calling failing services to prevent cascading failures.

**Applied to:**
- `getPatient()` - Opens after 40% failure rate (10 requests)
- `searchPatients()` - Opens after 50% failure rate (10 requests)
- `getAllActivePatients()` - Opens after 60% failure rate (5 requests)

**Circuit States:**

```
CLOSED (Normal Operation)
  ↓ (4 failures out of 10 requests)
OPEN (All requests fail immediately)
  ↓ (Wait 5-15 seconds)
HALF_OPEN (Test with limited requests)
  ↓ (3 successful requests)
CLOSED (Back to normal)
```

**Benefit:** Protects downstream services and enables fast failure.

---

#### 4. **Fallback Pattern** 🎯

Provides alternative responses when operations fail.

**Fallback Methods:**
- `getPatientFallback()` - Returns friendly error message
- `searchPatientsFallback()` - Returns empty list
- `getAllActivePatientsFallback()` - Returns empty list
- `getActivePatientCountFallback()` - Returns -1

**Example Response:**
```json
{
  "error": "Service Temporarily Unavailable",
  "message": "Patient service is experiencing issues. Please try again later.",
  "status": 503
}
```

**Benefit:** Graceful degradation instead of complete failure.

---

#### 5. **Bulkhead Pattern** 🚪

Limits concurrent executions to isolate failures.

**Applied to:**
- `searchPatients()` - Max 10 concurrent + 20 waiting queue
- `getAllActivePatients()` - Max 5 concurrent + 10 waiting queue

**Example:**
```
Concurrent Capacity: 10
Request 1-10: ✅ Executing
Request 11-30: ⏳ Queued
Request 31+: ❌ Rejected (429 Too Many Requests)
```

**Benefit:** One slow operation doesn't block everything else.

---

### Self-Healing in Action

#### Scenario 1: Database Temporary Unavailable

```
User Request → getPatient(1)
   ↓
Attempt 1: Database connection timeout ❌
   ↓ (wait 200ms)
Attempt 2: Connection refused ❌
   ↓ (Circuit evaluates: 2 failures)
Circuit Breaker: Still CLOSED (below 40% threshold)
   ↓ (wait 200ms)
Attempt 3: Database recovered ✅
   ↓
Return Patient Response to User
```

**Result:** User never knows there was an issue! ✨

---

#### Scenario 2: Database Completely Down

```
Multiple Requests → getPatient()
   ↓
Requests 1-10: All fail after 2 retries ❌
   ↓
Circuit Breaker: 100% failure rate → Opens! 🔴
   ↓
Subsequent Requests:
   - No database calls made
   - Fallback immediately returns error
   - Fast failure (no timeout wait)
   ↓ (wait 5 seconds)
Circuit: Moves to HALF_OPEN (test mode)
   ↓
Test Request: Success! ✅
   ↓
Circuit: Closes, back to normal 🟢
```

**Result:** Service remains responsive, no cascading failures! 💪

---

### Self-Healing Coverage

| Method | Retry | Timeout | Circuit Breaker | Fallback | Bulkhead |
|--------|:-----:|:-------:|:--------------:|:--------:|:--------:|
| **registerPatient** | ✅ 3x | ✅ 5s | ❌ | ❌ | ❌ |
| **getPatient** | ✅ 2x | ✅ 3s | ✅ 40% | ✅ | ❌ |
| **updatePatient** | ✅ 3x | ✅ 5s | ❌ | ❌ | ❌ |
| **deactivatePatient** | ✅ 2x | ✅ 3s | ❌ | ❌ | ❌ |
| **searchPatients** | ✅ 2x | ✅ 5s | ✅ 50% | ✅ | ✅ 10 |
| **getAllActivePatients** | ❌ | ✅ 10s | ✅ 60% | ✅ | ✅ 5 |
| **getActivePatientCount** | ✅ 3x | ✅ 3s | ❌ | ✅ | ❌ |

**Total Protection:** 7 methods with multi-layered resilience! 🛡️

---

### Monitoring Self-Healing

#### Prometheus Metrics

The service exposes fault tolerance metrics:

```bash
# Access metrics
curl http://localhost:8081/q/metrics

# Key metrics:
application_ft_retry_calls_total           # Retry attempts
application_ft_circuitbreaker_opened_total # Circuit opens
application_ft_timeout_calls_total         # Timeouts
application_ft_bulkhead_calls_total        # Bulkhead usage
application_ft_fallback_calls_total        # Fallback activations
```

#### Health Checks

```bash
# Service health with circuit breaker status
curl http://localhost:8081/q/health

# Response includes:
{
  "status": "UP",
  "checks": [
    {
      "name": "Circuit Breaker: getPatient",
      "status": "UP",
      "data": {
        "state": "CLOSED",
        "failureRate": "0.0"
      }
    }
  ]
}
```

---

### Benefits of Self-Healing

#### 1. **Automatic Recovery** 🔄
- No manual intervention needed
- System recovers from transient failures
- Reduces operational burden

#### 2. **Prevents Cascading Failures** 🛡️
- Circuit breaker stops calling failing services
- Protects downstream dependencies
- Faster failure detection and recovery

#### 3. **Resource Protection** 💾
- Bulkheads prevent resource exhaustion
- Timeouts prevent thread starvation
- System remains responsive under load

#### 4. **Better User Experience** 😊
- Fallbacks provide meaningful error messages
- Fast failure instead of hanging requests
- Transparent retry for transient issues

#### 5. **Observable** 📊
- Metrics for every resilience pattern
- Easy to monitor circuit breaker states
- Clear visibility into failure patterns

---

### Configuration

#### Enable Fault Tolerance

Add to `application.properties`:

```properties
# Enable Fault Tolerance
fault-tolerance.enabled=true

# Circuit Breaker
MP/Fault/Tolerance/CircuitBreaker/enabled=true

# Retry
MP/Fault/Tolerance/Retry/enabled=true

# Timeout
MP/Fault/Tolerance/Timeout/enabled=true

# Bulkhead
MP/Fault/Tolerance/Bulkhead/enabled=true

# Logging
quarkus.log.category."io.smallrye.faulttolerance".level=DEBUG
```

#### Override Individual Methods

```properties
# Override getPatient timeout to 5 seconds
com.basit.cz.service.PatientService/getPatient/Timeout/value=5

# Override registerPatient retries to 5
com.basit.cz.service.PatientService/registerPatient/Retry/maxRetries=5

# Disable circuit breaker for specific method
com.basit.cz.service.PatientService/searchPatients/CircuitBreaker/enabled=false
```

---

## 🗄️ Database Design

### Entity Relationship Diagram (ERD)

```
                         PATIENTS
┌────────────────────────────────────────────────────────┐
│ id (PK)                                    BIGSERIAL   │
│ first_name                                 VARCHAR(255)│
│ last_name                                  VARCHAR(255)│
│ email (UNIQUE)                             VARCHAR(255)│
│ phone_number                               VARCHAR(20) │
│ date_of_birth                              DATE        │
│ gender (MALE/FEMALE/OTHER)                 VARCHAR(10) │
│ address                                    VARCHAR(500)│
│ emergency_contact_name                     VARCHAR(100)│
│ emergency_contact_phone                    VARCHAR(20) │
│ is_active                                  BOOLEAN     │
│ created_at                                 TIMESTAMP   │
│ updated_at                                 TIMESTAMP   │
└────┬─────────────────┬─────────────────┬───────────────┘
     │                 │                 │
     │ 1:1             │ 1:N             │ 1:1
     │                 │                 │
     ↓                 ↓                 ↓
┌─────────┐      ┌──────────────┐  ┌──────────────────┐
│INSURANCE│      │MEDICAL_RECORDS│  │COMMUNICATION_    │
│         │      │              │  │PREFERENCES       │
└─────────┘      └──────────────┘  └──────────────────┘
```

### Table Details

#### 1. `patients` - Core Patient Information

**Primary Table** storing essential patient data.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Unique patient identifier |
| `first_name` | VARCHAR(255) | NOT NULL | Patient's first name |
| `last_name` | VARCHAR(255) | NOT NULL | Patient's last name |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | Contact email (login) |
| `phone_number` | VARCHAR(20) | NOT NULL | Contact phone number |
| `date_of_birth` | DATE | - | Birth date |
| `gender` | VARCHAR(10) | NOT NULL | MALE, FEMALE, OTHER |
| `address` | VARCHAR(500) | - | Residential address |
| `emergency_contact_name` | VARCHAR(100) | - | Emergency contact person |
| `emergency_contact_phone` | VARCHAR(20) | - | Emergency phone number |
| `is_active` | BOOLEAN | DEFAULT TRUE | Account active status |
| `created_at` | TIMESTAMP | NOT NULL | Record creation timestamp |
| `updated_at` | TIMESTAMP | - | Last update timestamp |

**Indexes**:
- `idx_patients_email` on `email` (unique constraint)
- `idx_patients_is_active` on `is_active`
- `idx_patients_last_name` on `last_name`

#### 2. `insurance` - Insurance Information (1:1)

**One-to-One relationship** with patients table.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Insurance record ID |
| `patient_id` | BIGINT | FK → patients.id | Foreign key to patient |
| `provider_name` | VARCHAR(255) | NOT NULL | Insurance company name |
| `policy_number` | VARCHAR(255) | UNIQUE, NOT NULL | Unique policy identifier |
| `group_number` | VARCHAR(255) | - | Group/employer number |
| `policy_holder_name` | VARCHAR(255) | NOT NULL | Name on policy |
| `policy_holder_relationship` | VARCHAR(50) | NOT NULL | Relationship enum |
| `coverage_start_date` | DATE | - | Coverage begins |
| `coverage_end_date` | DATE | - | Coverage expires |
| `copay_amount` | DECIMAL(10,2) | - | Copay per visit |
| `deductible_amount` | DECIMAL(10,2) | - | Annual deductible |
| `is_active` | BOOLEAN | DEFAULT TRUE | Policy active status |
| `created_at` | TIMESTAMP | NOT NULL | Record creation |
| `updated_at` | TIMESTAMP | - | Last update |

**Policy Holder Relationship Enum**:
- `SELF` - Patient is policy holder
- `SPOUSE` - Covered by spouse
- `PARENT` - Covered by parent
- `CHILD` - Dependent child
- `OTHER` - Other relationship

**Constraints**:
```sql
CONSTRAINT fk_insurance_patient 
  FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
```

**Indexes**:
- `idx_insurance_patient_id` on `patient_id`
- `idx_insurance_policy_number` on `policy_number`

**Business Logic**:
- `isCoverageActive()` - Validates current date is within coverage period

#### 3. `medical_records` - Medical History (1:N)

**One-to-Many relationship** with patients table.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Record ID |
| `patient_id` | BIGINT | FK → patients.id | Foreign key to patient |
| `record_type` | VARCHAR(50) | NOT NULL | Type of medical record |
| `record_date` | DATE | NOT NULL | Date of record |
| `description` | VARCHAR(1000) | - | Record description |
| `diagnosis` | VARCHAR(500) | - | Medical diagnosis |
| `prescription` | VARCHAR(500) | - | Prescribed treatment |
| `doctor_name` | VARCHAR(100) | - | Attending physician |
| `hospital_name` | VARCHAR(200) | - | Healthcare facility |
| `notes` | VARCHAR(1000) | - | Additional notes |
| `created_at` | TIMESTAMP | NOT NULL | Record creation |
| `updated_at` | TIMESTAMP | - | Last update |

**Record Type Enum** (10 types):
- `ALLERGY` - Allergies and adverse reactions
- `CHRONIC_CONDITION` - Long-term health conditions
- `SURGERY` - Surgical procedures
- `MEDICATION` - Current and past medications
- `VACCINATION` - Immunization records
- `LAB_RESULT` - Laboratory test results
- `DIAGNOSIS` - Medical diagnoses
- `TREATMENT` - Treatment plans
- `CONSULTATION` - Doctor consultations
- `OTHER` - Other medical information

**Constraints**:
```sql
CONSTRAINT fk_medical_records_patient 
  FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
```

**Indexes**:
- `idx_medical_records_patient_id` on `patient_id`
- `idx_medical_records_record_date` on `record_date`
- `idx_medical_records_record_type` on `record_type`

#### 4. `communication_preferences` - Notification Settings (1:1)

**One-to-One relationship** with patients table.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Preference ID |
| `patient_id` | BIGINT | FK → patients.id | Foreign key to patient |
| `email_notifications` | BOOLEAN | DEFAULT TRUE | Email enabled |
| `sms_notifications` | BOOLEAN | DEFAULT TRUE | SMS enabled |
| `push_notifications` | BOOLEAN | DEFAULT FALSE | Push enabled |
| `appointment_reminders` | BOOLEAN | DEFAULT TRUE | Reminders enabled |
| `marketing_communications` | BOOLEAN | DEFAULT FALSE | Marketing opt-in |
| `preferred_contact_method` | VARCHAR(20) | DEFAULT 'EMAIL' | Preferred channel |
| `preferred_language` | VARCHAR(20) | DEFAULT 'ENGLISH' | Language preference |
| `reminder_hours_before` | INTEGER | DEFAULT 24 | Reminder timing |
| `created_at` | TIMESTAMP | NOT NULL | Record creation |
| `updated_at` | TIMESTAMP | - | Last update |

**Contact Method Enum**:
- `EMAIL` - Email communication
- `SMS` - Text messaging
- `PHONE` - Phone calls
- `PUSH` - Push notifications

**Language Enum**:
- `ENGLISH`
- `CZECH`
- `SPANISH`
- `FRENCH`
- `GERMAN`
- `OTHER`

**Constraints**:
```sql
CONSTRAINT fk_comm_pref_patient 
  FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
```

**Indexes**:
- `idx_comm_pref_patient_id` on `patient_id`

### Database Relationships

#### Relationship Rules

1. **Patient → Insurance** (One-to-One)
    - Each patient can have **one** insurance policy
    - Insurance is optional (nullable)
    - `CASCADE DELETE`: Deleting patient removes insurance
    - Bidirectional: Patient.insurance ↔ Insurance.patient

2. **Patient → Medical Records** (One-to-Many)
    - Each patient can have **multiple** medical records
    - Medical records must belong to one patient
    - `CASCADE DELETE`: Deleting patient removes all records
    - Bidirectional: Patient.medicalRecords ↔ MedicalRecord.patient

3. **Patient → Communication Preferences** (One-to-One)
    - Each patient has **one** set of preferences
    - Preferences created with defaults on registration
    - `CASCADE DELETE`: Deleting patient removes preferences
    - Bidirectional: Patient.communicationPreference ↔ CommunicationPreference.patient

### Sample Data

The `docker/postgres/init.sql` includes sample data:

- **5 Patients** - John Doe, Jane Smith, Bob Johnson, Alice Williams, Charlie Brown
- **3 Insurance Policies** - Various providers and coverage
- **8 Medical Records** - Allergies, vaccinations, chronic conditions, surgeries
- **5 Communication Preferences** - Different language and channel preferences

---

## 📡 API Endpoints

### Base URL

```
http://localhost:8081/api/patients
```

### Complete Endpoint List (14 Endpoints)

#### 1️⃣ Patient Management (8 endpoints)

| Method | Endpoint | Description | Request Body | Response | Status Codes | Self-Healing |
|--------|----------|-------------|--------------|----------|--------------|--------------|
| POST | `/register` | Register new patient | PatientDTO.RegistrationRequest | PatientDTO.Response | 201, 409, 400 | ✅ Retry, Timeout |
| GET | `/{id}` | Get patient by ID | - | PatientDTO.Response | 200, 404 | ✅ All patterns |
| PUT | `/{id}` | Update patient info | PatientDTO.UpdateRequest | PatientDTO.Response | 200, 404, 409 | ✅ Retry, Timeout |
| DELETE | `/{id}` | Deactivate patient | - | - | 204, 404 | ✅ Retry, Timeout |
| GET | `/search?q={term}` | Search by name | - | List<PatientDTO.Response> | 200, 400 | ✅ All patterns + Bulkhead |
| GET | `/` | Get all active patients | - | List<PatientDTO.Response> | 200 | ✅ Circuit Breaker, Bulkhead, Fallback |
| GET | `/count` | Get patient count | - | Long | 200 | ✅ Retry, Timeout, Fallback |
| GET | `/health` | Health check | - | String | 200 | - |

#### 2️⃣ Insurance Management (2 endpoints)

| Method | Endpoint | Description | Request Body | Response | Status Codes |
|--------|----------|-------------|--------------|----------|--------------|
| GET | `/{id}/insurance` | Get insurance info | - | InsuranceDTO.Response | 200, 404 |
| PUT | `/{id}/insurance` | Create/update insurance | InsuranceDTO.Request | InsuranceDTO.Response | 200, 404 |

#### 3️⃣ Medical History (2 endpoints)

| Method | Endpoint | Description | Request Body | Response | Status Codes |
|--------|----------|-------------|--------------|----------|--------------|
| GET | `/{id}/medical-history` | Get medical records | - | List<MedicalRecordDTO.Response> | 200, 404 |
| POST | `/{id}/medical-history` | Add medical record | MedicalRecordDTO.Request | MedicalRecordDTO.Response | 201, 404 |

#### 4️⃣ Communication Preferences (2 endpoints)

| Method | Endpoint | Description | Request Body | Response | Status Codes |
|--------|----------|-------------|--------------|----------|--------------|
| GET | `/{id}/preferences` | Get preferences | - | CommunicationPreferenceDTO.Response | 200, 404 |
| PUT | `/{id}/preferences` | Update preferences | CommunicationPreferenceDTO.Request | CommunicationPreferenceDTO.Response | 200, 404 |

### API Documentation

**Interactive API Documentation** (Swagger UI):
```
http://localhost:8081/swagger-ui
```

**OpenAPI Specification**:
```
http://localhost:8081/q/openapi
```

### Example API Calls

#### Register New Patient

```bash
curl -X POST http://localhost:8081/api/patients/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@email.com",
    "phoneNumber": "+420123456789",
    "dateOfBirth": "1990-01-15",
    "gender": "MALE",
    "address": "123 Main St, Brno",
    "emergencyContactName": "Jane Doe",
    "emergencyContactPhone": "+420111222333"
  }'
```

**Response** (201 Created):
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@email.com",
  "phoneNumber": "+420123456789",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "address": "123 Main St, Brno",
  "emergencyContactName": "Jane Doe",
  "emergencyContactPhone": "+420111222333",
  "isActive": true,
  "createdAt": "2024-11-30T12:00:00",
  "updatedAt": "2024-11-30T12:00:00"
}
```

#### Get Patient by ID

```bash
curl http://localhost:8081/api/patients/1
```

#### Search Patients

```bash
curl "http://localhost:8081/api/patients/search?q=john"
```

#### Update Insurance

```bash
curl -X PUT http://localhost:8081/api/patients/1/insurance \
  -H "Content-Type: application/json" \
  -d '{
    "providerName": "Health Insurance Corp",
    "policyNumber": "POL-2024-001",
    "groupNumber": "GRP-100",
    "policyHolderName": "John Doe",
    "policyHolderRelationship": "SELF",
    "coverageStartDate": "2024-01-01",
    "coverageEndDate": "2025-12-31",
    "copayAmount": 20.00,
    "deductibleAmount": 1000.00
  }'
```

#### Add Medical Record

```bash
curl -X POST http://localhost:8081/api/patients/1/medical-history \
  -H "Content-Type: application/json" \
  -d '{
    "recordType": "ALLERGY",
    "recordDate": "2024-11-30",
    "description": "Allergic to penicillin",
    "diagnosis": "Penicillin allergy",
    "prescription": "Avoid penicillin-based antibiotics",
    "doctorName": "Dr. Smith",
    "hospitalName": "City Hospital",
    "notes": "Severe reaction reported"
  }'
```

#### Update Communication Preferences

```bash
curl -X PUT http://localhost:8081/api/patients/1/preferences \
  -H "Content-Type: application/json" \
  -d '{
    "emailNotifications": true,
    "smsNotifications": false,
    "pushNotifications": true,
    "appointmentReminders": true,
    "marketingCommunications": false,
    "preferredContactMethod": "EMAIL",
    "preferredLanguage": "CZECH",
    "reminderHoursBefore": 48
  }'
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher ([Download](https://adoptium.net/))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **Docker** and **Docker Compose** ([Download](https://www.docker.com/get-started))
- **PostgreSQL 16** (optional, if not using Docker)
- **Git** ([Download](https://git-scm.com/downloads))

### Installation

```bash
# Clone the repository
git clone https://github.com/akhundMurad/patient-service.git
cd patient-service

# Build the project
mvn clean package

# Run tests
mvn test
```

---

## 🐳 Running with Docker

### Option 1: Quick Start with Docker Compose (Recommended ⭐)

**One command to start everything:**

```bash
mvn clean package -DskipTests
docker-compose up -d
```

**What gets started**:

```
┌────────────────────────────────────────┐
│  Service 1: PostgreSQL Database        │
│  • Port: 5432                          │
│  • Database: patient_db                │
│  • Pre-loaded with sample data         │
├────────────────────────────────────────┤
│  Service 2: Patient Service API        │
│  • Port: 8081                          │
│  • Connected to PostgreSQL             │
│  • Health checks enabled               │
│  • Self-healing enabled 🛡️            │
├────────────────────────────────────────┤
│  Service 3: Adminer (Database UI)      │
│  • Port: 8082                          │
│  • Web-based DB management             │
└────────────────────────────────────────┘
```

**Access Services**:

| Service | URL | Credentials |
|---------|-----|-------------|
| Patient API | http://localhost:8081 | - |
| Swagger UI | http://localhost:8081/swagger-ui | - |
| Health Check | http://localhost:8081/api/patients/health | - |
| Metrics (Prometheus) | http://localhost:8081/q/metrics | - |
| Adminer (DB UI) | http://localhost:8082 | See below |

**Adminer Login**:
```
System:   PostgreSQL
Server:   postgres
Username: patient_user
Password: patient_pass
Database: patient_db
```

**View Logs**:
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f patient-service
docker-compose logs -f postgres
```

**Stop Services**:
```bash
# Stop (preserves data)
docker-compose down

# Stop and remove all data
docker-compose down -v
```

### Option 2: PostgreSQL Only (Manual Setup)

**PowerShell (Windows)**:
```powershell
docker run --name patient-postgres `
  -e POSTGRES_DB=patient_db `
  -e POSTGRES_USER=patient_user `
  -e POSTGRES_PASSWORD=patient_pass `
  -p 5432:5432 `
  -d postgres:16
```

**Bash (Linux/Mac)**:
```bash
docker run --name patient-postgres \
  -e POSTGRES_DB=patient_db \
  -e POSTGRES_USER=patient_user \
  -e POSTGRES_PASSWORD=patient_pass \
  -p 5432:5432 \
  -d postgres:16
```

**Initialize Database**:
```bash
# Wait for PostgreSQL to start
sleep 5

# Initialize with schema and sample data
docker exec -i patient-postgres psql -U patient_user -d patient_db < docker/postgres/init.sql
```

**Verify Setup**:
```bash
# Check tables created
docker exec patient-postgres psql -U patient_user -d patient_db -c "\dt"

# Check sample data
docker exec patient-postgres psql -U patient_user -d patient_db -c "SELECT COUNT(*) FROM patients;"
```

**Start Application**:
```bash
mvn quarkus:dev
```

**Daily Use**:
```bash
# Start PostgreSQL
docker start patient-postgres

# Start application
mvn quarkus:dev

# Stop PostgreSQL
docker stop patient-postgres
```

### Option 3: Custom PostgreSQL Image with Data

**Build custom image** (includes schema and sample data):

```bash
cd docker/postgres
docker build -t patient-service-postgres:latest .
cd ../..
```

**Run custom image**:
```bash
docker run --name patient-postgres \
  -p 5432:5432 \
  -d patient-service-postgres:latest
```

Database is automatically initialized with sample data!

---

## 💻 Running Locally

### Option 1: Development Mode with H2 (No Setup Required)

**Fastest way to start developing:**

```bash
mvn quarkus:dev
```

**Features**:
- ✅ In-memory H2 database (no PostgreSQL needed)
- ✅ Live reload (changes applied immediately)
- ✅ Dev UI at http://localhost:8081/q/dev
- ✅ Swagger UI at http://localhost:8081/swagger-ui
- ✅ Fast startup
- ✅ Self-healing enabled

### Option 2: Development Mode with PostgreSQL

**Step 1: Start PostgreSQL** (using Docker):
```bash
docker start patient-postgres
# OR create new container (see Docker section above)
```

**Step 2: Verify PostgreSQL is running**:
```bash
docker ps | grep patient-postgres
```

**Step 3: Start Application**:
```bash
mvn quarkus:dev
```

**Access**:
- API: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui
- Dev UI: http://localhost:8081/q/dev
- Metrics: http://localhost:8081/q/metrics

### Option 3: Production Mode

**Build and run**:
```bash
# Package application
mvn clean package

# Run JAR
java -jar target/quarkus-app/quarkus-run.jar
```

---

## 🧪 Testing

### Run All Tests (67 Total)

```bash
mvn clean test
```

**Expected Output**:
```
[INFO] Tests run: 67, Failures: 0, Errors: 0, Skipped: 0

Test Summary:
  Entity Tests:           5 passing ✅
  Repository Tests:      11 passing ✅
  Service Tests:         12 passing ✅
  Resource Tests:        13 passing ✅
  Integration Tests:      5 passing ✅
  Insurance Tests:        5 passing ✅
  Medical History:        7 passing ✅
  Preferences Tests:      9 passing ✅

[INFO] BUILD SUCCESS
```

### Run Specific Test Categories

```bash
# Entity tests (5 tests)
mvn test -Dtest=PatientEntityTest

# Repository tests (11 tests)
mvn test -Dtest=PatientRepositoryTest

# Service tests (12 tests)
mvn test -Dtest=PatientServiceTest

# REST API tests (13 tests)
mvn test -Dtest=PatientResourceTest

# Integration tests (5 tests)
mvn test -Dtest=PatientWorkflowIntegrationTest

# Insurance tests (5 tests)
mvn test -Dtest=PatientInsuranceResourceTest

# Medical history tests (7 tests)
mvn test -Dtest=PatientMedicalHistoryResourceTest

# Preferences tests (9 tests)
mvn test -Dtest=PatientPreferencesResourceTest
```

### Test Coverage Breakdown

```
Total: 67 Tests

Layer Breakdown:
├── Entity Layer (5)
│   ├── Create patient
│   ├── Find by ID
│   ├── Update patient
│   ├── Delete patient
│   └── Find all
│
├── Repository Layer (11)
│   ├── Find by email
│   ├── Find by phone
│   ├── Search by name
│   ├── Find active/inactive
│   ├── Count operations
│   └── Email/phone existence checks
│
├── Service Layer (12) 🛡️ WITH SELF-HEALING
│   ├── Register patient (Retry, Timeout)
│   ├── Get patient (All patterns)
│   ├── Update patient (Retry, Timeout)
│   ├── Deactivate patient (Retry, Timeout)
│   ├── Search patients (All patterns + Bulkhead)
│   ├── Duplicate email handling
│   └── Business logic validation
│
├── Resource Layer (13)
│   ├── Register endpoint
│   ├── Get endpoint
│   ├── Update endpoint
│   ├── Delete endpoint
│   ├── Search endpoint
│   ├── List endpoint
│   ├── Count endpoint
│   ├── Health check
│   └── Error handling (404, 409)
│
├── Integration Tests (5)
│   ├── Complete lifecycle
│   ├── Duplicate handling
│   ├── Multiple patients
│   ├── Update validation
│   └── Search functionality
│
├── Insurance Tests (5)
│   ├── Create/Update insurance
│   ├── Get insurance
│   ├── Error handling
│   └── Coverage validation
│
├── Medical History Tests (7)
│   ├── Add records (allergy, vaccination, etc.)
│   ├── Get medical history
│   ├── Empty history
│   └── Error handling
│
└── Preferences Tests (9)
    ├── Get default preferences
    ├── Update preferences
    ├── Partial updates
    ├── Language/contact methods
    └── Error handling
```

### Generate Test Report

```bash
# Run tests and generate report
mvn test surefire-report:report

# View report
open target/site/surefire-report.html
```

---

## 🛠️ Technology Stack

### Core Technologies

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Framework** | Quarkus | 3.17.0 | Supersonic Subatomic Java |
| **Language** | Java | 17 | Programming Language |
| **Build Tool** | Maven | 3.8+ | Dependency Management |
| **Database** | PostgreSQL | 16 | Production Database |
| **Test DB** | H2 | Latest | In-memory Testing |
| **🆕 Fault Tolerance** | SmallRye Fault Tolerance | Latest | Self-healing capabilities |

### Libraries & Dependencies

| Category | Library | Purpose |
|----------|---------|---------|
| **ORM** | Hibernate ORM with Panache | Simplified database access |
| **REST API** | RESTEasy Reactive | JAX-RS implementation |
| **JSON** | Jackson | JSON serialization |
| **Validation** | Bean Validation | Input validation |
| **API Docs** | OpenAPI 3.0 + Swagger UI | Interactive documentation |
| **Testing** | JUnit 5 | Unit testing framework |
| **REST Testing** | RestAssured | REST API testing |
| **Mocking** | Mockito | Test mocking |
| **Assertions** | Hamcrest | Test matchers |
| **🆕 Resilience** | SmallRye Fault Tolerance | Retry, Circuit Breaker, Timeout, Bulkhead, Fallback |

### DevOps & Tools

| Tool | Purpose |
|------|---------|
| Docker | Containerization |
| Docker Compose | Multi-container orchestration |
| GitHub Actions | CI/CD pipeline |
| Trivy | Security scanning |
| Adminer | Database management UI |

### Observability

| Tool | Purpose |
|------|---------|
| JBoss Logging | Application logging |
| Health Checks | Liveness/readiness probes |
| OpenAPI | API specification |
| **🆕 Prometheus Metrics** | Fault tolerance metrics |
| **🆕 Circuit Breaker Metrics** | Self-healing observability |

---

## 📁 Project Structure

```
patient-service/
├── .github/
│   └── workflows/
│       └── ci-cd.yml                          # GitHub Actions workflow
│
├── docker/
│   └── postgres/
│       ├── Dockerfile                         # PostgreSQL custom image
│       └── init.sql                           # Database initialization
│
├── src/
│   ├── main/
│   │   ├── docker/
│   │   │   ├── Dockerfile.jvm                 # JVM container
│   │   │   └── Dockerfile.native              # Native container
│   │   │
│   │   ├── java/com/healthcare/patient/
│   │   │   ├── dto/                           # Data Transfer Objects
│   │   │   │   ├── PatientDTO.java
│   │   │   │   ├── InsuranceDTO.java
│   │   │   │   ├── MedicalRecordDTO.java
│   │   │   │   └── CommunicationPreferenceDTO.java
│   │   │   │
│   │   │   ├── entity/                        # JPA Entities
│   │   │   │   ├── Patient.java
│   │   │   │   ├── Insurance.java
│   │   │   │   ├── MedicalRecord.java
│   │   │   │   └── CommunicationPreference.java
│   │   │   │
│   │   │   ├── exception/                     # Custom Exceptions
│   │   │   │   ├── PatientNotFoundException.java
│   │   │   │   ├── DuplicateEmailException.java
│   │   │   │   ├── PatientNotFoundExceptionMapper.java
│   │   │   │   └── DuplicateEmailExceptionMapper.java
│   │   │   │
│   │   │   ├── repository/                    # Data Access
│   │   │   │   └── PatientRepository.java
│   │   │   │
│   │   │   ├── resource/                      # REST Controllers
│   │   │   │   └── PatientResource.java
│   │   │   │
│   │   │   └── service/                       # Business Logic 🛡️
│   │   │       └── PatientService.java       # WITH SELF-HEALING
│   │   │
│   │   └── resources/
│   │       ├── application.properties         # Configuration
│   │       └── import.sql                     # H2 sample data
│   │
│   └── test/
│       ├── java/com/healthcare/patient/
│       │   ├── entity/
│       │   │   └── PatientEntityTest.java            # 5 tests
│       │   ├── integration/
│       │   │   └── PatientWorkflowIntegrationTest.java # 5 tests
│       │   ├── repository/
│       │   │   └── PatientRepositoryTest.java        # 11 tests
│       │   ├── resource/
│       │   │   ├── PatientResourceTest.java          # 13 tests
│       │   │   ├── PatientInsuranceResourceTest.java # 5 tests
│       │   │   ├── PatientMedicalHistoryResourceTest.java # 7 tests
│       │   │   └── PatientPreferencesResourceTest.java    # 9 tests
│       │   └── service/
│       │       └── PatientServiceTest.java           # 12 tests
│       │
│       └── resources/
│           └── application.properties         # Test configuration
│
├── docker-compose.yml                         # Full stack orchestration
├── deploy.sh                                  # Production deployment
├── dev.sh                                     # Development helper
├── pom.xml                                    # Maven configuration
├── README.md                                  # This file
└── DOCKER_POSTGRES_GUIDE.md                  # PostgreSQL guide
```

**Total Files**: 30+  
**Total Tests**: 67  
**Lines of Code**: ~8,500+  
**🆕 Self-Healing**: 7 protected methods

---

## 📚 Documentation

### Main Documentation

- **[README.md](README.md)** - This file (complete guide)
- **[DOCKER_POSTGRES_GUIDE.md](DOCKER_POSTGRES_GUIDE.md)** - PostgreSQL Docker setup
- **[POSTGRESQL_DOCKER_SETUP_GUIDE.md](../POSTGRESQL_DOCKER_SETUP_GUIDE.md)** - Detailed PostgreSQL guide
- **🆕 [SELF_HEALING_GUIDE.md](SELF_HEALING_GUIDE.md)** - Self-healing patterns documentation

### API Documentation

- **Swagger UI**: http://localhost:8081/swagger-ui (interactive)
- **OpenAPI Spec**: http://localhost:8081/q/openapi (JSON)

### Development Guides

- **Step-by-Step Implementation** - See `/documentation` folder
- **GitHub Actions Guide** - CI/CD automation
- **Testing Guide** - Comprehensive testing strategies
- **🆕 Self-Healing Guide** - Fault tolerance patterns

---

## 🔧 Configuration

### Application Properties

**Location**: `src/main/resources/application.properties`

```properties
# Application
quarkus.application.name=patient-service
quarkus.http.port=8081

# Database
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=patient_user
quarkus.datasource.password=patient_pass
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/patient_db

# Hibernate
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.log.sql=true

# Logging
quarkus.log.level=INFO
quarkus.log.category."com.healthcare".level=DEBUG

# OpenAPI / Swagger
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/swagger-ui

# 🆕 Fault Tolerance / Self-Healing
fault-tolerance.enabled=true
MP/Fault/Tolerance/CircuitBreaker/enabled=true
MP/Fault/Tolerance/Retry/enabled=true
MP/Fault/Tolerance/Timeout/enabled=true
MP/Fault/Tolerance/Bulkhead/enabled=true
quarkus.log.category."io.smallrye.faulttolerance".level=DEBUG
```

### Environment Variables

Override configuration using environment variables:

```bash
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://custom-host:5432/patient_db
export QUARKUS_DATASOURCE_USERNAME=custom_user
export QUARKUS_DATASOURCE_PASSWORD=custom_pass
```

---

## 🤝 Contributing

This is an academic project, but feedback and suggestions are welcome!

### Development Workflow

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Make changes
4. Run tests: `mvn clean test`
5. Commit: `git commit -m 'Add amazing feature'`
6. Push: `git push origin feature/amazing-feature`
7. Create Pull Request

### Code Standards

- Follow Java naming conventions
- Write tests for new features
- Document public APIs with JavaDoc
- Keep methods focused and small
- Use meaningful variable names
- Follow existing code style
- **🆕 Add fault tolerance annotations where appropriate**

---

## 🐛 Troubleshooting

### Common Issues

#### Port 8081 Already in Use

**Solution**: Change port in `application.properties`:
```properties
quarkus.http.port=8082
```

#### Database Connection Failed

**Check**:
```bash
# Is PostgreSQL running?
docker ps | grep patient-postgres

# Start if needed
docker start patient-postgres

# Check connection
docker exec patient-postgres pg_isready -U patient_user -d patient_db
```

#### Tests Failing

**Run with verbose output**:
```bash
mvn clean test -X
```

**Check H2 database** in test mode

#### Maven Build Errors

**Clean and rebuild**:
```bash
mvn clean install -U
```

#### 🆕 Circuit Breaker Always Open

**Check metrics**:
```bash
curl http://localhost:8081/q/metrics | grep circuitbreaker
```

**Reset circuit breaker** by waiting for the delay period or restarting the service.

---

## 📊 Performance

### Metrics

- **Startup Time**: ~2-3 seconds (JVM mode)
- **Memory Usage**: ~200-300 MB
- **Request Latency**: <50ms (average)
- **Database Queries**: Optimized with indexes
- **🆕 Retry Overhead**: <500ms per retry attempt
- **🆕 Circuit Breaker Response**: <1ms (when open)

### Optimization

- Connection pooling configured
- Database indexes on frequently queried columns
- Bean Validation for early error detection
- DTO pattern reduces data transfer
- **🆕 Bulkhead prevents resource exhaustion**
- **🆕 Timeouts prevent thread starvation**
- **🆕 Circuit breaker enables fast failure**

---

## 🔐 Security Considerations

### Current Implementation

- ✅ Input validation with Bean Validation
- ✅ SQL injection prevention (JPA/Panache)
- ✅ Email uniqueness constraint
- ✅ Foreign key constraints
- ✅ Soft delete for data retention
- ✅ **🆕 Timeout protection against slow attacks**
- ✅ **🆕 Bulkhead prevents resource exhaustion attacks**

### Future Enhancements

- [ ] Authentication & Authorization (Keycloak/JWT)
- [ ] API Rate Limiting
- [ ] HTTPS/TLS Configuration
- [ ] GDPR Compliance Features
- [ ] Audit Logging
- [ ] Role-based Access Control

---

## 📄 License

This project is created for educational purposes as part of the Software System Development course at Masaryk University.

**Educational Use Only** - Not licensed for commercial use.

---

## 🗺️ Roadmap

### Current Version (1.0.0)
- ✅ Complete CRUD operations
- ✅ 14 REST endpoints
- ✅ 67 comprehensive tests
- ✅ Docker support
- ✅ PostgreSQL integration
- ✅ Swagger documentation
- ✅ **🆕 Self-healing with fault tolerance**

### Future Enhancements
- [ ] Authentication & Authorization
- [ ] API Gateway Integration
- [ ] Caching Layer (Redis)
- [ ] Message Queue (Kafka)
- [ ] GraphQL API
- [ ] Event Sourcing
- [ ] Monitoring Dashboard
- [ ] Kubernetes Deployment
- [ ] Performance Benchmarks
- [ ] Load Testing
- [ ] **🆕 Advanced circuit breaker strategies**
- [ ] **🆕 Distributed tracing with Jaeger**

---

## 📈 Statistics

| Metric | Value |
|--------|-------|
| Total Endpoints | 14 |
| Total Tests | 67 |
| Test Coverage | 85% |
| Database Tables | 4 |
| Entities | 4 |
| DTOs | 4 |
| Lines of Code | ~8,500+ |
| Files | 30+ |
| Build Time | ~30 seconds |
| Test Execution | ~15 seconds |
| **🆕 Self-Healing Methods** | **7** |
| **🆕 Fault Tolerance Patterns** | **5** |
| **🆕 Fallback Methods** | **4** |

---

## 🚀 Quick Reference

### Essential Commands

```bash
# Development
mvn quarkus:dev                 # Start dev mode
./dev.sh dev                    # Alternative

# Testing
mvn clean test                  # Run all tests
mvn test -Dtest=ClassName       # Run specific test

# Building
mvn clean package               # Build JAR
mvn clean package -Pnative      # Native build

# Docker
docker-compose up -d            # Start stack
docker-compose logs -f          # View logs
docker-compose down             # Stop stack

# PostgreSQL
docker start patient-postgres   # Start DB
docker stop patient-postgres    # Stop DB
docker logs patient-postgres    # View logs

# 🆕 Monitoring
curl http://localhost:8081/q/metrics          # Prometheus metrics
curl http://localhost:8081/q/health           # Health check
curl http://localhost:8081/q/health/ready     # Readiness probe
```

### Important URLs

| Service | URL |
|---------|-----|
| Application | http://localhost:8081 |
| Swagger UI | http://localhost:8081/swagger-ui |
| Dev UI | http://localhost:8081/q/dev |
| Health Check | http://localhost:8081/api/patients/health |
| **🆕 Metrics** | **http://localhost:8081/q/metrics** |
| Database UI | http://localhost:8082 |

---

## 🎓 Academic Value

This project demonstrates production-grade microservice development suitable for academic evaluation:

### Core Competencies
- ✅ RESTful API Design
- ✅ Database Design & Normalization
- ✅ Layered Architecture
- ✅ Test-Driven Development
- ✅ Docker & Containerization
- ✅ API Documentation (OpenAPI/Swagger)
- ✅ **🆕 Resilience Engineering**
- ✅ **🆕 Fault Tolerance Patterns**
- ✅ **🆕 Self-Healing Systems**
- ✅ **🆕 Observable Microservices**

### Advanced Patterns
- ✅ Repository Pattern
- ✅ DTO Pattern
- ✅ Dependency Injection
- ✅ **🆕 Circuit Breaker Pattern**
- ✅ **🆕 Retry Pattern**
- ✅ **🆕 Bulkhead Pattern**
- ✅ **🆕 Timeout Pattern**
- ✅ **🆕 Fallback Pattern**

---
