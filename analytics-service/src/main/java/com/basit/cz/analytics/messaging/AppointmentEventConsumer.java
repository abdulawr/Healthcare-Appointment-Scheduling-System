package com.basit.cz.analytics.messaging;

import com.basit.cz.analytics.entity.FactAppointment;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.time.OffsetDateTime;

@ApplicationScoped
public class AppointmentEventConsumer {

    @Inject
    ObjectMapper objectMapper;

    @Incoming("appointment-events")
    @Blocking
    @Transactional
    public void consume(String payload) {
        try {
            AppointmentEvent event = objectMapper.readValue(payload, AppointmentEvent.class);

            FactAppointment fact = new FactAppointment();
            fact.appointmentId = event.appointmentId;
            fact.doctorId = event.doctorId;
            fact.patientId = event.patientId;
            fact.status = event.status;
            fact.startTime = event.startTime;
            fact.endTime = event.endTime;
            fact.bookingTime = event.bookingTime;
            fact.cancellationTime = event.cancellationTime;
            fact.cancellationReason = event.cancellationReason;
            fact.priceCents = event.priceCents;
            fact.sourceService = "appointment-service";
            fact.eventType = event.eventType;
            fact.eventTime = event.eventTime != null ? event.eventTime : OffsetDateTime.now();
            fact.createdAt = OffsetDateTime.now();

            fact.persist();
        } catch (Exception e) {
            // TODO: add proper logging/metrics
            e.printStackTrace();
        }
    }
}
