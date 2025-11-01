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

    rect gray
    note right of Client: step=BEGIN_SCHEDULE_APPOINTMENT_WF
    Client->>APIGateway: 1. POST /appointment/schedule
    APIGateway->>AppointmentService: 1.1. POST /appointment/schedule
    activate WorkflowHandler
    AppointmentService->>WorkflowHandler: 1.2. ScheduleAppointmentWF.begin()
    WorkflowHandler->>WorkflowHandler: 1.3. step=BEGIN_SCHEDULE_APPOINTMENT_WF
    end

    rect gray
    note right of WorkflowHandler: step=CREATE_APPOINTMENT
    WorkflowHandler->>WorkflowHandler: 2. step=CREATE_APPOINTMENT

    activate AppointmentService
    AppointmentService->>CalendarService: 2.1. GET /calendar/availability
    AppointmentService->>AppointmentServiceDatabase: 2.2. insert appointment record
    AppointmentService->>CalendarService: 2.3. POST /calendar/event
    CalendarService->>ExternalCalendarAPI: 2.4. create an event in external calendar (optional)
    CalendarService->>NotificationService: 2.5. POST /notification/broadcast
    deactivate AppointmentService
    end

   rect gray
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

Suppose patient/doctor doesn't have available slot.

```mermaid
sequenceDiagram
    participant Client
    participant APIGateway
    participant AppointmentService
    participant WorkflowHandler
    participant CalendarService

    Client->>APIGateway: 1. POST /appointment/schedule
    APIGateway->>AppointmentService: 1.1. POST /appointment/schedule
    activate WorkflowHandler
    AppointmentService->>WorkflowHandler: 1.2. ScheduleAppointmentWF.begin()
    WorkflowHandler->>WorkflowHandler: 1.3. step=BEGIN_SCHEDULE_APPOINTMENT_WF

    WorkflowHandler->>WorkflowHandler: 2. step=CREATE_APPOINTMENT

    activate AppointmentService
    AppointmentService->>CalendarService: 2.1. GET /calendar/availability
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
