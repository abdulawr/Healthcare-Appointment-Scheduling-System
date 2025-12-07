package com.basit.repository;

import com.basit.constant.RefundStatus;
import com.basit.entity.Refund;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Refund entity with custom query methods
 */
@ApplicationScoped
public class RefundRepository implements PanacheRepository<Refund> {

    /**
     * Find refund by transaction ID
     */
    public Optional<Refund> findByRefundTransactionId(String refundTransactionId) {
        return find("refundTransactionId", refundTransactionId).firstResultOptional();
    }

    /**
     * Find all refunds for a payment
     */
    public List<Refund> findByPaymentId(Long paymentId) {
        return find("paymentId", Sort.descending("createdAt"), paymentId).list();
    }

    /**
     * Find all refunds for an invoice
     */
    public List<Refund> findByInvoiceId(Long invoiceId) {
        return find("invoiceId", Sort.descending("createdAt"), invoiceId).list();
    }

    /**
     * Find all refunds for a patient
     */
    public List<Refund> findByPatientId(Long patientId) {
        return find("patientId", Sort.descending("createdAt"), patientId).list();
    }

    /**
     * Find all refunds for a patient with pagination
     */
    public List<Refund> findByPatientId(Long patientId, Page page) {
        return find("patientId", Sort.descending("createdAt"), patientId)
                .page(page)
                .list();
    }

    /**
     * Find refunds by status
     */
    public List<Refund> findByStatus(RefundStatus status) {
        return find("status", Sort.descending("createdAt"), status).list();
    }

    /**
     * Find refunds by status with pagination
     */
    public List<Refund> findByStatus(RefundStatus status, Page page) {
        return find("status", Sort.descending("createdAt"), status)
                .page(page)
                .list();
    }

    /**
     * Find pending refunds
     */
    public List<Refund> findPendingRefunds() {
        return find("status in (?1, ?2)", Sort.ascending("createdAt"),
                RefundStatus.PENDING, RefundStatus.PROCESSING)
                .list();
    }

    /**
     * Find refunds requiring review/approval
     */
    public List<Refund> findRequiringApproval() {
        return find("status = ?1", Sort.ascending("createdAt"), RefundStatus.REQUIRES_REVIEW).list();
    }

    /**
     * Find completed refunds
     */
    public List<Refund> findCompletedRefunds() {
        return find("status = ?1", Sort.descending("processedAt"), RefundStatus.COMPLETED).list();
    }

    /**
     * Find failed refunds
     */
    public List<Refund> findFailedRefunds() {
        return find("status = ?1", Sort.descending("createdAt"), RefundStatus.FAILED).list();
    }

    /**
     * Find refunds processed within date range
     */
    public List<Refund> findProcessedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return find("processedAt between ?1 and ?2",
                Sort.descending("processedAt"), startDate, endDate)
                .list();
    }

    /**
     * Calculate total refunded amount for a payment
     */
    public BigDecimal calculateTotalRefundedForPayment(Long paymentId) {
        return find("select sum(r.refundAmount) from Refund r where r.paymentId = ?1 and r.status = ?2",
                paymentId, RefundStatus.COMPLETED)
                .project(BigDecimal.class)
                .firstResult();
    }

    /**
     * Calculate total refunded amount for a patient
     */
    public BigDecimal calculateTotalRefundedForPatient(Long patientId) {
        return find("select sum(r.refundAmount) from Refund r where r.patientId = ?1 and r.status = ?2",
                patientId, RefundStatus.COMPLETED)
                .project(BigDecimal.class)
                .firstResult();
    }

    /**
     * Count refunds by status
     */
    public long countByStatus(RefundStatus status) {
        return count("status", status);
    }

    /**
     * Find refunds by payment gateway
     */
    public List<Refund> findByPaymentGateway(String gateway) {
        return find("paymentGateway", Sort.descending("createdAt"), gateway).list();
    }

    /**
     * Find refunds requested by user
     */
    public List<Refund> findByRequestedBy(String requestedBy) {
        return find("requestedBy", Sort.descending("createdAt"), requestedBy).list();
    }

    /**
     * Find refunds approved by user
     */
    public List<Refund> findByApprovedBy(String approvedBy) {
        return find("approvedBy", Sort.descending("approvedAt"), approvedBy).list();
    }

    /**
     * Find refunds with amount greater than specified
     */
    public List<Refund> findByAmountGreaterThan(BigDecimal amount) {
        return find("refundAmount > ?1", Sort.descending("refundAmount"), amount).list();
    }

    /**
     * Find recently created refunds (last N days)
     */
    public List<Refund> findRecentlyCreated(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return find("createdAt >= ?1", Sort.descending("createdAt"), since).list();
    }

    /**
     * Find unapproved refunds older than specified hours
     */
    public List<Refund> findUnapprovedOlderThan(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return find("status = ?1 and createdAt < ?2",
                Sort.ascending("createdAt"), RefundStatus.REQUIRES_REVIEW, cutoff)
                .list();
    }
}

