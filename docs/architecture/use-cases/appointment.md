# AppointmentService use-cases documentation

This service handles the following use cases:

- Schedule appointment
- Reschedule appointment
- Cancel appointment

---

## Schedule appointment

### Happy scenario

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant AppointmentService
    participant WorkflowHandler
    participant CalendarService
    participant NotificationService
    participant AnalyticsService
    participant ExternalCalendarAPI
    participant AppointmentServiceDatabase

    rect green
    note right of Client: step=BEGIN_SCHEDULE_APPOINTMENT_WF
    Client->>APIGateway: 1.1. POST /calendar/availability
    APIGateway->>CalendarService: 1.2. GET /calendar/availability (success)
    CalendarService-->>APIGateway: 1.2.1 status=SUCCESS
    APIGateway-->>Client: 1.2.2 status=SUCCESS
    Client->>APIGateway: 1.3. POST /appointment/schedule
    APIGateway->>AppointmentService: 1.4. POST /appointment/schedule
    activate WorkflowHandler
    AppointmentService->>WorkflowHandler: 1.5. ScheduleAppointmentWF.begin()
    WorkflowHandler->>WorkflowHandler: 1.6. step=BEGIN_SCHEDULE_APPOINTMENT_WF
    end

    rect green
    note right of WorkflowHandler: step=CREATE_APPOINTMENT
    WorkflowHandler->>WorkflowHandler: 2. step=CREATE_APPOINTMENT

    activate AppointmentService
    AppointmentService->>CalendarService: 2.1. GET /calendar/availability (success)
    AppointmentService->>AppointmentServiceDatabase: 2.2. insert appointment record
    AppointmentService->>CalendarService: 2.3. POST /calendar/event
    CalendarService->>ExternalCalendarAPI: 2.4. create an event in external calendar (optional)
    CalendarService->>NotificationService: 2.5. POST /notification/broadcast
    deactivate AppointmentService
    end

   rect green
   note right of WorkflowHandler: step=EMIT_EVENTS
    WorkflowHandler->>WorkflowHandler: 3. step=EMIT_EVENTS
    WorkflowHandler->>AnalyticsService: 3.1. emit AppointmentScheduledEvent

    WorkflowHandler->>WorkflowHandler: 4. step=FINISH_SCHEDULE_APPOINTMENT_WF
    deactivate WorkflowHandler
    
    Client->>APIGateway: 5. GET /workflow/status
    APIGateway-->>Client: 5.1. status=SUCCESS
    end
    
```

1. **step=BEGIN_SCHEDULE_APPOINTMENT_WF** start workflow and pass necessary input parameters

2. **step=CREATE_APPOINTMENT** validates availability of participants before creating an appointment and event in the calendar.

    2.1. `GET /calendar/availability?participantId=patientId&participantId=doctorId&startTime=timestamp1&endTime=timestamp2` -- check availability of doctor and patient, fail if there's an issue.

    2.2. Create a row in the database if validation passed.

    2.3. Creates an event in the internal calendar with given parameters. 

    2.4. optional step -- sends request to external calendar (Google Calendar/Outlook) to create an event.

    2.5. `POST /notification/broadcast?recieverId=patientId&recieverId=doctorId&notification=APPOINTMENT_SCHEDULED` broadcast a notification to patient and doctor.

3. **step=EMIT_EVENTS** notify the analytics service about a new appointment (might execute ETL in the background).

**Requirements:**

- Entire use case is handled in the background.
- Steps 1-2 must have transactional behavior.
- Graceful shutdown should be implemented.
- We should have an endpoint to get process status.
- Proper logging and metrics must be implemented.

### Failure scenario

Suppose participant's time slot has been reserved after client made validation.

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant AppointmentService
    participant WorkflowHandler
    participant CalendarService

   Client->>APIGateway: 1.1. POST /calendar/availability
   APIGateway->>CalendarService: 1.2. GET /calendar/availability (success)
   CalendarService-->>APIGateway: 1.2.1 status=SUCCESS
   APIGateway-->>Client: 1.2.2 status=SUCCESS
   Client->>APIGateway: 1.3. POST /appointment/schedule
   APIGateway->>AppointmentService: 1.4. POST /appointment/schedule
   activate WorkflowHandler
   AppointmentService->>WorkflowHandler: 1.5. ScheduleAppointmentWF.begin()
   WorkflowHandler->>WorkflowHandler: 1.6. step=BEGIN_SCHEDULE_APPOINTMENT_WF

    WorkflowHandler->>WorkflowHandler: 2. step=CREATE_APPOINTMENT

    activate AppointmentService
    AppointmentService->>CalendarService: 2.1. GET /calendar/availability (failed)
    deactivate AppointmentService

    WorkflowHandler->>WorkflowHandler: 3. step=FAIL_SCHEDULE_APPOINTMENT_WF

   Client->>APIGateway: 4. GET /workflow/status
   APIGateway-->>Client: 4.1. status=FAILED, reason=""

    deactivate WorkflowHandler
```

