package com.basit.cz.notification.api;

import com.basit.cz.notification.model.NotificationEntity;
import com.basit.cz.notification.model.NotificationStatus;
import com.basit.cz.notification.novu.NovuClient;
import com.basit.cz.notification.novu.NovuTriggerRequest;
import com.basit.cz.notification.novu.NovuTriggerResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

@QuarkusTest
class NotificationResourceTest {

    @InjectMock
    @RestClient
    NovuClient novuClient;

    @Test
    void createNotification_happyPath_persistsAndMarksSent() {
        // arrange: stub Novu to acknowledge
        NovuTriggerResponse novuResp = new NovuTriggerResponse();
        novuResp.acknowledged = true;
        novuResp.status = "processed";
        novuResp.transactionId = "novu-tx-123";

        when(novuClient.triggerEvent(startsWith("ApiKey "), any(NovuTriggerRequest.class)))
                .thenReturn(novuResp);

        String body = """
                {
                  "idempotencyKey": "order-123-user-42-email",
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

        // act: call REST endpoint
        String id = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/notifications")
                .then()
                .statusCode(202)
                .extract()
                .jsonPath().getString("id");

        // assert: DB contains a SENT notification with that id
        NotificationEntity entity = findById(id);
        assertThat(entity).isNotNull();
        assertThat(entity.status).isEqualTo(NotificationStatus.SENT);
        assertThat(entity.novuTransactionId).isEqualTo("novu-tx-123");

        // verify: Novu was called exactly once
        verify(novuClient, times(1))
                .triggerEvent(any(), any(NovuTriggerRequest.class));
    }

    @Transactional
    NotificationEntity findById(String id) {
        return NotificationEntity.findById(java.util.UUID.fromString(id));
    }
}
