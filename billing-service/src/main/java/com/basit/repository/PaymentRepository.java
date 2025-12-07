package com.basit.repository;

import com.basit.constant.PaymentMethodType;
import com.basit.constant.PaymentStatus;
import com.basit.entity.Payment;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Payment entity with custom query methods
 */
@ApplicationScoped
public class PaymentRepository implements PanacheRepository<Payment> {

    /**
     * Find payment by transaction ID
     */
    public Optional<Payment> findByTransactionId(String transactionId) {
        return find("transactionId", transactionId).firstResultOptional();
    }

    /**
     * Find payment by idempotency key
     */
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return find("idempotencyKey", idempotencyKey).firstResultOptional();
    }

    /**
     * Find all payments for an invoice
     */
    public List<Payment> findByInvoiceId(Long invoiceId) {
        return find("invoiceId", Sort.descending("createdAt"), invoiceId).list();
    }

    /**
     * Find all payments for a patient
     */
    public List<Payment> findByPatientId(Long patientId) {
        return find("patientId", Sort.descending("createdAt"), patientId).list();
    }

    /**
     * Find all payments for a patient with pagination
     */
    public List<Payment> findByPatientId(Long patientId, Page page) {
        return find("patientId", Sort.descending("createdAt"), patientId)
                .page(page)
                .list();
    }

    /**
     * Find payments by status
     */
    public List<Payment> findByStatus(PaymentStatus status) {
        return find("status", Sort.descending("createdAt"), status).list();
    }

    /**
     * Find payments by payment method
     */
    public List<Payment> findByPaymentMethod(PaymentMethodType paymentMethod) {
        return find("paymentMethod", Sort.descending("createdAt"), paymentMethod).list();
    }

    /**
     * Find payments by payment gateway
     */
    public List<Payment> findByPaymentGateway(String gateway) {
        return find("paymentGateway", Sort.descending("createdAt"), gateway).list();
    }

    /**
     * Find successful payments
     */
    public List<Payment> findSuccessfulPayments() {
        return find("status = ?1", Sort.descending("processedAt"), PaymentStatus.COMPLETED).list();
    }

    /**
     * Find failed payments
     */
    public List<Payment> findFailedPayments() {
        return find("status = ?1", Sort.descending("createdAt"), PaymentStatus.FAILED).list();
    }

    /**
     * Find pending payments
     */
    public List<Payment> findPendingPayments() {
        return find("status in (?1, ?2)", Sort.ascending("createdAt"),
                PaymentStatus.PENDING, PaymentStatus.PROCESSING)
                .list();
    }

    /**
     * Find refundable payments
     */
    public List<Payment> findRefundablePayments() {
        return find("isRefundable = true and status = ?1 and refundedAmount < amount",
                PaymentStatus.COMPLETED)
                .list();
    }

    /**
     * Find payments processed within date range
     */
    public List<Payment> findProcessedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return find("processedAt between ?1 and ?2",
                Sort.descending("processedAt"), startDate, endDate)
                .list();
    }

    /**
     * Calculate total payments for an invoice
     */
    public BigDecimal calculateTotalForInvoice(Long invoiceId) {
        return find("select sum(p.amount) from Payment p where p.invoiceId = ?1 and p.status = ?2",
                invoiceId, PaymentStatus.COMPLETED)
                .project(BigDecimal.class)
                .firstResult();
    }

    /**
     * Calculate total payments for a patient
     */
    public BigDecimal calculateTotalForPatient(Long patientId) {
        return find("select sum(p.amount) from Payment p where p.patientId = ?1 and p.status = ?2",
                patientId, PaymentStatus.COMPLETED)
                .project(BigDecimal.class)
                .firstResult();
    }

    /**
     * Count payments by status
     */
    public long countByStatus(PaymentStatus status) {
        return count("status", status);
    }

    /**
     * Count failed payments for a patient
     */
    public long countFailedPaymentsByPatient(Long patientId) {
        return count("patientId = ?1 and status = ?2", patientId, PaymentStatus.FAILED);
    }

    /**
     * Find payments with amount greater than specified
     */
    public List<Payment> findByAmountGreaterThan(BigDecimal amount) {
        return find("amount > ?1", Sort.descending("amount"), amount).list();
    }

    /**
     * Find recently processed payments (last N days)
     */
    public List<Payment> findRecentlyProcessed(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return find("processedAt >= ?1", Sort.descending("processedAt"), since).list();
    }

    /**
     * Find payments that can be refunded
     */
    public List<Payment> findEligibleForRefund() {
        return find("status = ?1 and isRefundable = true and refundedAmount < amount",
                Sort.descending("processedAt"), PaymentStatus.COMPLETED)
                .list();
    }

    /**
     * Find payments by saved payment method
     */
    public List<Payment> findByPaymentMethodId(Long paymentMethodId) {
        return find("paymentMethodId", Sort.descending("createdAt"), paymentMethodId).list();
    }

    /**
     * Check if payment exists with idempotency key
     */
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return count("idempotencyKey", idempotencyKey) > 0;
    }
}

