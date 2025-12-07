package com.basit.service;

import com.basit.constant.RefundStatus;
import com.basit.entity.Payment;
import com.basit.entity.Refund;
import com.basit.repository.PaymentRepository;
import com.basit.repository.RefundRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for Refund business logic
 */
@ApplicationScoped
public class RefundService {

    @Inject
    RefundRepository refundRepository;

    @Inject
    PaymentRepository paymentRepository;

    /**
     * Create a refund request
     */
    @Transactional
    public Refund createRefund(Refund refund) {
        // Get the payment
        Payment payment = paymentRepository.findById(refund.paymentId);
        if (payment == null) {
            throw new RuntimeException("Payment not found with id: " + refund.paymentId);
        }

        // Validate refund can be created
        if (!payment.canBeRefunded()) {
            throw new IllegalStateException("Payment cannot be refunded");
        }

        // Validate refund amount
        BigDecimal refundableAmount = payment.getRefundableAmount();
        if (refund.refundAmount.compareTo(refundableAmount) > 0) {
            throw new IllegalArgumentException(
                    "Refund amount exceeds refundable amount: " + refundableAmount);
        }

        // Set initial status
        refund.status = RefundStatus.PENDING;

        // Check if requires approval (e.g., refunds > $500)
        if (refund.refundAmount.compareTo(new BigDecimal("500.00")) > 0) {
            refund.status = RefundStatus.REQUIRES_REVIEW;
        }

        // Persist refund (onCreate() will be called automatically by @PrePersist)
        refundRepository.persist(refund);

        return refund;
    }

    /**
     * Get refund by ID
     */
    public Refund getRefundById(Long id) {
        return refundRepository.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Refund not found with id: " + id));
    }

    /**
     * Get refund by transaction ID
     */
    public Refund getRefundByTransactionId(String refundTransactionId) {
        return refundRepository.findByRefundTransactionId(refundTransactionId)
                .orElseThrow(() -> new RuntimeException("Refund not found with transaction ID: " + refundTransactionId));
    }

    /**
     * Get all refunds for a payment
     */
    public List<Refund> getRefundsByPaymentId(Long paymentId) {
        return refundRepository.findByPaymentId(paymentId);
    }

    /**
     * Get all refunds for a patient
     */
    public List<Refund> getRefundsByPatientId(Long patientId) {
        return refundRepository.findByPatientId(patientId);
    }

    /**
     * Get pending refunds
     */
    public List<Refund> getPendingRefunds() {
        return refundRepository.findPendingRefunds();
    }

    /**
     * Get refunds requiring approval
     */
    public List<Refund> getRefundsRequiringApproval() {
        return refundRepository.findRequiringApproval();
    }

    /**
     * Approve refund
     */
    @Transactional
    public Refund approveRefund(Long refundId, String approvedBy) {
        Refund refund = getRefundById(refundId);

        if (!refund.requiresApproval()) {
            throw new IllegalStateException("Refund does not require approval");
        }

        refund.approve(approvedBy);

        // Process the refund
        return processRefund(refundId);
    }

    /**
     * Process refund
     */
    @Transactional
    public Refund processRefund(Long refundId) {
        Refund refund = getRefundById(refundId);

        if (refund.status != RefundStatus.PENDING && refund.status != RefundStatus.PROCESSING) {
            throw new IllegalStateException("Refund cannot be processed in current status: " + refund.status);
        }

        refund.status = RefundStatus.PROCESSING;

        try {
            // TODO: Integrate with payment gateway for refund
            // For now, simulate successful refund
            refund.markCompleted();
            refund.refundTransactionId = "REFUND-" + UUID.randomUUID().toString();
            refund.processedAt = LocalDateTime.now();

            // Update payment
            Payment payment = paymentRepository.findById(refund.paymentId);
            payment.recordRefund(refund.refundAmount);

        } catch (Exception e) {
            refund.markFailed("Refund processing failed: " + e.getMessage());
            throw new RuntimeException("Refund failed", e);
        }

        return refund;
    }

    /**
     * Reject/Cancel refund
     */
    @Transactional
    public Refund cancelRefund(Long refundId, String reason) {
        Refund refund = getRefundById(refundId);

        if (refund.status == RefundStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed refund");
        }

        refund.status = RefundStatus.CANCELLED;
        refund.notes = (refund.notes != null ? refund.notes + "; " : "") + "CANCELLED: " + reason;

        return refund;
    }

    /**
     * Get completed refunds
     */
    public List<Refund> getCompletedRefunds() {
        return refundRepository.findCompletedRefunds();
    }

    /**
     * Get failed refunds
     */
    public List<Refund> getFailedRefunds() {
        return refundRepository.findFailedRefunds();
    }

    /**
     * Calculate total refunded for payment
     */
    public BigDecimal calculateTotalRefundedForPayment(Long paymentId) {
        BigDecimal total = refundRepository.calculateTotalRefundedForPayment(paymentId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Calculate total refunded for patient
     */
    public BigDecimal calculateTotalRefundedForPatient(Long patientId) {
        BigDecimal total = refundRepository.calculateTotalRefundedForPatient(patientId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Get recently created refunds
     */
    public List<Refund> getRecentlyCreatedRefunds(int days) {
        return refundRepository.findRecentlyCreated(days);
    }

    /**
     * Get unapproved refunds older than specified hours
     */
    public List<Refund> getUnapprovedRefundsOlderThan(int hours) {
        return refundRepository.findUnapprovedOlderThan(hours);
    }

    /**
     * Get all refunds
     */
    public List<Refund> getAllRefunds() {
        return refundRepository.listAll();
    }

    /**
     * Count refunds by status
     */
    public long countByStatus(RefundStatus status) {
        return refundRepository.countByStatus(status);
    }

    /**
     * Retry failed refund
     */
    @Transactional
    public Refund retryRefund(Long refundId) {
        Refund refund = getRefundById(refundId);

        if (refund.status != RefundStatus.FAILED) {
            throw new IllegalStateException("Only failed refunds can be retried");
        }

        return processRefund(refundId);
    }
}
