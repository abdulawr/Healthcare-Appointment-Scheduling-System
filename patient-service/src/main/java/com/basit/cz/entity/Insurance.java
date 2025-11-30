package com.basit.cz.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "insurance")
public class Insurance extends PanacheEntity {

    @OneToOne
    @JoinColumn(name = "patient_id", nullable = false)
    public Patient patient;

    @Column(name = "provider_name", nullable = false)
    public String providerName;

    @Column(name = "policy_number", unique = true, nullable = false)
    public String policyNumber;

    @Column(name = "group_number")
    public String groupNumber;

    @Column(name = "policy_holder_name", nullable = false)
    public String policyHolderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_holder_relationship", nullable = false)
    public PolicyHolderRelationship policyHolderRelationship;

    @Column(name = "coverage_start_date")
    public LocalDate coverageStartDate;

    @Column(name = "coverage_end_date")
    public LocalDate coverageEndDate;

    @Column(name = "copay_amount")
    public Double copayAmount;

    @Column(name = "deductible_amount")
    public Double deductibleAmount;

    @Column(name = "is_active", nullable = false)
    public Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public enum PolicyHolderRelationship {
        SELF, SPOUSE, PARENT, CHILD, OTHER
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if coverage is currently active
     */
    public boolean isCoverageActive() {
        if (!isActive) return false;
        LocalDate now = LocalDate.now();
        if (coverageStartDate != null && now.isBefore(coverageStartDate)) return false;
        if (coverageEndDate != null && now.isAfter(coverageEndDate)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Insurance{" +
                "id=" + id +
                ", providerName='" + providerName + '\'' +
                ", policyNumber='" + policyNumber + '\'' +
                '}';
    }
}



