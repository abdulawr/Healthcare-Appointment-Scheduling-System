package com.basit.service;

import com.basit.dto.request.RefundRequest;
import com.basit.dto.response.RefundResponse;
import com.basit.entity.Invoice;
import com.basit.entity.Payment;
import com.basit.constant.InvoiceStatus;
import com.basit.constant.PaymentMethodType;
import com.basit.constant.PaymentStatus;
import com.basit.constant.RefundStatus;
import com.basit.repository.InvoiceRepository;
import com.basit.repository.PaymentRepository;
import com.basit.repository.RefundRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class RefundServiceTest {

    @Inject
    RefundService refundService;

    @Inject
    PaymentRepository paymentRepository;

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    RefundRepository refundRepository;

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
        // Create invoice
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        // Create completed payment
        Payment payment = createTestPayment(invoice.id);
        payment.status = PaymentStatus.COMPLETED;
        paymentRepository.persist(payment);

        // Create refund request
        RefundRequest request = new RefundRequest();
        request.paymentId = payment.id;
        request.amount = new BigDecimal("50.00");
        request.reason = "Customer request";

        // Process refund
        RefundResponse response = refundService.processRefund(request);

        assertNotNull(response);
        assertNotNull(response.id);
        assertEquals(0, new BigDecimal("50.00").compareTo(response.amount));
        assertEquals("Customer request", response.reason);
        assertNotNull(response.refundTransactionId);
        assertTrue(response.refundTransactionId.startsWith("REF-"));
        assertEquals(RefundStatus.COMPLETED, response.status);
    }

    @Test
    @Transactional
    public void testRefundExceedsPaymentAmount() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        Payment payment = createTestPayment(invoice.id);
        payment.status = PaymentStatus.COMPLETED;
        payment.amount = new BigDecimal("100.00");
        paymentRepository.persist(payment);

        RefundRequest request = new RefundRequest();
        request.paymentId = payment.id;
        request.amount = new BigDecimal("150.00"); // More than payment
        request.reason = "Test";

        assertThrows(IllegalArgumentException.class, () -> {
            refundService.processRefund(request);
        }, "Refund amount cannot exceed payment amount");
    }

    @Test
    @Transactional
    public void testRefundNonCompletedPayment() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        Payment payment = createTestPayment(invoice.id);
        payment.status = PaymentStatus.PENDING; // Not completed
        paymentRepository.persist(payment);

        RefundRequest request = new RefundRequest();
        request.paymentId = payment.id;
        request.amount = new BigDecimal("50.00");
        request.reason = "Test";

        assertThrows(IllegalStateException.class, () -> {
            refundService.processRefund(request);
        }, "Can only refund completed payments");
    }

    @Test
    @Transactional
    public void testRefundUpdatesInvoiceBalance() {
        Invoice invoice = createTestInvoice();
        invoice.amountPaid = new BigDecimal("100.00");
        invoice.calculateBalance();
        invoiceRepository.persist(invoice);

        Payment payment = createTestPayment(invoice.id);
        payment.status = PaymentStatus.COMPLETED;
        paymentRepository.persist(payment);

        RefundRequest request = new RefundRequest();
        request.paymentId = payment.id;
        request.amount = new BigDecimal("50.00");
        request.reason = "Test";

        refundService.processRefund(request);

        // Verify invoice updated
        Invoice updatedInvoice = invoiceRepository.findById(invoice.id);
        assertEquals(0, new BigDecimal("50.00").compareTo(updatedInvoice.amountPaid));
        assertEquals(0, new BigDecimal("60.00").compareTo(updatedInvoice.balance));
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

        RefundResponse created = refundService.processRefund(request);

        // Get refund
        RefundResponse retrieved = refundService.getRefund(created.id);

        assertNotNull(retrieved);
        assertEquals(created.id, retrieved.id);
        assertEquals(created.refundTransactionId, retrieved.refundTransactionId);
    }

    @Test
    @Transactional
    public void testGetRefundsByPayment() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        Payment payment = createTestPayment(invoice.id);
        payment.status = PaymentStatus.COMPLETED;
        paymentRepository.persist(payment);

        // Create multiple refunds
        RefundRequest request1 = createRefundRequest(payment.id, "30.00");
        RefundRequest request2 = createRefundRequest(payment.id, "20.00");

        refundService.processRefund(request1);
        refundService.processRefund(request2);

        List<RefundResponse> refunds = refundService.getRefundsByPayment(payment.id);

        assertEquals(2, refunds.size());
    }

    @Test
    @Transactional
    public void testGetRefundsByPatient() {
        Invoice invoice = createTestInvoice();
        invoiceRepository.persist(invoice);

        Payment payment = createTestPayment(invoice.id);
        payment.status = PaymentStatus.COMPLETED;
        paymentRepository.persist(payment);

        RefundRequest request = createRefundRequest(payment.id, "50.00");
        refundService.processRefund(request);

        List<RefundResponse> refunds = refundService.getRefundsByPatient(1L);

        assertEquals(1, refunds.size());
        assertEquals(1L, refunds.get(0).patientId);
    }

    private Invoice createTestInvoice() {
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 1L;
        invoice.subtotal = new BigDecimal("100.00");
        invoice.tax = new BigDecimal("10.00");
        invoice.total = new BigDecimal("110.00");
        invoice.status = InvoiceStatus.PENDING;
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

    private RefundRequest createRefundRequest(Long paymentId, String amount) {
        RefundRequest request = new RefundRequest();
        request.paymentId = paymentId;
        request.amount = new BigDecimal(amount);
        request.reason = "Test refund";
        return request;
    }
}
