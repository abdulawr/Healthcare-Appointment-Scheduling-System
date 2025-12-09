package com.basit.rest;

import com.basit.dto.request.PaymentMethodRequest;
import com.basit.constant.PaymentMethodType;
import com.basit.repository.PaymentMethodRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class PaymentMethodResourceTest {

    @Inject
    PaymentMethodRepository paymentMethodRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        paymentMethodRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testSavePaymentMethod() {
        PaymentMethodRequest request = new PaymentMethodRequest();
        request.patientId = 1L;
        request.type = PaymentMethodType.CREDIT_CARD;
        request.cardholderName = "John Doe";
        request.lastFourDigits = "4242";
        request.cardBrand = "VISA";
        request.expiryMonth = 12;
        request.expiryYear = 2025;
        request.token = "tok_visa_4242";
        request.isDefault = true;

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/payment-methods")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("patientId", equalTo(1))
                .body("type", equalTo("CREDIT_CARD"))
                .body("cardholderName", equalTo("John Doe"))
                .body("lastFourDigits", equalTo("4242"))
                .body("cardBrand", equalTo("VISA"))
                .body("expiryMonth", equalTo(12))
                .body("expiryYear", equalTo(2025))
                .body("isDefault", equalTo(true))
                .body("isActive", equalTo(true))
                .body("createdAt", notNullValue());
    }

    @Test
    @Transactional
    public void testSavePaymentMethodInvalidRequest() {
        PaymentMethodRequest request = new PaymentMethodRequest();
        // Missing required fields

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/payment-methods")
                .then()
                .statusCode(400);
    }

    @Test
    @Transactional
    public void testUpdatePaymentMethod() {
        Long pmId = createPaymentMethod(1L, false);

        PaymentMethodRequest updateRequest = new PaymentMethodRequest();
        updateRequest.patientId = 1L;
        updateRequest.type = PaymentMethodType.CREDIT_CARD;
        updateRequest.cardholderName = "Jane Doe"; // Updated
        updateRequest.lastFourDigits = "4242";
        updateRequest.cardBrand = "MASTERCARD"; // Updated
        updateRequest.expiryMonth = 6;
        updateRequest.expiryYear = 2026;
        updateRequest.isDefault = true;

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", pmId)
                .body(updateRequest)
                .when()
                .put("/api/billing/payment-methods/{id}")
                .then()
                .statusCode(200)
                .body("cardholderName", equalTo("Jane Doe"))
                .body("cardBrand", equalTo("MASTERCARD"))
                .body("isDefault", equalTo(true))
                .body("updatedAt", notNullValue());
    }

    @Test
    public void testUpdatePaymentMethodNotFound() {
        PaymentMethodRequest request = new PaymentMethodRequest();
        request.patientId = 1L;
        request.type = PaymentMethodType.CREDIT_CARD;
        request.cardholderName = "Test";
        request.lastFourDigits = "4242";
        request.cardBrand = "VISA";
        request.expiryMonth = 12;
        request.expiryYear = 2025;

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", 99999)
                .body(request)
                .when()
                .put("/api/billing/payment-methods/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    @Transactional
    public void testDeletePaymentMethod() {
        Long pmId = createPaymentMethod(1L, false);

        given()
                .pathParam("id", pmId)
                .when()
                .delete("/api/billing/payment-methods/{id}")
                .then()
                .statusCode(204);

        // Verify it's soft deleted (inactive)
        given()
                .pathParam("id", pmId)
                .when()
                .get("/api/billing/payment-methods/{id}")
                .then()
                .statusCode(200)
                .body("isActive", equalTo(false));
    }

    @Test
    public void testDeletePaymentMethodNotFound() {
        given()
                .pathParam("id", 99999)
                .when()
                .delete("/api/billing/payment-methods/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    @Transactional
    public void testSetAsDefault() {
        Long pm1 = createPaymentMethod(1L, true);
        Long pm2 = createPaymentMethod(1L, false);

        given()
                .pathParam("id", pm2)
                .queryParam("patientId", 1)
                .when()
                .post("/api/billing/payment-methods/{id}/set-default")
                .then()
                .statusCode(200)
                .body("isDefault", equalTo(true));

        // Verify first is no longer default
        given()
                .pathParam("id", pm1)
                .when()
                .get("/api/billing/payment-methods/{id}")
                .then()
                .statusCode(200)
                .body("isDefault", equalTo(false));
    }

    @Test
    @Transactional
    public void testSetAsDefaultWrongPatient() {
        Long pmId = createPaymentMethod(1L, false);

        given()
                .pathParam("id", pmId)
                .queryParam("patientId", 999)
                .when()
                .post("/api/billing/payment-methods/{id}/set-default")
                .then()
                .statusCode(400);
    }

    @Test
    @Transactional
    public void testGetPaymentMethod() {
        Long pmId = createPaymentMethod(1L, false);

        given()
                .pathParam("id", pmId)
                .when()
                .get("/api/billing/payment-methods/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(pmId.intValue()))
                .body("patientId", equalTo(1));
    }

    @Test
    public void testGetPaymentMethodNotFound() {
        given()
                .pathParam("id", 99999)
                .when()
                .get("/api/billing/payment-methods/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    @Transactional
    public void testGetPatientPaymentMethods() {
        createPaymentMethod(1L, true);
        createPaymentMethod(1L, false);
        createPaymentMethod(2L, false); // Different patient

        given()
                .pathParam("patientId", 1)
                .when()
                .get("/api/billing/payment-methods/patient/{patientId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("[0].patientId", equalTo(1))
                .body("[1].patientId", equalTo(1))
                .body("[0].isActive", equalTo(true))
                .body("[1].isActive", equalTo(true));
    }

    @Test
    @Transactional
    public void testGetPatientPaymentMethodsExcludesInactive() {
        Long pm1 = createPaymentMethod(1L, false);
        Long pm2 = createPaymentMethod(1L, false);

        // Delete first method
        given()
                .pathParam("id", pm1)
                .when()
                .delete("/api/billing/payment-methods/{id}")
                .then()
                .statusCode(204);

        given()
                .pathParam("patientId", 1)
                .when()
                .get("/api/billing/payment-methods/patient/{patientId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo(pm2.intValue()));
    }

    @Test
    @Transactional
    public void testGetDefaultPaymentMethod() {
        createPaymentMethod(1L, false);
        Long defaultPm = createPaymentMethod(1L, true);

        given()
                .pathParam("patientId", 1)
                .when()
                .get("/api/billing/payment-methods/patient/{patientId}/default")
                .then()
                .statusCode(200)
                .body("id", equalTo(defaultPm.intValue()))
                .body("isDefault", equalTo(true));
    }

    @Test
    public void testGetDefaultPaymentMethodNotFound() {
        given()
                .pathParam("patientId", 999)
                .when()
                .get("/api/billing/payment-methods/patient/{patientId}/default")
                .then()
                .statusCode(404);
    }

    @Test
    @Transactional
    public void testGetPaymentMethodByToken() {
        String token = "unique_token_" + System.currentTimeMillis();
        Long pmId = createPaymentMethodWithToken(1L, token);

        given()
                .pathParam("token", token)
                .when()
                .get("/api/billing/payment-methods/token/{token}")
                .then()
                .statusCode(200)
                .body("id", equalTo(pmId.intValue()));
    }

    @Test
    public void testGetPaymentMethodByTokenNotFound() {
        given()
                .pathParam("token", "nonexistent_token")
                .when()
                .get("/api/billing/payment-methods/token/{token}")
                .then()
                .statusCode(404);
    }

    @Test
    @Transactional
    public void testSaveMultiplePaymentMethodTypes() {
        // Credit card
        PaymentMethodRequest ccRequest = createRequest(1L, PaymentMethodType.CREDIT_CARD);
        ccRequest.lastFourDigits = "4242";

        // Debit card
        PaymentMethodRequest dcRequest = createRequest(1L, PaymentMethodType.DEBIT_CARD);
        dcRequest.lastFourDigits = "5555";

        // PayPal
        PaymentMethodRequest ppRequest = createRequest(1L, PaymentMethodType.PAYPAL);
        ppRequest.lastFourDigits = null; // PayPal doesn't have card digits

        given().contentType(ContentType.JSON).body(ccRequest)
                .post("/api/billing/payment-methods").then().statusCode(201);

        given().contentType(ContentType.JSON).body(dcRequest)
                .post("/api/billing/payment-methods").then().statusCode(201);

        given().contentType(ContentType.JSON).body(ppRequest)
                .post("/api/billing/payment-methods").then().statusCode(201);

        given()
                .pathParam("patientId", 1)
                .when()
                .get("/api/billing/payment-methods/patient/{patientId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(3));
    }

    private Long createPaymentMethod(Long patientId, Boolean isDefault) {
        PaymentMethodRequest request = createRequest(patientId, PaymentMethodType.CREDIT_CARD);
        request.isDefault = isDefault;

        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/payment-methods")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private Long createPaymentMethodWithToken(Long patientId, String token) {
        PaymentMethodRequest request = createRequest(patientId, PaymentMethodType.CREDIT_CARD);
        request.token = token;

        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/payment-methods")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private PaymentMethodRequest createRequest(Long patientId, PaymentMethodType type) {
        PaymentMethodRequest request = new PaymentMethodRequest();
        request.patientId = patientId;
        request.type = type;
        request.cardholderName = "Test User";
        request.lastFourDigits = "4242";
        request.cardBrand = "VISA";
        request.expiryMonth = 12;
        request.expiryYear = 2025;
        request.token = "tok_test_" + System.currentTimeMillis();
        request.isDefault = false;
        return request;
    }
}
