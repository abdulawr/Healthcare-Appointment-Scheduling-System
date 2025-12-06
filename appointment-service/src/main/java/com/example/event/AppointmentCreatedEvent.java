package com.example.event;


import com.example.constant.AppointmentType;

import java.time.LocalDateTime;

/**
 * Event emitted when a new appointment is created
 */
public class AppointmentCreatedEvent extends AppointmentEvent {

    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public AppointmentType type;
    public String reason;

    public AppointmentCreatedEvent() {
        super();
    }

    public AppointmentCreatedEvent(Long appointmentId, Long patientId, Long doctorId,
                                   LocalDateTime startTime, LocalDateTime endTime,
                                   AppointmentType type, String reason) {
        super("APPOINTMENT_CREATED", appointmentId, patientId, doctorId);
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.reason = reason;
    }
}