**Requirements:**

- Appointment should not be created.
- Transaction should be aborted.
- User should be notified about the issue.

---

## Re-schedule appointment

### Happy scenario

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant AppointmentService
    participant WorkflowHandler
    participant CalendarService
    participant NotificationService
    participant AnalyticsService
    participant ExternalCalendarAPI
    participant AppointmentServiceDatabase

    rect green
    note right of Client: step=BEGIN_RESCHEDULE_APPOINTMENT_WF
    Client->>APIGateway: 1.1. POST /calendar/availability
    APIGateway->>CalendarService: 1.2. GET /calendar/availability (success)
    CalendarService-->>APIGateway: 1.2.1 status=SUCCESS
    APIGateway-->>Client: 1.2.2 status=SUCCESS
    Client->>APIGateway: 1.3. POST /appointment/reschedule
    APIGateway->>AppointmentService: 1.4. POST /appointment/reschedule
    activate WorkflowHandler
    AppointmentService->>WorkflowHandler: 1.5. RescheduleAppointmentWF.begin()
    WorkflowHandler->>WorkflowHandler: 1.6. step=BEGIN_RESCHEDULE_APPOINTMENT_WF
    end

    rect green
    note right of WorkflowHandler: step=UPDATE_APPOINTMENT
    WorkflowHandler->>WorkflowHandler: 2. step=UPDATE_APPOINTMENT

    activate AppointmentService
    AppointmentService->>CalendarService: 2.1. GET /calendar/availability
    AppointmentService->>AppointmentServiceDatabase: 2.2. update appointment record
    AppointmentService->>CalendarService: 2.3. PATCH /calendar/event
    CalendarService->>ExternalCalendarAPI: 2.4. update external calendar's event (optional)
    CalendarService->>NotificationService: 2.5. POST /notification/broadcast
    deactivate AppointmentService
    end

   rect green
   note right of WorkflowHandler: step=EMIT_EVENTS
    WorkflowHandler->>WorkflowHandler: 3. step=EMIT_EVENTS
    WorkflowHandler->>AnalyticsService: 3.1. emit AppointmentRescheduledEvent

    WorkflowHandler->>WorkflowHandler: 4. step=FINISH_RESCHEDULE_APPOINTMENT_WF
    deactivate WorkflowHandler
    
    Client->>APIGateway: 5. GET /workflow/status
    APIGateway-->>Client: 5.1. status=SUCCESS
    end
