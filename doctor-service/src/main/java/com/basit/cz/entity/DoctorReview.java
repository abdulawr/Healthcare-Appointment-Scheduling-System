package com.basit.cz.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Doctor Review Entity
 *
 * Represents a patient review and rating for a doctor.
 * Stores rating (1-5 stars) and optional comment.
 *
 * Relationship:
 * - Many-to-One with Doctor
 */
@Entity
@Table(name = "doctor_reviews", indexes = {
        @Index(name = "idx_reviews_doctor_id", columnList = "doctor_id"),
        @Index(name = "idx_reviews_patient_id", columnList = "patient_id"),
        @Index(name = "idx_reviews_rating", columnList = "rating"),
        @Index(name = "idx_reviews_created_at", columnList = "created_at")
})
public class DoctorReview extends PanacheEntity {

    @NotNull(message = "Doctor is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    public Doctor doctor;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    public Long patientId; // Reference to patient in Patient Service

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Column(name = "rating", nullable = false)
    public Integer rating;

    @Column(name = "comment", length = 1000)
    public String comment;

    @Column(name = "patient_name", length = 200)
    public String patientName; // Cached for display purposes

    @Column(name = "appointment_date")
    public LocalDateTime appointmentDate; // When the appointment was

    @Column(name = "is_verified", nullable = false)
    public Boolean isVerified = false; // Verified review after actual appointment

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if review is positive (4-5 stars)
     */
    public boolean isPositive() {
        return rating != null && rating >= 4;
    }

    /**
     * Check if review is negative (1-2 stars)
     */
    public boolean isNegative() {
        return rating != null && rating <= 2;
    }

    /**
     * Check if review is neutral (3 stars)
     */
    public boolean isNeutral() {
        return rating != null && rating == 3;
    }

    /**
     * Get rating category as string
     */
    public String getRatingCategory() {
        if (rating == null) return "UNKNOWN";
        if (rating >= 4) return "POSITIVE";
        if (rating <= 2) return "NEGATIVE";
        return "NEUTRAL";
    }

    /**
     * Verify this review
     */
    public void verify() {
        this.isVerified = true;
    }
}



