package com.example.event;

import java.time.LocalDateTime;

/**
 * Event emitted when an appointment is cancelled
 */
public class AppointmentCancelledEvent extends AppointmentEvent {

    public LocalDateTime cancelledAt;
    public String cancellationReason;
    public LocalDateTime originalScheduledTime;

    public AppointmentCancelledEvent() {
        super();
    }

    public AppointmentCancelledEvent(Long appointmentId, Long patientId, Long doctorId,
                                     LocalDateTime cancelledAt, String cancellationReason,
                                     LocalDateTime originalScheduledTime) {
        super("APPOINTMENT_CANCELLED", appointmentId, patientId, doctorId);
        this.cancelledAt = cancelledAt;
        this.cancellationReason = cancellationReason;
        this.originalScheduledTime = originalScheduledTime;
    }
}