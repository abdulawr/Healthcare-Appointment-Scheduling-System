package com.basit.service;

import com.basit.constant.PaymentStatus;
import com.basit.entity.Invoice;
import com.basit.entity.Payment;
import com.basit.repository.InvoiceRepository;
import com.basit.repository.PaymentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for Payment business logic
 */
@ApplicationScoped
public class PaymentService {

    @Inject
    PaymentRepository paymentRepository;

    @Inject
    InvoiceRepository invoiceRepository;

    /**
     * Process a new payment
     */
    @Transactional
    public Payment processPayment(Payment payment) {
        // Check for duplicate payment using idempotency key
        if (payment.idempotencyKey != null &&
                paymentRepository.existsByIdempotencyKey(payment.idempotencyKey)) {
            // Return existing payment
            return paymentRepository.findByIdempotencyKey(payment.idempotencyKey)
                    .orElseThrow();
        }

        // Generate idempotency key if not provided
        if (payment.idempotencyKey == null) {
            payment.idempotencyKey = UUID.randomUUID().toString();
        }

        // Get the invoice
        Invoice invoice = invoiceRepository.findById(payment.invoiceId);
        if (invoice == null) {
            throw new RuntimeException("Invoice not found with id: " + payment.invoiceId);
        }

        // Validate payment amount
        if (payment.amount.compareTo(invoice.amountDue) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds amount due");
        }

        // Set initial status
        payment.status = PaymentStatus.PROCESSING;

        // Persist payment (onCreate() will be called automatically by @PrePersist)
        paymentRepository.persist(payment);

        try {
            // TODO: Integrate with payment gateway (Stripe/PayPal)
            // For now, simulate successful payment
            payment.markCompleted();
            payment.transactionId = "TXN-" + UUID.randomUUID().toString();
            payment.processedAt = LocalDateTime.now();

            // Update invoice
            invoice.recordPayment(payment.amount);

        } catch (Exception e) {
            payment.markFailed("Payment processing failed: " + e.getMessage());
            throw new RuntimeException("Payment failed", e);
        }

        return payment;
    }

    /**
     * Get payment by ID
     */
    public Payment getPaymentById(Long id) {
        return paymentRepository.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    /**
     * Get payment by transaction ID
     */
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found with transaction ID: " + transactionId));
    }

    /**
     * Get all payments for an invoice
     */
    public List<Payment> getPaymentsByInvoiceId(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }

    /**
     * Get all payments for a patient
     */
    public List<Payment> getPaymentsByPatientId(Long patientId) {
        return paymentRepository.findByPatientId(patientId);
    }

    /**
     * Get successful payments
     */
    public List<Payment> getSuccessfulPayments() {
        return paymentRepository.findSuccessfulPayments();
    }

    /**
     * Get failed payments
     */
    public List<Payment> getFailedPayments() {
        return paymentRepository.findFailedPayments();
    }

    /**
     * Get pending payments
     */
    public List<Payment> getPendingPayments() {
        return paymentRepository.findPendingPayments();
    }

    /**
     * Retry failed payment
     */
    @Transactional
    public Payment retryPayment(Long paymentId) {
        Payment payment = getPaymentById(paymentId);

        if (payment.status != PaymentStatus.FAILED) {
            throw new IllegalStateException("Only failed payments can be retried");
        }

        payment.status = PaymentStatus.PROCESSING;

        try {
            // TODO: Retry payment gateway call
            payment.markCompleted();
            payment.processedAt = LocalDateTime.now();

            // Update invoice
            Invoice invoice = invoiceRepository.findById(payment.invoiceId);
            invoice.recordPayment(payment.amount);

        } catch (Exception e) {
            payment.markFailed("Retry failed: " + e.getMessage());
            throw new RuntimeException("Payment retry failed", e);
        }

        return payment;
    }

    /**
     * Calculate total payments for invoice
     */
    public BigDecimal calculateTotalPaymentsForInvoice(Long invoiceId) {
        BigDecimal total = paymentRepository.calculateTotalForInvoice(invoiceId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Calculate total payments for patient
     */
    public BigDecimal calculateTotalPaymentsForPatient(Long patientId) {
        BigDecimal total = paymentRepository.calculateTotalForPatient(patientId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Get refundable payments
     */
    public List<Payment> getRefundablePayments() {
        return paymentRepository.findRefundablePayments();
    }

    /**
     * Check if payment can be refunded
     */
    public boolean canRefund(Long paymentId) {
        Payment payment = getPaymentById(paymentId);
        return payment.canBeRefunded();
    }

    /**
     * Get refundable amount for payment
     */
    public BigDecimal getRefundableAmount(Long paymentId) {
        Payment payment = getPaymentById(paymentId);
        return payment.getRefundableAmount();
    }

    /**
     * Get recently processed payments
     */
    public List<Payment> getRecentlyProcessedPayments(int days) {
        return paymentRepository.findRecentlyProcessed(days);
    }

    /**
     * Get all payments
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.listAll();
    }

    /**
     * Count payments by status
     */
    public long countByStatus(PaymentStatus status) {
        return paymentRepository.countByStatus(status);
    }

    /**
     * Verify payment exists with idempotency key
     */
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return paymentRepository.existsByIdempotencyKey(idempotencyKey);
    }
}