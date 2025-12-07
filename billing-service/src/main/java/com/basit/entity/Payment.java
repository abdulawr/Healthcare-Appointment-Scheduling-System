package com.basit.entity;

import com.basit.constant.PaymentMethodType;
import com.basit.constant.PaymentStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a payment transaction for an invoice.
 * Supports multiple payment gateways and methods.
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_invoice", columnList = "invoice_id"),
        @Index(name = "idx_payment_patient", columnList = "patient_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_payment_idempotency_key", columnList = "idempotency_key", unique = true)
})
public class Payment extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull(message = "Invoice ID is required")
    @Column(name = "invoice_id", nullable = false)
    public Long invoiceId;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    public Long patientId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal amount;

    @NotNull(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    public PaymentMethodType paymentMethod;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    public PaymentStatus status;

    @NotBlank(message = "Transaction ID is required")
    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    public String transactionId;

    @NotBlank(message = "Payment gateway is required")
    @Column(name = "payment_gateway", nullable = false, length = 50)
    public String paymentGateway;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    public String gatewayResponse;

    @Column(name = "payment_method_id")
    public Long paymentMethodId;

    @NotBlank(message = "Idempotency key is required")
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    public String idempotencyKey;

    @Column(name = "processed_at")
    public LocalDateTime processedAt;

    @Column(name = "failed_reason", length = 500)
    public String failedReason;

    @Column(length = 1000)
    public String notes;

    @Column(name = "refunded_amount", precision = 10, scale = 2)
    public BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(name = "is_refundable")
    public Boolean isRefundable = true;

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
            status = PaymentStatus.PENDING;
        }
        if (idempotencyKey == null) {
            idempotencyKey = UUID.randomUUID().toString();
        }
        if (refundedAmount == null) {
            refundedAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Marks the payment as completed
     */
    public void markCompleted() {
        this.status = PaymentStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Marks the payment as failed with a reason
     */
    public void markFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failedReason = reason;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Records a refund for this payment
     */
    public void recordRefund(BigDecimal refundAmount) {
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than zero");
        }

        BigDecimal totalRefunded = this.refundedAmount.add(refundAmount);
        if (totalRefunded.compareTo(this.amount) > 0) {
            throw new IllegalArgumentException("Total refund amount cannot exceed payment amount");
        }

        this.refundedAmount = totalRefunded;

        if (this.refundedAmount.compareTo(this.amount) == 0) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
    }

    /**
     * Checks if the payment is successful
     */
    public boolean isSuccessful() {
        return status == PaymentStatus.COMPLETED;
    }

    /**
     * Checks if the payment can be refunded
     */
    public boolean canBeRefunded() {
        return isRefundable &&
                status == PaymentStatus.COMPLETED &&
                refundedAmount.compareTo(amount) < 0;
    }

    /**
     * Gets the refundable amount
     */
    public BigDecimal getRefundableAmount() {
        return amount.subtract(refundedAmount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id) &&
                Objects.equals(transactionId, payment.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, transactionId);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", invoiceId=" + invoiceId +
                ", amount=" + amount +
                ", paymentMethod=" + paymentMethod +
                ", status=" + status +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}
