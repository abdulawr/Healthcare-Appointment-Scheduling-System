package com.basit.entity;

import com.basit.constant.RefundStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a refund transaction for a payment.
 */
@Entity
@Table(name = "refunds", indexes = {
        @Index(name = "idx_refund_payment", columnList = "payment_id"),
        @Index(name = "idx_refund_invoice", columnList = "invoice_id"),
        @Index(name = "idx_refund_status", columnList = "status"),
        @Index(name = "idx_refund_transaction_id", columnList = "refund_transaction_id")
})
public class Refund extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull(message = "Payment ID is required")
    @Column(name = "payment_id", nullable = false)
    public Long paymentId;

    @NotNull(message = "Invoice ID is required")
    @Column(name = "invoice_id", nullable = false)
    public Long invoiceId;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    public Long patientId;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Refund amount must be greater than 0")
    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    public BigDecimal refundAmount;

    @NotNull(message = "Refund status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    public RefundStatus status;

    @NotBlank(message = "Reason is required")
    @Column(nullable = false, length = 1000)
    public String reason;

    @Column(name = "refund_transaction_id", unique = true, length = 100)
    public String refundTransactionId;

    @Column(name = "payment_gateway", length = 50)
    public String paymentGateway;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    public String gatewayResponse;

    @Column(name = "processed_at")
    public LocalDateTime processedAt;

    @Column(name = "failed_reason", length = 500)
    public String failedReason;

    @Column(name = "requested_by", length = 100)
    public String requestedBy;

    @Column(name = "approved_by", length = 100)
    public String approvedBy;

    @Column(name = "approved_at")
    public LocalDateTime approvedAt;

    @Column(length = 1000)
    public String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @Version
    public Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = RefundStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Marks the refund as completed
     */
    public void markCompleted() {
        this.status = RefundStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Marks the refund as failed with a reason
     */
    public void markFailed(String reason) {
        this.status = RefundStatus.FAILED;
        this.failedReason = reason;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Approves the refund request
     */
    public void approve(String approver) {
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
        this.status = RefundStatus.PROCESSING;
    }

    /**
     * Checks if the refund is completed
     */
    public boolean isCompleted() {
        return status == RefundStatus.COMPLETED;
    }

    /**
     * Checks if the refund requires approval
     */
    public boolean requiresApproval() {
        return status == RefundStatus.REQUIRES_REVIEW;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Refund)) return false;
        Refund refund = (Refund) o;
        return Objects.equals(id, refund.id) &&
                Objects.equals(refundTransactionId, refund.refundTransactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, refundTransactionId);
    }

    @Override
    public String toString() {
        return "Refund{" +
                "id=" + id +
                ", paymentId=" + paymentId +
                ", refundAmount=" + refundAmount +
                ", status=" + status +
                ", refundTransactionId='" + refundTransactionId + '\'' +
                '}';
    }
}
