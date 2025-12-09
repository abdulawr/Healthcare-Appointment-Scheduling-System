package com.basit.repository;

import com.basit.entity.Payment;
import com.basit.constant.PaymentMethodType;
import com.basit.constant.PaymentStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class PaymentRepositoryTest {

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    PaymentRepository paymentRepository;

    @Inject
    PaymentMethodRepository paymentMethodRepository;

    @Inject
    InsuranceClaimRepository insuranceClaimRepository;

    @Inject
    RefundRepository refundRepository;


    @BeforeEach
    @Transactional
    public void setup() {
        // Delete children FIRST (to avoid foreign key constraint violations)
        paymentRepository.deleteAll();
        refundRepository.deleteAll();
        insuranceClaimRepository.deleteAll();

        // Delete parent LAST
        invoiceRepository.deleteAll();

        // Delete independent entities
        paymentMethodRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testFindByInvoiceId() {
        Payment payment1 = createTestPayment(1L, 100L, "txn_001");
        Payment payment2 = createTestPayment(1L, 100L, "txn_002");
        Payment payment3 = createTestPayment(2L, 101L, "txn_003");

        paymentRepository.persist(payment1);
        paymentRepository.persist(payment2);
        paymentRepository.persist(payment3);

        List<Payment> invoice100Payments = paymentRepository.findByInvoiceId(100L);
        assertEquals(2, invoice100Payments.size());
    }

    @Test
    @Transactional
    public void testFindByTransactionId() {
        Payment payment = createTestPayment(1L, 100L, "txn_unique_123");
        paymentRepository.persist(payment);

        Payment found = paymentRepository.findByTransactionId("txn_unique_123");
        assertNotNull(found);
        assertEquals("txn_unique_123", found.transactionId);
    }

    @Test
    @Transactional
    public void testFindFailedPayments() {
        Payment successPayment = createTestPayment(1L, 100L, "txn_001");
        successPayment.status = PaymentStatus.COMPLETED;

        Payment failedPayment = createTestPayment(2L, 101L, "txn_002");
        failedPayment.status = PaymentStatus.FAILED;

        paymentRepository.persist(successPayment);
        paymentRepository.persist(failedPayment);

        List<Payment> failed = paymentRepository.findFailedPayments();
        assertEquals(1, failed.size());
        assertEquals(failedPayment.id, failed.get(0).id);
    }

    private Payment createTestPayment(Long patientId, Long invoiceId, String txnId) {
        Payment payment = new Payment();
        payment.patientId = patientId;
        payment.invoiceId = invoiceId;
        payment.amount = new BigDecimal("50.00");
        payment.paymentMethod = PaymentMethodType.CREDIT_CARD;
        payment.transactionId = txnId;
        payment.gateway = "STRIPE";
        return payment;
    }
}