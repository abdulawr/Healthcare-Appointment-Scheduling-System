package com.basit.rest;

import com.basit.dto.request.RefundRequest;
import com.basit.entity.Invoice;
import com.basit.entity.Payment;
import com.basit.constant.PaymentMethodType;
import com.basit.constant.PaymentStatus;
import com.basit.repository.InvoiceRepository;
import com.basit.repository.PaymentRepository;
import com.basit.repository.RefundRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class RefundResourceTest {

    @Inject
    RefundRepository refundRepository;

    @Inject
    PaymentRepository paymentRepository;

    @Inject
    InvoiceRepository invoiceRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        refundRepository.deleteAll();
        paymentRepository.deleteAll();
        invoiceRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testProcessRefund() {
        // Create test data
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        Payment payment = createTestPayment(invoice.id);
        payment.status = PaymentStatus.COMPLETED;
        paymentRepository.persist(payment);

        // Create refund request
        RefundRequest request = new RefundRequest();
        request.paymentId = payment.id;
        request.amount = new BigDecimal("50.00");
        request.reason = "Customer request";

        // Process refund
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/refunds")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("paymentId", equalTo(payment.id.intValue()))
                .body("amount", equalTo(50.00f))
                .body("reason", equalTo("Customer request"))
                .body("refundTransactionId", notNullValue())
                .body("refundTransactionId", startsWith("REF-"))
                .body("status", equalTo("COMPLETED"));
    }

    @Test
    @Transactional
    public void testProcessRefundExceedsAmount() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        Payment payment = createTestPayment(invoice.id);
        payment.status = PaymentStatus.COMPLETED;
        payment.amount = new BigDecimal("100.00");
        paymentRepository.persist(payment);

        RefundRequest request = new RefundRequest();
        request.paymentId = payment.id;
        request.amount = new BigDecimal("150.00");
        request.reason = "Test";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/refunds")
                .then()
                .statusCode(400);
    }

    @Test
    @Transactional
    public void testProcessRefundPaymentNotFound() {
        RefundRequest request = new RefundRequest();
        request.paymentId = 99999L;
        request.amount = new BigDecimal("50.00");
        request.reason = "Test";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/refunds")
                .then()
                .statusCode(404);
    }

    @Test
    @Transactional
    public void testGetRefund() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        Payment payment = createTestPayment(invoice.id);
        payment.status = PaymentStatus.COMPLETED;
        paymentRepository.persist(payment);

        RefundRequest request = new RefundRequest();
        request.paymentId = payment.id;
        request.amount = new BigDecimal("50.00");
        request.reason = "Test";

        Long refundId = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/refunds")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .pathParam("id", refundId)
                .when()
                .get("/api/billing/refunds/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(refundId.intValue()))
                .body("amount", equalTo(50.00f));
    }

    @Test
    public void testGetRefundNotFound() {
        given()
                .pathParam("id", 99999)
                .when()
                .get("/api/billing/refunds/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    @Transactional
    public void testGetRefundsByPayment() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        Payment payment = createTestPayment(invoice.id);
        payment.status = PaymentStatus.COMPLETED;
        paymentRepository.persist(payment);

        // Create two refunds
        createRefund(payment.id, "30.00");
        createRefund(payment.id, "20.00");

        given()
                .pathParam("paymentId", payment.id)
                .when()
                .get("/api/billing/refunds/payment/{paymentId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("[0].paymentId", equalTo(payment.id.intValue()))
                .body("[1].paymentId", equalTo(payment.id.intValue()));
    }

    @Test
    @Transactional
    public void testGetRefundsByInvoice() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        Payment payment = createTestPayment(invoice.id);
        payment.status = PaymentStatus.COMPLETED;
        paymentRepository.persist(payment);

        createRefund(payment.id, "50.00");

        given()
                .pathParam("invoiceId", invoice.id)
                .when()
                .get("/api/billing/refunds/invoice/{invoiceId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].invoiceId", equalTo(invoice.id.intValue()));
    }

    @Test
    @Transactional
    public void testGetRefundsByPatient() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        Payment payment = createTestPayment(invoice.id);
        payment.status = PaymentStatus.COMPLETED;
        paymentRepository.persist(payment);

        createRefund(payment.id, "50.00");

        given()
                .pathParam("patientId", 1)
                .when()
                .get("/api/billing/refunds/patient/{patientId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].patientId", equalTo(1));
    }

    @Test
    @Transactional
    public void testGetRefundByTransactionId() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        Payment payment = createTestPayment(invoice.id);
        payment.status = PaymentStatus.COMPLETED;
        paymentRepository.persist(payment);

        String transactionId = createRefund(payment.id, "50.00");

        given()
                .pathParam("refundTransactionId", transactionId)
                .when()
                .get("/api/billing/refunds/transaction/{refundTransactionId}")
                .then()
                .statusCode(200)
                .body("refundTransactionId", equalTo(transactionId));
    }

    private Invoice createTestInvoice() {
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 1L;
        invoice.subtotal = new BigDecimal("100.00");
        invoice.tax = new BigDecimal("10.00");
        invoice.total = new BigDecimal("110.00");
        return invoice;
    }

    private Payment createTestPayment(Long invoiceId) {
        Payment payment = new Payment();
        payment.invoiceId = invoiceId;
        payment.patientId = 1L;
        payment.amount = new BigDecimal("100.00");
        payment.paymentMethod = PaymentMethodType.CREDIT_CARD;
        payment.gateway = "STRIPE";
        payment.transactionId = "txn_test_" + System.currentTimeMillis();
        return payment;
    }

    private String createRefund(Long paymentId, String amount) {
        RefundRequest request = new RefundRequest();
        request.paymentId = paymentId;
        request.amount = new BigDecimal(amount);
        request.reason = "Test refund";

        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/refunds")
                .then()
                .statusCode(201)
                .extract().path("refundTransactionId");
    }
}
