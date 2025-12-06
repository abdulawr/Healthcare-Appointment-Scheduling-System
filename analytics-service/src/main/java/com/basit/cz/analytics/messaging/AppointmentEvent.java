package com.basit.cz.analytics.messaging;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AppointmentEvent {

    public UUID appointmentId;
    public UUID doctorId;
    public UUID patientId;
    public String status;
    public OffsetDateTime startTime;
    public OffsetDateTime endTime;
    public OffsetDateTime bookingTime;
    public OffsetDateTime cancellationTime;
    public String cancellationReason;
    public Long priceCents;
    public String eventType;
    public OffsetDateTime eventTime;
}
