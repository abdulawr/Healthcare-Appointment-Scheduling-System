package com.basit.entity;

import com.basit.constant.ClaimStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "insurance_claims")
public class InsuranceClaim extends PanacheEntity {

    @Column(nullable = false)
    public Long invoiceId;

    @Column(nullable = false)
    public Long patientId;

    @Column(nullable = false, length = 100)
    public String insuranceProvider;

    @Column(nullable = false, length = 100)
    public String policyNumber;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal claimedAmount;

    @Column(precision = 10, scale = 2)
    public BigDecimal approvedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ClaimStatus status = ClaimStatus.SUBMITTED;

    @Column(unique = true, length = 255)
    public String claimNumber;

    @Column(nullable = false)
    public LocalDateTime submissionDate;

    public LocalDateTime approvalDate;

    @Column(length = 1000)
    public String notes;

    @PrePersist
    public void prePersist() {
        if (submissionDate == null) {
            submissionDate = LocalDateTime.now();
        }
    }
}
