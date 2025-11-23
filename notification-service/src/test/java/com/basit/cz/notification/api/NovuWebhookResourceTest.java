package com.basit.cz.notification.api;

import com.basit.cz.notification.model.NotificationEntity;
import com.basit.cz.notification.model.NotificationStatus;
import com.basit.cz.notification.novu.NovuClient;
import com.basit.cz.notification.novu.NovuTriggerRequest;
import com.basit.cz.notification.novu.NovuTriggerResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
public class NovuWebhookResourceTest {

    @InjectMock
    @RestClient
    NovuClient novuClient;

    @Test
    void handleWebhook_happyPath_movesToDelivered() {
        // arrange: stub Novu to acknowledge and return a transactionId
        String novuTxId = "novu-tx-123";

        NovuTriggerResponse novuResp = new NovuTriggerResponse();
        novuResp.acknowledged = true;
        novuResp.status = "processed";
        novuResp.transactionId = novuTxId;

        when(novuClient.triggerEvent(startsWith("ApiKey "), any(NovuTriggerRequest.class)))
                .thenReturn(novuResp);

        String notificationBody = """
                {
                  "idempotencyKey": "order-124-user-42-email",
                  "userId": "user-42",
                  "eventType": "order.shipped",
                  "locale": "en-US",
                  "brand": "default",
                  "channels": ["EMAIL"],
                  "payload": {
                    "orderId": "123",
                    "trackingUrl": "https://example.com/track/123"
                  }
                }
                """;

        // act 1: create notification
        String id = given()
                .contentType(ContentType.JSON)
                .body(notificationBody)
                .when()
                .post("/notifications")
                .then()
                .statusCode(202)
                .extract()
                .jsonPath().getString("id");

        // act 2: send webhook with the SAME transactionId that we stored as novuTransactionId
        String webhookBody = """
            {
              "transactionId": "%s",
              "status": "delivered",
              "raw": {}
            }
            """.formatted(novuTxId);

        given()
                .contentType(ContentType.JSON)
                .body(webhookBody)
                .when()
                .post("/webhooks/novu")
                .then()
                .statusCode(200);

        // assert
        NotificationEntity entity = findById(id);
        assertThat(entity).isNotNull();
        assertThat(entity.status).isEqualTo(NotificationStatus.DELIVERED);
        assertThat(entity.novuTransactionId).isEqualTo(novuTxId);

        verify(novuClient, times(1))
                .triggerEvent(any(), any(NovuTriggerRequest.class));
    }

    @Transactional
    NotificationEntity findById(String id) {
        return NotificationEntity.findById(java.util.UUID.fromString(id));
    }
}
