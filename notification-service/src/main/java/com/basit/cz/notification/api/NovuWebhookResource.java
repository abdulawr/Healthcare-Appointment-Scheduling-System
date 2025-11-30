package com.basit.cz.notification.api;

import com.basit.cz.notification.model.NotificationEntity;
import com.basit.cz.notification.model.NotificationStatus;
import com.basit.cz.notification.novu.NovuWebhookEvent;
import io.quarkus.logging.Log;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.OffsetDateTime;

@Path("/notifications/callbacks/novu")
@Consumes(MediaType.APPLICATION_JSON)
public class NovuWebhookResource {

    @POST
    @Transactional
    public Response handle(NovuWebhookEvent event) {
        if (event.transactionId == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        NotificationEntity entity = NotificationEntity
                .find("novuTransactionId", event.transactionId)
                .firstResult();

        if (entity == null) {
            Log.warnf("Received Novu webhook for unknown transactionId %s", event.transactionId);
            return Response.ok().build();
        }

        if ("delivered".equalsIgnoreCase(event.status)) {
            entity.status = NotificationStatus.DELIVERED;
        } else if ("failed".equalsIgnoreCase(event.status) || "bounced".equalsIgnoreCase(event.status)) {
            entity.status = NotificationStatus.FAILED;
        }
        entity.updatedAt = OffsetDateTime.now();
        entity.persist();

        return Response.ok().build();
    }
}
