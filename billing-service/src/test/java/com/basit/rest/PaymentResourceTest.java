package com.basit.rest;

import com.basit.constant.*;
import com.basit.dto.request.ProcessPaymentRequest;
import com.basit.entity.Invoice;
import com.basit.entity.InvoiceItem;
import com.basit.repository.InvoiceRepository;
import com.basit.repository.PaymentRepository;
import com.basit.service.InvoiceService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentResourceTest {

    @Inject
    PaymentRepository paymentRepository;

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    InvoiceService invoiceService;

    private static Long testInvoiceId;
    private static Long testPaymentId;

    @BeforeEach
    @Transactional
    void setUp() {
        paymentRepository.deleteAll();
        invoiceRepository.deleteAll();
        testInvoiceId = createTestInvoice();
    }

    @Test
    @Order(1)
    void testProcessPayment() {
        ProcessPaymentRequest request = createPaymentRequest(testInvoiceId);

        testPaymentId = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/payments")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("status", equalTo("COMPLETED"))
                .body("transactionId", notNullValue())
                .body("amount", equalTo(100.0f))
                .extract()
                .path("id");
    }

    @Test
    @Order(2)
    void testProcessPayment_InvalidRequest() {
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        // Missing required fields

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/payments")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(3)
    void testProcessPayment_WithIdempotencyKey() {
        ProcessPaymentRequest request = createPaymentRequest(testInvoiceId);
        request.idempotencyKey = "UNIQUE-KEY-789";

        // First request
        Long paymentId1 = given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/billing/payments")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Second request with same key
        Long paymentId2 = given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/billing/payments")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Should return same payment
        assert paymentId1.equals(paymentId2);
    }

    @Test
    @Order(4)
    void testGetPaymentById() {
        ProcessPaymentRequest request = createPaymentRequest(testInvoiceId);
        Long paymentId = given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/billing/payments")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .when()
                .get("/api/billing/payments/" + paymentId)
                .then()
                .statusCode(200)
                .body("id", equalTo(paymentId.intValue()))
                .body("invoiceId", equalTo(testInvoiceId.intValue()));
    }

    @Test
    @Order(5)
    void testGetPaymentById_NotFound() {
        given()
                .when()
                .get("/api/billing/payments/99999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    void testGetPaymentsByInvoice() {
        // Create multiple payments for same invoice
        ProcessPaymentRequest request1 = createPaymentRequest(testInvoiceId);
        request1.amount = new BigDecimal("50.00");
        given().contentType(ContentType.JSON).body(request1).post("/api/billing/payments");

        ProcessPaymentRequest request2 = createPaymentRequest(testInvoiceId);
        request2.amount = new BigDecimal("50.00");
        given().contentType(ContentType.JSON).body(request2).post("/api/billing/payments");

        // Get payments for invoice
        given()
                .when()
                .get("/api/billing/payments/invoice/" + testInvoiceId)
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @Order(7)
    void testGetPaymentsByPatient() {
        Long patientId = 1L;
        ProcessPaymentRequest request = createPaymentRequest(testInvoiceId);
        request.patientId = patientId;

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/billing/payments");

        given()
                .when()
                .get("/api/billing/payments/patient/" + patientId)
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(8)
    void testGetSuccessfulPayments() {
        ProcessPaymentRequest request = createPaymentRequest(testInvoiceId);
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/billing/payments");

        given()
                .when()
                .get("/api/billing/payments/successful")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(9)
    void testGetPendingPayments() {
        given()
                .when()
                .get("/api/billing/payments/pending")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(10)
    void testGetFailedPayments() {
        given()
                .when()
                .get("/api/billing/payments/failed")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(11)
    void testCanRefund() {
        ProcessPaymentRequest request = createPaymentRequest(testInvoiceId);
        Long paymentId = given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/billing/payments")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .when()
                .get("/api/billing/payments/" + paymentId + "/refundable")
                .then()
                .statusCode(200)
                .body("canRefund", equalTo(true));
    }

    // Helper methods
    private ProcessPaymentRequest createPaymentRequest(Long invoiceId) {
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.invoiceId = invoiceId;
        request.patientId = 1L;
        request.amount = new BigDecimal("100.00");
        request.paymentMethod = PaymentMethodType.CREDIT_CARD;
        request.paymentGateway = "stripe";
        return request;
    }

    // Helper method - package-private, called from @Transactional setUp method
    Long createTestInvoice() {
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 1L;
        invoice.invoiceNumber = "INV-" + System.currentTimeMillis();
        invoice.issueDate = LocalDate.now();
        invoice.dueDate = LocalDate.now().plusDays(30);
        invoice.taxAmount = new BigDecimal("10.00");
        invoice.discountAmount = BigDecimal.ZERO;
        invoice.status = InvoiceStatus.ISSUED;

        InvoiceItem item = new InvoiceItem();
        item.description = "Consultation";
        item.quantity = 1;
        item.unitPrice = new BigDecimal("100.00");
        invoice.addItem(item);

        Invoice created = invoiceService.createInvoice(invoice);
        return created.id;
    }
}