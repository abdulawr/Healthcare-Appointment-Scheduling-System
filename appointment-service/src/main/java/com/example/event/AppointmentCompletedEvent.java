package com.example.event;

import java.time.LocalDateTime;

/**
 * Event emitted when an appointment is marked as completed
 */
public class AppointmentCompletedEvent extends AppointmentEvent {

    public LocalDateTime completedAt;
    public LocalDateTime scheduledTime;
    public Integer durationMinutes;

    public AppointmentCompletedEvent() {
        super();
    }

    public AppointmentCompletedEvent(Long appointmentId, Long patientId, Long doctorId,
                                     LocalDateTime completedAt, LocalDateTime scheduledTime,
                                     Integer durationMinutes) {
        super("APPOINTMENT_COMPLETED", appointmentId, patientId, doctorId);
        this.completedAt = completedAt;
        this.scheduledTime = scheduledTime;
        this.durationMinutes = durationMinutes;
    }
}


