package com.example.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

/**
 * Producer for publishing appointment events to Kafka
 */
@ApplicationScoped
public class AppointmentEventProducer {

    private static final Logger LOG = Logger.getLogger(AppointmentEventProducer.class);

    @Channel("appointment-events")
    Emitter<Record<String, String>> eventEmitter;

    private final ObjectMapper objectMapper;

    public AppointmentEventProducer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Publish an appointment event to Kafka
     * @param event The event to publish
     */
    public void publishEvent(AppointmentEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = event.appointmentId.toString();

            LOG.infof("Publishing event: %s for appointment: %s", event.eventType, event.appointmentId);

            eventEmitter.send(Record.of(key, eventJson));

            LOG.infof("Successfully published event: %s", event.eventId);
        } catch (JsonProcessingException e) {
            LOG.errorf("Failed to serialize event: %s", e.getMessage());
            throw new RuntimeException("Failed to publish event", e);
        } catch (Exception e) {
            LOG.errorf("Failed to publish event to Kafka: %s", e.getMessage());
            // Don't throw - we don't want to fail the main operation if event publishing fails
            // In production, you might want to implement retry logic or dead letter queue
        }
    }

    /**
     * Publish appointment created event
     */
    public void publishCreatedEvent(AppointmentCreatedEvent event) {
        publishEvent(event);
    }

    /**
     * Publish appointment confirmed event
     */
    public void publishConfirmedEvent(AppointmentConfirmedEvent event) {
        publishEvent(event);
    }

    /**
     * Publish appointment cancelled event
     */
    public void publishCancelledEvent(AppointmentCancelledEvent event) {
        publishEvent(event);
    }

    /**
     * Publish appointment rescheduled event
     */
    public void publishRescheduledEvent(AppointmentRescheduledEvent event) {
        publishEvent(event);
    }

    /**
     * Publish appointment completed event
     */
    public void publishCompletedEvent(AppointmentCompletedEvent event) {
        publishEvent(event);
    }
}


