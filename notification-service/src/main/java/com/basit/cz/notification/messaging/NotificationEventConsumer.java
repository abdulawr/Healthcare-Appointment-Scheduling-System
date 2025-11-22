package com.basit.cz.notification.messaging;

import com.basit.cz.notification.api.CreateNotificationRequest;
import com.basit.cz.notification.service.NotificationService;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.quarkus.logging.Log;

@ApplicationScoped
public class NotificationEventConsumer {

    @Inject
    NotificationService notificationService;

    @Incoming("notification-events")
    @Blocking // run in worker thread because we touch DB and external HTTP
    public void consume(NotificationEvent event) {
        try {
            CreateNotificationRequest req = new CreateNotificationRequest();
            req.idempotencyKey = event.idempotencyKey;
            req.userId = event.userId;
            req.eventType = event.eventType;
            req.locale = event.locale;
            req.brand = event.brand;
            req.channels = event.channels;
            req.payload = event.payload;

            notificationService.createAndSend(req);
        } catch (Exception ex) {
            Log.error("Failed to process notification event from Kafka", ex);
            // For more robust handling you'd emit to a DLQ topic here
        }
    }
}
