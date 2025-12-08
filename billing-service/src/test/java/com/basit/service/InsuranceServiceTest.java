package com.basit.service;

import com.basit.dto.request.InsuranceClaimRequest;
import com.basit.dto.response.InsuranceClaimResponse;
import com.basit.entity.Invoice;
import com.basit.constant.ClaimStatus;
import com.basit.repository.InsuranceClaimRepository;
import com.basit.repository.InvoiceRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class InsuranceServiceTest {

    @Inject
    InsuranceService insuranceService;

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    InsuranceClaimRepository claimRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        claimRepository.deleteAll();
        invoiceRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testSubmitClaim() {
        // Create invoice
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        // Create claim request
        InsuranceClaimRequest request = new InsuranceClaimRequest();
        request.invoiceId = invoice.id;
        request.insuranceProvider = "Blue Cross";
        request.policyNumber = "POL-123456";
        request.claimedAmount = new BigDecimal("110.00");
        request.notes = "Test claim";

        // Submit claim
        InsuranceClaimResponse response = insuranceService.submitClaim(request);

        assertNotNull(response);
        assertNotNull(response.id);
        assertEquals(ClaimStatus.SUBMITTED, response.status);
        assertNotNull(response.claimNumber);
        assertTrue(response.claimNumber.startsWith("CLM-"));
        assertEquals("Blue Cross", response.insuranceProvider);
        assertEquals("POL-123456", response.policyNumber);
        assertEquals(0, new BigDecimal("110.00").compareTo(response.claimedAmount));
        assertEquals(1L, response.patientId);
        assertNotNull(response.submissionDate);
    }

    @Test
    @Transactional
    public void testClaimExceedsInvoiceTotal() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        InsuranceClaimRequest request = new InsuranceClaimRequest();
        request.invoiceId = invoice.id;
        request.insuranceProvider = "Blue Cross";
        request.policyNumber = "POL-123456";
        request.claimedAmount = new BigDecimal("200.00"); // Exceeds total

        assertThrows(IllegalArgumentException.class, () -> {
            insuranceService.submitClaim(request);
        }, "Claimed amount cannot exceed invoice total");
    }

    @Test
    @Transactional
    public void testDuplicateClaimForInvoice() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        InsuranceClaimRequest request = new InsuranceClaimRequest();
        request.invoiceId = invoice.id;
        request.insuranceProvider = "Blue Cross";
        request.policyNumber = "POL-123456";
        request.claimedAmount = new BigDecimal("110.00");

        // Submit first claim
        insuranceService.submitClaim(request);

        // Try to submit duplicate
        assertThrows(IllegalStateException.class, () -> {
            insuranceService.submitClaim(request);
        }, "Insurance claim already exists for invoice");
    }

    @Test
    @Transactional
    public void testApproveClaim() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        InsuranceClaimRequest request = new InsuranceClaimRequest();
        request.invoiceId = invoice.id;
        request.insuranceProvider = "Blue Cross";
        request.policyNumber = "POL-123456";
        request.claimedAmount = new BigDecimal("110.00");

        InsuranceClaimResponse submitted = insuranceService.submitClaim(request);

        // Approve claim
        BigDecimal approvedAmount = new BigDecimal("110.00");
        InsuranceClaimResponse approved = insuranceService.approveClaim(
                submitted.id, approvedAmount);

        assertEquals(ClaimStatus.APPROVED, approved.status);
        assertNotNull(approved.approvalDate);
        assertEquals(0, approvedAmount.compareTo(approved.approvedAmount));

        // Verify invoice updated
        Invoice updatedInvoice = invoiceRepository.findById(invoice.id);
        assertEquals(0, approvedAmount.compareTo(updatedInvoice.amountPaid));
        assertEquals(0, BigDecimal.ZERO.compareTo(updatedInvoice.balance));
    }

    @Test
    @Transactional
    public void testPartialApproval() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        InsuranceClaimRequest request = new InsuranceClaimRequest();
        request.invoiceId = invoice.id;
        request.insuranceProvider = "Blue Cross";
        request.policyNumber = "POL-123456";
        request.claimedAmount = new BigDecimal("110.00");

        InsuranceClaimResponse submitted = insuranceService.submitClaim(request);

        // Partially approve (only 80% covered)
        BigDecimal approvedAmount = new BigDecimal("88.00");
        InsuranceClaimResponse approved = insuranceService.approveClaim(
                submitted.id, approvedAmount);

        assertEquals(ClaimStatus.APPROVED, approved.status);
        assertEquals(0, approvedAmount.compareTo(approved.approvedAmount));

        // Verify invoice partially paid
        Invoice updatedInvoice = invoiceRepository.findById(invoice.id);
        assertEquals(0, new BigDecimal("88.00").compareTo(updatedInvoice.amountPaid));
        assertEquals(0, new BigDecimal("22.00").compareTo(updatedInvoice.balance));
    }

    @Test
    @Transactional
    public void testRejectClaim() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        InsuranceClaimRequest request = new InsuranceClaimRequest();
        request.invoiceId = invoice.id;
        request.insuranceProvider = "Blue Cross";
        request.policyNumber = "POL-123456";
        request.claimedAmount = new BigDecimal("110.00");

        InsuranceClaimResponse submitted = insuranceService.submitClaim(request);

        // Reject claim
        String rejectionReason = "Service not covered under policy";
        InsuranceClaimResponse rejected = insuranceService.rejectClaim(
                submitted.id, rejectionReason);

        assertEquals(ClaimStatus.REJECTED, rejected.status);
        assertEquals(rejectionReason, rejected.notes);
        assertNotNull(rejected.approvalDate);
    }

    @Test
    @Transactional
    public void testGetClaim() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        InsuranceClaimRequest request = createClaimRequest(invoice.id);
        InsuranceClaimResponse created = insuranceService.submitClaim(request);

        InsuranceClaimResponse retrieved = insuranceService.getClaim(created.id);

        assertNotNull(retrieved);
        assertEquals(created.id, retrieved.id);
        assertEquals(created.claimNumber, retrieved.claimNumber);
    }

    @Test
    @Transactional
    public void testGetClaimByNumber() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        InsuranceClaimRequest request = createClaimRequest(invoice.id);
        InsuranceClaimResponse created = insuranceService.submitClaim(request);

        InsuranceClaimResponse retrieved = insuranceService.getClaimByNumber(
                created.claimNumber);

        assertNotNull(retrieved);
        assertEquals(created.id, retrieved.id);
    }

    @Test
    @Transactional
    public void testGetClaimsByPatient() {
        Invoice invoice1 = createTestInvoice();
        invoiceRepository.persist(invoice1);

        Invoice invoice2 = createTestInvoice();
        invoice2.appointmentId = 2L;
        invoiceRepository.persist(invoice2);

        insuranceService.submitClaim(createClaimRequest(invoice1.id));
        insuranceService.submitClaim(createClaimRequest(invoice2.id));

        List<InsuranceClaimResponse> claims = insuranceService.getClaimsByPatient(1L);

        assertEquals(2, claims.size());
    }

    @Test
    @Transactional
    public void testGetClaimsByProvider() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        InsuranceClaimRequest request = createClaimRequest(invoice.id);
        request.insuranceProvider = "Aetna";
        insuranceService.submitClaim(request);

        List<InsuranceClaimResponse> claims =
                insuranceService.getClaimsByProvider("Aetna");

        assertEquals(1, claims.size());
        assertEquals("Aetna", claims.get(0).insuranceProvider);
    }

    @Test
    @Transactional
    public void testGetClaimsByStatus() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        InsuranceClaimRequest request = createClaimRequest(invoice.id);
        insuranceService.submitClaim(request);

        List<InsuranceClaimResponse> claims =
                insuranceService.getClaimsByStatus(ClaimStatus.SUBMITTED);

        assertEquals(1, claims.size());
        assertEquals(ClaimStatus.SUBMITTED, claims.get(0).status);
    }

    @Test
    @Transactional
    public void testVerifyCoverage() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        InsuranceClaimRequest request = createClaimRequest(invoice.id);

        InsuranceClaimResponse verification = insuranceService.verifyCoverage(request);

        assertNotNull(verification);
        assertNotNull(verification.claimNumber);
        assertTrue(verification.claimNumber.startsWith("VERIFY-"));
        assertEquals(ClaimStatus.UNDER_REVIEW, verification.status);
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

    private InsuranceClaimRequest createClaimRequest(Long invoiceId) {
        InsuranceClaimRequest request = new InsuranceClaimRequest();
        request.invoiceId = invoiceId;
        request.insuranceProvider = "Blue Cross";
        request.policyNumber = "POL-123456";
        request.claimedAmount = new BigDecimal("110.00");
        return request;
    }
}
