package com.basit.cz.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Doctor Entity
 *
 * Represents a medical doctor in the healthcare system.
 * Contains personal information, specialization, experience, and ratings.
 *
 * Relationships:
 * - One-to-Many with DoctorAvailability (availability slots)
 * - One-to-Many with DoctorReview (patient reviews)
 * - One-to-Many with DoctorSchedule (weekly schedules)
 */
@Entity
@Table(name = "doctors", indexes = {
        @Index(name = "idx_doctors_email", columnList = "email"),
        @Index(name = "idx_doctors_specialization", columnList = "specialization"),
        @Index(name = "idx_doctors_is_active", columnList = "is_active")
})
public class Doctor extends PanacheEntity {

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    public String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    public String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true)
    public String email;

    @NotBlank(message = "Phone number is required")
    @Column(name = "phone_number", nullable = false, length = 20)
    public String phoneNumber;

    @NotBlank(message = "Specialization is required")
    @Column(name = "specialization", nullable = false)
    public String specialization;

    @NotNull(message = "Years of experience is required")
    @Column(name = "years_of_experience", nullable = false)
    public Integer yearsOfExperience;

    @Column(name = "license_number", unique = true, length = 100)
    public String licenseNumber;

    @Column(name = "qualifications", length = 1000)
    public String qualifications;

    @Column(name = "bio", length = 2000)
    public String bio;

    @Column(name = "consultation_fee")
    public Double consultationFee;

    @Column(name = "average_rating")
    public Double averageRating = 0.0;

    @Column(name = "total_reviews")
    public Integer totalReviews = 0;

    @Column(name = "is_active", nullable = false)
    public Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<DoctorAvailability> availabilitySlots = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<DoctorReview> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<DoctorSchedule> schedules = new ArrayList<>();

    /**
     * Lifecycle callback - set createdAt timestamp before persisting
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Lifecycle callback - update updatedAt timestamp before updating
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Get full name of the doctor
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Update average rating based on reviews
     */
    public void updateRating() {
        if (reviews.isEmpty()) {
            this.averageRating = 0.0;
            this.totalReviews = 0;
            return;
        }

        double sum = reviews.stream()
                .mapToDouble(review -> review.rating)
                .sum();

        this.averageRating = Math.round((sum / reviews.size()) * 10.0) / 10.0; // Round to 1 decimal
        this.totalReviews = reviews.size();
    }

    /**
     * Check if doctor has a specific specialization
     */
    public boolean hasSpecialization(String spec) {
        return this.specialization != null &&
                this.specialization.equalsIgnoreCase(spec);
    }

    /**
     * Deactivate doctor account
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Activate doctor account
     */
    public void activate() {
        this.isActive = true;
    }
}





