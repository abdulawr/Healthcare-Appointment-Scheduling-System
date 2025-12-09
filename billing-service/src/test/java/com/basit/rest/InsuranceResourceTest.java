package com.basit.rest;

import com.basit.dto.request.InsuranceClaimRequest;
import com.basit.entity.Invoice;
import com.basit.repository.InsuranceClaimRepository;
import com.basit.repository.InvoiceRepository;
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
public class InsuranceResourceTest {

    @Inject
    InsuranceClaimRepository claimRepository;

    @Inject
    InvoiceRepository invoiceRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        claimRepository.deleteAll();
        invoiceRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testSubmitClaim() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        InsuranceClaimRequest request = new InsuranceClaimRequest();
        request.invoiceId = invoice.id;
        request.insuranceProvider = "Blue Cross";
        request.policyNumber = "POL-123456";
        request.claimedAmount = new BigDecimal("110.00");
        request.notes = "Test claim";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/insurance/claim")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("invoiceId", equalTo(invoice.id.intValue()))
                .body("patientId", equalTo(1))
                .body("insuranceProvider", equalTo("Blue Cross"))
                .body("policyNumber", equalTo("POL-123456"))
                .body("claimedAmount", equalTo(110.00f))
                .body("status", equalTo("SUBMITTED"))
                .body("claimNumber", notNullValue())
                .body("claimNumber", startsWith("CLM-"))
                .body("submissionDate", notNullValue());
    }

    @Test
    @Transactional
    public void testSubmitClaimInvoiceNotFound() {
        InsuranceClaimRequest request = new InsuranceClaimRequest();
        request.invoiceId = 99999L;
        request.insuranceProvider = "Blue Cross";
        request.policyNumber = "POL-123456";
        request.claimedAmount = new BigDecimal("110.00");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/insurance/claim")
                .then()
                .statusCode(404);
    }

    @Test
    @Transactional
    public void testSubmitClaimExceedsInvoiceTotal() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        InsuranceClaimRequest request = new InsuranceClaimRequest();
        request.invoiceId = invoice.id;
        request.insuranceProvider = "Blue Cross";
        request.policyNumber = "POL-123456";
        request.claimedAmount = new BigDecimal("200.00");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/insurance/claim")
                .then()
                .statusCode(400);
    }

    @Test
    @Transactional
    public void testVerifyCoverage() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        InsuranceClaimRequest request = new InsuranceClaimRequest();
        request.invoiceId = invoice.id;
        request.insuranceProvider = "Blue Cross";
        request.policyNumber = "POL-123456";
        request.claimedAmount = new BigDecimal("110.00");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/insurance/verify")
                .then()
                .statusCode(200)
                .body("claimNumber", notNullValue())
                .body("claimNumber", startsWith("VERIFY-"))
                .body("status", equalTo("UNDER_REVIEW"));
    }

    @Test
    @Transactional
    public void testApproveClaim() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        Long claimId = createClaim(invoice.id);

        given()
                .pathParam("id", claimId)
                .queryParam("approvedAmount", 110.00)
                .when()
                .post("/api/billing/insurance/claim/{id}/approve")
                .then()
                .statusCode(200)
                .body("id", equalTo(claimId.intValue()))
                .body("status", equalTo("APPROVED"))
                .body("approvedAmount", equalTo(110.00f))
                .body("approvalDate", notNullValue());
    }

    @Test
    @Transactional
    public void testPartialApproval() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        Long claimId = createClaim(invoice.id);

        given()
                .pathParam("id", claimId)
                .queryParam("approvedAmount", 88.00)
                .when()
                .post("/api/billing/insurance/claim/{id}/approve")
                .then()
                .statusCode(200)
                .body("approvedAmount", equalTo(88.00f))
                .body("status", equalTo("APPROVED"));
    }

    @Test
    @Transactional
    public void testRejectClaim() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        Long claimId = createClaim(invoice.id);

        given()
                .pathParam("id", claimId)
                .queryParam("reason", "Service not covered")
                .when()
                .post("/api/billing/insurance/claim/{id}/reject")
                .then()
                .statusCode(200)
                .body("id", equalTo(claimId.intValue()))
                .body("status", equalTo("REJECTED"))
                .body("notes", equalTo("Service not covered"))
                .body("approvalDate", notNullValue());
    }

    @Test
    @Transactional
    public void testGetClaim() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        Long claimId = createClaim(invoice.id);

        given()
                .pathParam("id", claimId)
                .when()
                .get("/api/billing/insurance/claim/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(claimId.intValue()))
                .body("insuranceProvider", equalTo("Blue Cross"));
    }

    @Test
    public void testGetClaimNotFound() {
        given()
                .pathParam("id", 99999)
                .when()
                .get("/api/billing/insurance/claim/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    @Transactional
    public void testGetClaimByNumber() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        String claimNumber = createClaimAndGetNumber(invoice.id);

        given()
                .pathParam("claimNumber", claimNumber)
                .when()
                .get("/api/billing/insurance/claim/number/{claimNumber}")
                .then()
                .statusCode(200)
                .body("claimNumber", equalTo(claimNumber));
    }

    @Test
    @Transactional
    public void testGetClaimsByInvoice() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        createClaim(invoice.id);

        given()
                .pathParam("invoiceId", invoice.id)
                .when()
                .get("/api/billing/insurance/claims/invoice/{invoiceId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].invoiceId", equalTo(invoice.id.intValue()));
    }

    @Test
    @Transactional
    public void testGetClaimsByPatient() {
        Invoice invoice1 = createTestInvoice();
        invoiceRepository.persist(invoice1);

        Invoice invoice2 = createTestInvoice();
        invoice2.appointmentId = 2L;
        invoiceRepository.persist(invoice2);

        createClaim(invoice1.id);
        createClaim(invoice2.id);

        given()
                .pathParam("patientId", 1)
                .when()
                .get("/api/billing/insurance/claims/patient/{patientId}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("[0].patientId", equalTo(1))
                .body("[1].patientId", equalTo(1));
    }

    @Test
    @Transactional
    public void testGetClaimsByProvider() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        createClaimWithProvider(invoice.id, "Aetna");

        given()
                .pathParam("provider", "Aetna")
                .when()
                .get("/api/billing/insurance/claims/provider/{provider}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].insuranceProvider", equalTo("Aetna"));
    }

    @Test
    @Transactional
    public void testGetClaimsByStatus() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        createClaim(invoice.id);

        given()
                .pathParam("status", "SUBMITTED")
                .when()
                .get("/api/billing/insurance/claims/status/{status}")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].status", equalTo("SUBMITTED"));
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

    private Long createClaim(Long invoiceId) {
        InsuranceClaimRequest request = new InsuranceClaimRequest();
        request.invoiceId = invoiceId;
        request.insuranceProvider = "Blue Cross";
        request.policyNumber = "POL-123456";
        request.claimedAmount = new BigDecimal("110.00");

        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/insurance/claim")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

    }

    private String createClaimAndGetNumber(Long invoiceId) {
        InsuranceClaimRequest request = new InsuranceClaimRequest();
        request.invoiceId = invoiceId;
        request.insuranceProvider = "Blue Cross";
        request.policyNumber = "POL-123456";
        request.claimedAmount = new BigDecimal("110.00");

        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/insurance/claim")
                .then()
                .statusCode(201)
                .extract().path("claimNumber");
    }

    private void createClaimWithProvider(Long invoiceId, String provider) {
        InsuranceClaimRequest request = new InsuranceClaimRequest();
        request.invoiceId = invoiceId;
        request.insuranceProvider = provider;
        request.policyNumber = "POL-123456";
        request.claimedAmount = new BigDecimal("110.00");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/insurance/claim")
                .then()
                .statusCode(201);
    }
}