```

1. **step=BEGIN_RESCHEDULE_APPOINTMENT_WF** start workflow and pass necessary input parameters

2. **step=UPDATE_APPOINTMENT** validates availability of participants before updating an appointment and event in the calendar.

   2.1. `GET /calendar/availability?participantId=patientId&participantId=doctorId&startTime=timestamp1&endTime=timestamp2` -- check availability of doctor and patient, fail if there's an issue.

   2.2. Update a row in the database if validation passed.

   2.3. Update an event in the internal calendar with given parameters.

   2.4. optional step -- sends request to external calendar (Google Calendar/Outlook) to update an event.

   2.5. `POST /notification/broadcast?recieverId=patientId&recieverId=doctorId&notification=APPOINTMENT_RESCHEDULED` broadcast a notification to patient and doctor.

3. **step=EMIT_EVENTS** notify the analytics service about an updated appointment (might execute ETL in the background).

**Requirements:**

- Entire use case is handled in the background.
- Steps 1-2 must have transactional behavior.
- Graceful shutdown should be implemented.
- We should have an endpoint to get process status.
- Proper logging and metrics must be implemented.

### Failure scenario

Suppose participant's time slot has been reserved after client made validation.

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant AppointmentService
    participant WorkflowHandler
    participant CalendarService

   Client->>APIGateway: 1.1. POST /calendar/availability
   APIGateway->>CalendarService: 1.2. GET /calendar/availability (success)
   CalendarService-->>APIGateway: 1.2.1 status=SUCCESS
   APIGateway-->>Client: 1.2.2 status=SUCCESS
   Client->>APIGateway: 1.3. POST /appointment/reschedule
   APIGateway->>AppointmentService: 1.4. POST /appointment/reschedule
   activate WorkflowHandler
   AppointmentService->>WorkflowHandler: 1.5. RescheduleAppointmentWF.begin()
   WorkflowHandler->>WorkflowHandler: 1.6. step=BEGIN_RESCHEDULE_APPOINTMENT_WF

    WorkflowHandler->>WorkflowHandler: 2. step=UPDATE_APPOINTMENT

    activate AppointmentService
    AppointmentService->>CalendarService: 2.1. GET /calendar/availability
    deactivate AppointmentService

    WorkflowHandler->>WorkflowHandler: 3. step=FAIL_RESCHEDULE_APPOINTMENT_WF

   Client->>APIGateway: 4. GET /workflow/status
   APIGateway-->>Client: 4.1. status=FAILED, reason=""

    deactivate WorkflowHandler
```

**Requirements:**

- Appointment should not be created.
- Transaction should be aborted.
- User should be notified about the issue.

---

## Cancel appointment

### Happy scenario

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant AppointmentService
    participant WorkflowHandler
    participant CalendarService
    participant NotificationService
    participant AnalyticsService
    participant ExternalCalendarAPI
    participant AppointmentServiceDatabase

    rect green
    note right of Client: step=BEGIN_CANCEL_APPOINTMENT_WF
    Client->>APIGateway: 1.1. POST /appointment/cancel
    APIGateway->>AppointmentService: 1.2. POST /appointment/cancel
    activate WorkflowHandler
    AppointmentService->>WorkflowHandler: 1.3. CancelAppointmentWF.begin()
    WorkflowHandler->>WorkflowHandler: 1.4. step=BEGIN_CANCEL_APPOINTMENT_WF
    end

    rect green
    note right of WorkflowHandler: step=CANCEL_APPOINTMENT
    WorkflowHandler->>WorkflowHandler: 2. step=CANCEL_APPOINTMENT

    activate AppointmentService
    AppointmentService->>AppointmentServiceDatabase: 2.1. update appointment record (soft-delete)
    AppointmentService->>CalendarService: 2.2. POST /calendar/event/cancel (soft-delete)
    CalendarService->>ExternalCalendarAPI: 2.3. update external calendar's event (optional)
    CalendarService->>NotificationService: 2.4. POST /notification/broadcast
    deactivate AppointmentService
    end

   rect green
   note right of WorkflowHandler: step=EMIT_EVENTS
    WorkflowHandler->>WorkflowHandler: 3. step=EMIT_EVENTS
    WorkflowHandler->>AnalyticsService: 3.1. emit AppointmentCanceledEvent

    WorkflowHandler->>WorkflowHandler: 4. step=FINISH_CANCEL_APPOINTMENT_WF
    deactivate WorkflowHandler
    
    Client->>APIGateway: 5. GET /workflow/status
    APIGateway-->>Client: 5.1. status=SUCCESS
    end
```

1. **step=BEGIN_CANCEL_APPOINTMENT_WF** start workflow and pass necessary input parameters

2. **step=CANCEL_APPOINTMENT** soft-deletes appointment + calendar event and updates external calendar's event
   2.1. Update a row in the database (is_deleted=false).

   2.3. Update an event in the internal calendar with given parameters (is_deleted=false).

   2.4. optional step -- sends request to external calendar (Google Calendar/Outlook) to cancel an event.

   2.5. `POST /notification/broadcast?recieverId=patientId&recieverId=doctorId&notification=APPOINTMENT_CANCELED` broadcast a notification to patient and doctor.

3. **step=EMIT_EVENTS** notify the analytics service about a cancelled appointment (might execute ETL in the background).

**Requirements:**

- Entire use case is handled in the background.
- Steps 1-2 must have transactional behavior.
- Graceful shutdown should be implemented.
- We should have an endpoint to get process status.
- Proper logging and metrics must be implemented.

