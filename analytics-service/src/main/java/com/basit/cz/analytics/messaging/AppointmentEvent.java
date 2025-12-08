package com.basit.cz.analytics.messaging;

import java.time.OffsetDateTime;

public class AppointmentEvent {

    public Long appointmentId;
    public Long doctorId;
    public Long patientId;
    public String status;
    public OffsetDateTime startTime;
    public OffsetDateTime endTime;
    public OffsetDateTime bookingTime;
    public OffsetDateTime cancellationTime;
    public String cancellationReason;
    public Long priceCents;
    public String eventType;
    public OffsetDateTime eventTime;
    public String sourceService = "appointment-service";
}
