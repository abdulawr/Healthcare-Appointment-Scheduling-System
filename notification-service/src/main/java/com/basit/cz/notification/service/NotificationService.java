package com.basit.cz.notification.service;

import com.basit.cz.notification.api.CreateNotificationRequest;
import com.basit.cz.notification.api.NotificationMapper;
import com.basit.cz.notification.api.NotificationResponse;
import com.basit.cz.notification.model.NotificationChannel;
import com.basit.cz.notification.model.NotificationEntity;
import com.basit.cz.notification.model.NotificationStatus;
import com.basit.cz.notification.novu.NovuClient;
import com.basit.cz.notification.novu.NovuTriggerRequest;
import com.basit.cz.notification.novu.NovuTriggerResponse;
import com.github.f4b6a3.uuid.UuidCreator;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class NotificationService {

    @Inject
    @RestClient
    NovuClient novuClient;

    @ConfigProperty(name = "novu.api-key")
    String novuApiKey;

    @ConfigProperty(name = "novu.workflow.order-shipped")
    String orderShippedWorkflow; // example mapping, you can generalize

    @Inject
    JsonUtil jsonUtil;

    @Transactional
    public NotificationResponse createAndSend(CreateNotificationRequest req) {
        // small wrapper to keep transactional logic separate
        NotificationEntity entity = createNotificationRecord(req);
        sendToNovuAndUpdate(entity, req);
        return NotificationMapper.toResponse(entity);
    }

    protected NotificationEntity createNotificationRecord(CreateNotificationRequest req) {
        NotificationEntity existing = null;
        if (req.idempotencyKey != null && !req.idempotencyKey.isBlank()) {
            existing = NotificationEntity.find("idempotencyKey", req.idempotencyKey)
                    .firstResult();
        }
        if (existing != null) {
            Log.infof("Idempotent hit for key %s, returning existing notification %s",
                    req.idempotencyKey, existing.id);
            return existing;
        }

        NotificationEntity e = new NotificationEntity();
        e.id = UuidCreator.getTimeOrderedEpoch(); // UUIDv7
        e.userId = req.userId;
        e.eventType = req.eventType;
        e.locale = req.locale;
        e.brand = req.brand;
        List<NotificationChannel> channels = (req.channels == null || req.channels.isEmpty())
                ? List.of(NotificationChannel.EMAIL) // default
                : req.channels;
        e.channels = NotificationMapper.channelsToString(channels);
        e.status = NotificationStatus.PENDING;
        e.idempotencyKey = req.idempotencyKey;
        e.payloadJson = jsonUtil.toJson(req.payload);
        e.createdAt = OffsetDateTime.now();
        e.updatedAt = e.createdAt;
        e.persist();
        return e;
    }

    protected void sendToNovuAndUpdate(NotificationEntity entity, CreateNotificationRequest req) {
        try {
            String workflowName = resolveWorkflowName(entity.eventType, entity.brand);
            String transactionId = entity.id.toString(); // internal ID as Novu transactionId

            NovuTriggerRequest novuReq = new NovuTriggerRequest();
            novuReq.name = workflowName;
            novuReq.transactionId = transactionId;

            NovuTriggerRequest.To to = new NovuTriggerRequest.To();
            to.subscriberId = entity.userId;
            novuReq.to = List.of(to);

            // payload enrichment
            novuReq.payload = Map.of(
                    "eventType", entity.eventType,
                    "userId", entity.userId,
                    "locale", entity.locale,
                    "brand", entity.brand,
                    "data", req.payload
            );
            Log.debugf("Calling Novu with API key prefix: %s...", novuApiKey != null ? novuApiKey.substring(0, 6) : "null");
            NovuTriggerResponse resp =
                    novuClient.triggerEvent("ApiKey " + novuApiKey, novuReq);

            entity.novuTransactionId = resp.transactionId != null ? resp.transactionId : transactionId;
            entity.status = Boolean.TRUE.equals(resp.acknowledged)
                    ? NotificationStatus.SENT
                    : NotificationStatus.FAILED;
            entity.updatedAt = OffsetDateTime.now();

            Log.infof("Novu response: acknowledged=%s; status=%s; tx=%s; error=%s",
                    resp.acknowledged, resp.status, resp.transactionId, resp.error);

        } catch (ClientWebApplicationException ex) {
            String body = "";
            try {
                body = ex.getResponse() != null
                        ? ex.getResponse().readEntity(String.class)
                        : "<no body>";
            } catch (Exception ignore) {}

            Log.errorf("Novu returned error: status=%d, body=%s",
                    ex.getResponse() != null ? ex.getResponse().getStatus() : -1,
                    body);

            entity.status = NotificationStatus.FAILED;
            entity.updatedAt = OffsetDateTime.now();

        } catch (Exception ex) {
            Log.error("Failed to send notification to Novu", ex);
            entity.status = NotificationStatus.FAILED;
            entity.updatedAt = OffsetDateTime.now();
        }
    }

    private String resolveWorkflowName(String eventType, String brand) {
        // Very simple mapping placeholder. You’ll probably want a DB table for this.
        if ("order.shipped".equals(eventType)) {
            return orderShippedWorkflow;
        }
        return eventType.replace('.', '-'); // default heuristic
    }

    public NotificationResponse getById(UUID id) {
        NotificationEntity e = NotificationEntity.findById(id);
        if (e == null) return null;
        return NotificationMapper.toResponse(e);
    }

    public List<NotificationResponse> getByUserId(String userId, int limit) {
        return NotificationEntity.<NotificationEntity>find("userId = ?1 order by createdAt desc", userId)
                .page(Page.of(0, limit))
                .list()
                .stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }
}
