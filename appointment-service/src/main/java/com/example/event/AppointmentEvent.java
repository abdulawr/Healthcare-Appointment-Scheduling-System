package com.example.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all appointment-related events.
 * Uses Jackson polymorphic deserialization for event type handling.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "eventType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AppointmentCreatedEvent.class, name = "APPOINTMENT_CREATED"),
        @JsonSubTypes.Type(value = AppointmentConfirmedEvent.class, name = "APPOINTMENT_CONFIRMED"),
        @JsonSubTypes.Type(value = AppointmentCancelledEvent.class, name = "APPOINTMENT_CANCELLED"),
        @JsonSubTypes.Type(value = AppointmentRescheduledEvent.class, name = "APPOINTMENT_RESCHEDULED"),
        @JsonSubTypes.Type(value = AppointmentCompletedEvent.class, name = "APPOINTMENT_COMPLETED")
})
public abstract class AppointmentEvent {

    public String eventId;
    public String eventType;
    public LocalDateTime timestamp;
    public Long appointmentId;
    public Long patientId;
    public Long doctorId;

    public AppointmentEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    public AppointmentEvent(String eventType, Long appointmentId, Long patientId, Long doctorId) {
        this();
        this.eventType = eventType;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
    }
}



