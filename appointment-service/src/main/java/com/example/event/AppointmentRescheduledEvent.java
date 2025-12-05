package com.example.event;

import java.time.LocalDateTime;

/**
 * Event emitted when an appointment time is changed
 */
public class AppointmentRescheduledEvent extends AppointmentEvent {

    public LocalDateTime oldStartTime;
    public LocalDateTime oldEndTime;
    public LocalDateTime newStartTime;
    public LocalDateTime newEndTime;
    public LocalDateTime rescheduledAt;

    public AppointmentRescheduledEvent() {
        super();
    }

    public AppointmentRescheduledEvent(Long appointmentId, Long patientId, Long doctorId,
                                       LocalDateTime oldStartTime, LocalDateTime oldEndTime,
                                       LocalDateTime newStartTime, LocalDateTime newEndTime,
                                       LocalDateTime rescheduledAt) {
        super("APPOINTMENT_RESCHEDULED", appointmentId, patientId, doctorId);
        this.oldStartTime = oldStartTime;
        this.oldEndTime = oldEndTime;
        this.newStartTime = newStartTime;
        this.newEndTime = newEndTime;
        this.rescheduledAt = rescheduledAt;
    }
}



