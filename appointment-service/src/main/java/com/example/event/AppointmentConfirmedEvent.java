package com.example.event;

import java.time.LocalDateTime;

/**
 * Event emitted when an appointment is confirmed by the patient
 */
public class AppointmentConfirmedEvent extends AppointmentEvent {

    public LocalDateTime confirmedAt;
    public LocalDateTime scheduledTime;

    public AppointmentConfirmedEvent() {
        super();
    }

    public AppointmentConfirmedEvent(Long appointmentId, Long patientId, Long doctorId,
                                     LocalDateTime confirmedAt, LocalDateTime scheduledTime) {
        super("APPOINTMENT_CONFIRMED", appointmentId, patientId, doctorId);
        this.confirmedAt = confirmedAt;
        this.scheduledTime = scheduledTime;
    }
}