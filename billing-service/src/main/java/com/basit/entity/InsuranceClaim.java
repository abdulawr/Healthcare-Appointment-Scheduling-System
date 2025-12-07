package com.basit.entity;

import com.basit.constant.ClaimStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an insurance claim submitted for an invoice.
 */
@Entity
@Table(name = "insurance_claims", indexes = {
        @Index(name = "idx_claim_invoice", columnList = "invoice_id"),
        @Index(name = "idx_claim_patient", columnList = "patient_id"),
        @Index(name = "idx_claim_status", columnList = "status"),
        @Index(name = "idx_claim_number", columnList = "claim_number")
})
public class InsuranceClaim extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull(message = "Invoice ID is required")
    @Column(name = "invoice_id", nullable = false)
    public Long invoiceId;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    public Long patientId;

    @NotBlank(message = "Claim number is required")
    @Column(name = "claim_number", unique = true, nullable = false, length = 50)
    public String claimNumber;

    @NotBlank(message = "Insurance provider is required")
    @Column(name = "insurance_provider", nullable = false, length = 200)
    public String insuranceProvider;

    @NotBlank(message = "Policy number is required")
    @Column(name = "policy_number", nullable = false, length = 100)
    public String policyNumber;

    @Column(name = "group_number", length = 100)
    public String groupNumber;

    @NotNull(message = "Claim amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Claim amount must be greater than 0")
    @Column(name = "claim_amount", nullable = false, precision = 10, scale = 2)
    public BigDecimal claimAmount;

    @Column(name = "approved_amount", precision = 10, scale = 2)
    public BigDecimal approvedAmount;

    @Column(name = "paid_amount", precision = 10, scale = 2)
    public BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "patient_responsibility", precision = 10, scale = 2)
    public BigDecimal patientResponsibility;

    @NotNull(message = "Claim status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    public ClaimStatus status;

    @NotNull(message = "Submission date is required")
    @Column(name = "submission_date", nullable = false)
    public LocalDate submissionDate;

    @Column(name = "processed_date")
    public LocalDate processedDate;

    @Column(name = "paid_date")
    public LocalDate paidDate;

    @Column(name = "denial_reason", length = 1000)
    public String denialReason;

    @Column(name = "diagnosis_codes", length = 500)
    public String diagnosisCodes;

    @Column(name = "procedure_codes", length = 500)
    public String procedureCodes;

    @Column(name = "provider_npi", length = 20)
    public String providerNPI;

    @Column(name = "facility_code", length = 50)
    public String facilityCode;

    @Column(length = 2000)
    public String notes;

    @Column(name = "external_claim_id", length = 100)
    public String externalClaimId;

    @Column(name = "appeal_count")
    public Integer appealCount = 0;

    @Column(name = "last_appeal_date")
    public LocalDate lastAppealDate;

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
            status = ClaimStatus.DRAFT;
        }
        if (appealCount == null) {
            appealCount = 0;
        }
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Submits the claim to insurance
     */
    public void submit() {
        if (status != ClaimStatus.DRAFT) {
            throw new IllegalStateException("Only draft claims can be submitted");
        }
        this.status = ClaimStatus.SUBMITTED;
        this.submissionDate = LocalDate.now();
    }

    /**
     * Approves the claim with an approved amount
     */
    public void approve(BigDecimal approvedAmount) {
        if (approvedAmount == null || approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Approved amount must be greater than zero");
        }
        this.status = ClaimStatus.APPROVED;
        this.approvedAmount = approvedAmount;
        this.processedDate = LocalDate.now();

        // Calculate patient responsibility
        if (approvedAmount.compareTo(claimAmount) < 0) {
            this.patientResponsibility = claimAmount.subtract(approvedAmount);
        } else {
            this.patientResponsibility = BigDecimal.ZERO;
        }
    }

    /**
     * Denies the claim with a reason
     */
    public void deny(String reason) {
        this.status = ClaimStatus.DENIED;
        this.denialReason = reason;
        this.processedDate = LocalDate.now();
        this.patientResponsibility = claimAmount;
    }

    /**
     * Records payment received from insurance
     */
    public void recordPayment(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        this.paidAmount = this.paidAmount.add(amount);
        if (this.paidAmount.compareTo(this.approvedAmount) >= 0) {
            this.status = ClaimStatus.PAID;
            this.paidDate = LocalDate.now();
        }
    }

    /**
     * Appeals the denied claim
     */
    public void appeal() {
        if (status != ClaimStatus.DENIED) {
            throw new IllegalStateException("Only denied claims can be appealed");
        }
        this.status = ClaimStatus.APPEALED;
        this.appealCount++;
        this.lastAppealDate = LocalDate.now();
    }

    /**
     * Checks if the claim is approved
     */
    public boolean isApproved() {
        return status == ClaimStatus.APPROVED || status == ClaimStatus.PAID;
    }

    /**
     * Checks if the claim is paid in full
     */
    public boolean isPaidInFull() {
        return status == ClaimStatus.PAID &&
                approvedAmount != null &&
                paidAmount.compareTo(approvedAmount) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InsuranceClaim)) return false;
        InsuranceClaim that = (InsuranceClaim) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(claimNumber, that.claimNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, claimNumber);
    }

    @Override
    public String toString() {
        return "InsuranceClaim{" +
                "id=" + id +
                ", claimNumber='" + claimNumber + '\'' +
                ", insuranceProvider='" + insuranceProvider + '\'' +
                ", claimAmount=" + claimAmount +
                ", status=" + status +
                '}';
    }
}