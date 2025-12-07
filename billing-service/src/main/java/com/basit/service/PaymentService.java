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

@ApplicationScoped
public class PaymentService {

    @Inject
    PaymentRepository paymentRepository;

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    InvoiceService invoiceService;

    @Transactional
    public Payment processPayment(Payment payment) {
        // Check for duplicate using idempotency key
        if (payment.idempotencyKey != null) {
            Payment existing = paymentRepository.findByIdempotencyKey(payment.idempotencyKey).orElse(null);
            if (existing != null) {
                return existing; // Return existing payment, idempotent behavior
            }
        }

        // Get invoice to validate
        Invoice invoice = invoiceRepository.findById(payment.invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found");
        }

        // Validate payment amount doesn't exceed amount due
        BigDecimal amountDue = invoice.totalAmount.subtract(invoice.amountPaid);
        if (payment.amount.compareTo(amountDue) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds amount due");
        }

        // Generate transaction ID if not present
        if (payment.transactionId == null || payment.transactionId.isEmpty()) {
            payment.transactionId = "TXN-" + UUID.randomUUID().toString();
        }

        // Set payment status and processed time
        payment.status = PaymentStatus.COMPLETED;
        payment.processedAt = LocalDateTime.now();

        // Persist payment
        paymentRepository.persist(payment);

        // Update invoice with payment
        invoiceService.recordPayment(invoice.id, payment.amount);

        return payment;
    }

    public Payment getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id);
        if (payment == null) {
            throw new RuntimeException("Payment not found with id: " + id);
        }
        return payment;
    }

    public Payment getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId).orElse(null);
        if (payment == null) {
            throw new RuntimeException("Payment not found with transaction ID: " + transactionId);
        }
        return payment;
    }

    public List<Payment> getPaymentsByInvoiceId(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }

    public List<Payment> getPaymentsByPatientId(Long patientId) {
        return paymentRepository.findByPatientId(patientId);
    }

    public List<Payment> getSuccessfulPayments() {
        return paymentRepository.findByStatus(PaymentStatus.COMPLETED);
    }

    public List<Payment> getFailedPayments() {
        return paymentRepository.findByStatus(PaymentStatus.FAILED);
    }

    public List<Payment> getPendingPayments() {
        return paymentRepository.findByStatus(PaymentStatus.PENDING);
    }

    @Transactional
    public Payment retryPayment(Long paymentId) {
        Payment payment = getPaymentById(paymentId);

        if (payment.status != PaymentStatus.FAILED) {
            throw new IllegalStateException("Only failed payments can be retried");
        }

        // Reset status and retry
        payment.status = PaymentStatus.PROCESSING;

        // TODO: Integrate with payment gateway
        // For now, mark as completed
        payment.status = PaymentStatus.COMPLETED;
        payment.processedAt = LocalDateTime.now();

        return payment;
    }

    public BigDecimal calculateTotalPaymentsForInvoice(Long invoiceId) {
        List<Payment> payments = paymentRepository.findByInvoiceId(invoiceId);
        return payments.stream()
                .filter(p -> p.status == PaymentStatus.COMPLETED)
                .map(p -> p.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalPaymentsForPatient(Long patientId) {
        List<Payment> payments = paymentRepository.findByPatientId(patientId);
        return payments.stream()
                .filter(p -> p.status == PaymentStatus.COMPLETED)
                .map(p -> p.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean canRefund(Long paymentId) {
        Payment payment = getPaymentById(paymentId);

        if (payment.status != PaymentStatus.COMPLETED) {
            return false;
        }

        BigDecimal refundableAmount = getRefundableAmount(paymentId);
        return refundableAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getRefundableAmount(Long paymentId) {
        Payment payment = getPaymentById(paymentId);

        if (payment.status != PaymentStatus.COMPLETED) {
            return BigDecimal.ZERO;
        }

        return payment.amount.subtract(payment.refundedAmount);
    }

    public List<Payment> getRefundablePayments(Long invoiceId) {
        List<Payment> payments = getPaymentsByInvoiceId(invoiceId);
        return payments.stream()
                .filter(p -> p.status == PaymentStatus.COMPLETED)
                .filter(p -> p.refundedAmount.compareTo(p.amount) < 0)
                .toList();
    }

    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return paymentRepository.findByIdempotencyKey(idempotencyKey).isPresent();
    }
}
