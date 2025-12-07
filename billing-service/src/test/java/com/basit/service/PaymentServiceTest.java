package com.basit.service;

import com.basit.constant.InvoiceStatus;
import com.basit.constant.PaymentMethodType;
import com.basit.constant.PaymentStatus;
import com.basit.entity.Invoice;
import com.basit.entity.InvoiceItem;
import com.basit.entity.Payment;
import com.basit.repository.InvoiceRepository;
import com.basit.repository.PaymentRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentServiceTest {

    @Inject
    PaymentService paymentService;

    @Inject
    PaymentRepository paymentRepository;

    @Inject
    InvoiceService invoiceService;

    @Inject
    InvoiceRepository invoiceRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        paymentRepository.deleteAll();
        invoiceRepository.deleteAll();
    }

    @Test
    @Order(1)
    @Transactional
    void testProcessPayment() {
        // Given
        Invoice invoice = createInvoiceWithItems();
        Payment payment = createTestPayment(invoice.id);

        // When
        Payment processed = paymentService.processPayment(payment);

        // Then
        assertNotNull(processed.id);
        assertEquals(PaymentStatus.COMPLETED, processed.status);
        assertNotNull(processed.transactionId);
        assertNotNull(processed.processedAt);
    }

    @Test
    @Order(2)
    @Transactional
    void testProcessPayment_WithIdempotencyKey() {
        // Given
        Invoice invoice = createInvoiceWithItems();
        Payment payment1 = createTestPayment(invoice.id);
        payment1.idempotencyKey = "UNIQUE-KEY-123";

        // When
        Payment processed1 = paymentService.processPayment(payment1);

        // Try to process again with same idempotency key
        Payment payment2 = createTestPayment(invoice.id);
        payment2.idempotencyKey = "UNIQUE-KEY-123";
        Payment processed2 = paymentService.processPayment(payment2);

        // Then
        assertEquals(processed1.id, processed2.id);
        assertEquals(1, paymentRepository.count());
    }

    @Test
    @Order(3)
    @Transactional
    void testProcessPayment_AmountExceedsAmountDue_ShouldFail() {
        // Given
        Invoice invoice = createInvoiceWithItems();
        Payment payment = createTestPayment(invoice.id);
        payment.amount = new BigDecimal("99999.00"); // Exceeds amount due

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPayment(payment);
        });
    }

    @Test
    @Order(4)
    @Transactional
    void testGetPaymentById() {
        // Given
        Invoice invoice = createInvoiceWithItems();
        Payment payment = createTestPayment(invoice.id);
        Payment processed = paymentService.processPayment(payment);

        // When
        Payment found = paymentService.getPaymentById(processed.id);

        // Then
        assertNotNull(found);
        assertEquals(processed.id, found.id);
    }

    @Test
    @Order(5)
    @Transactional
    void testGetPaymentByTransactionId() {
        // Given
        Invoice invoice = createInvoiceWithItems();
        Payment payment = createTestPayment(invoice.id);
        Payment processed = paymentService.processPayment(payment);

        // When
        Payment found = paymentService.getPaymentByTransactionId(processed.transactionId);

        // Then
        assertNotNull(found);
        assertEquals(processed.transactionId, found.transactionId);
    }

    @Test
    @Order(6)
    @Transactional
    void testGetPaymentsByInvoiceId() {
        // Given
        Invoice invoice = createInvoiceWithItems();
        Payment payment1 = createTestPayment(invoice.id);
        payment1.amount = new BigDecimal("50.00");
        paymentService.processPayment(payment1);

        Payment payment2 = createTestPayment(invoice.id);
        payment2.amount = new BigDecimal("50.00");
        paymentService.processPayment(payment2);

        // When
        List<Payment> payments = paymentService.getPaymentsByInvoiceId(invoice.id);

        // Then
        assertEquals(2, payments.size());
    }

    @Test
    @Order(7)
    @Transactional
    void testGetPaymentsByPatientId() {
        // Given
        Long patientId = 1L;
        Invoice invoice = createInvoiceWithItems();
        invoice.patientId = patientId;
        Payment payment = createTestPayment(invoice.id);
        payment.patientId = patientId;
        paymentService.processPayment(payment);

        // When
        List<Payment> payments = paymentService.getPaymentsByPatientId(patientId);

        // Then
        assertTrue(payments.size() > 0);
    }

    @Test
    @Order(8)
    @Transactional
    void testGetSuccessfulPayments() {
        // Given
        Invoice invoice = createInvoiceWithItems();
        Payment payment = createTestPayment(invoice.id);
        paymentService.processPayment(payment);

        // When
        List<Payment> successful = paymentService.getSuccessfulPayments();

        // Then
        assertTrue(successful.size() > 0);
        assertTrue(successful.stream().allMatch(p -> p.status == PaymentStatus.COMPLETED));
    }

    @Test
    @Order(9)
    @Transactional
    void testCalculateTotalPaymentsForInvoice() {
        // Given
        Invoice invoice = createInvoiceWithItems();
        Payment payment1 = createTestPayment(invoice.id);
        payment1.amount = new BigDecimal("50.00");
        paymentService.processPayment(payment1);

        Payment payment2 = createTestPayment(invoice.id);
        payment2.amount = new BigDecimal("30.00");
        paymentService.processPayment(payment2);

        // When
        BigDecimal total = paymentService.calculateTotalPaymentsForInvoice(invoice.id);

        // Then
        assertEquals(new BigDecimal("80.00"), total);
    }

    @Test
    @Order(10)
    @Transactional
    void testCanRefund() {
        // Given
        Invoice invoice = createInvoiceWithItems();
        Payment payment = createTestPayment(invoice.id);
        Payment processed = paymentService.processPayment(payment);

        // When
        boolean canRefund = paymentService.canRefund(processed.id);

        // Then
        assertTrue(canRefund);
    }

    @Test
    @Order(11)
    @Transactional
    void testGetRefundableAmount() {
        // Given
        Invoice invoice = createInvoiceWithItems();
        Payment payment = createTestPayment(invoice.id);
        payment.amount = new BigDecimal("100.00");
        Payment processed = paymentService.processPayment(payment);

        // When
        BigDecimal refundableAmount = paymentService.getRefundableAmount(processed.id);

        // Then
        assertEquals(new BigDecimal("100.00"), refundableAmount);
    }

    @Test
    @Order(12)
    @Transactional
    void testExistsByIdempotencyKey() {
        // Given
        Invoice invoice = createInvoiceWithItems();
        Payment payment = createTestPayment(invoice.id);
        payment.idempotencyKey = "TEST-KEY-456";
        paymentService.processPayment(payment);

        // When
        boolean exists = paymentService.existsByIdempotencyKey("TEST-KEY-456");

        // Then
        assertTrue(exists);
    }

    // Helper methods - NOT @Transactional
    Payment createTestPayment(Long invoiceId) {
        Payment payment = new Payment();
        payment.invoiceId = invoiceId;
        payment.patientId = 1L;
        payment.amount = new BigDecimal("100.00");
        payment.paymentMethod = PaymentMethodType.CREDIT_CARD;
        payment.paymentGateway = "stripe";
        return payment;
    }

    Invoice createInvoiceWithItems() {
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 1L;
        invoice.invoiceNumber = "INV-" + System.currentTimeMillis();
        invoice.issueDate = LocalDate.now();
        invoice.dueDate = LocalDate.now().plusDays(30);
        invoice.taxAmount = new BigDecimal("10.00");
        invoice.discountAmount = BigDecimal.ZERO;

        InvoiceItem item = new InvoiceItem();
        item.description = "Consultation";
        item.quantity = 1;
        item.unitPrice = new BigDecimal("100.00");
        invoice.addItem(item);

        invoice.status = InvoiceStatus.ISSUED;
        return invoiceService.createInvoice(invoice);
    }
}