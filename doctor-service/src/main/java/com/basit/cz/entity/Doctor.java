package com.basit.cz.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a Doctor in the healthcare system
 */
@Entity
@Table(name = "doctors")
public class Doctor extends PanacheEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    public String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    public String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    public String email;

    @Column(name = "phone_number", length = 20)
    public String phoneNumber;

    @Column(name = "specialization", nullable = false, length = 100)
    public String specialization;

    @Column(name = "years_of_experience")
    public Integer yearsOfExperience;

    @Column(name = "license_number", unique = true, length = 50)
    public String licenseNumber;

    @Column(name = "consultation_fee")
    public Double consultationFee;

    @Column(name = "bio", columnDefinition = "TEXT")
    public String bio;

    @Column(name = "qualifications", length = 500)
    public String qualifications;

    @Column(name = "average_rating")
    public Double averageRating;

    @Column(name = "total_reviews")
    public Integer totalReviews;

    @Column(name = "is_active")
    public Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    // One-to-many relationship with DoctorAvailability
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<DoctorAvailability> availabilities = new ArrayList<>();

    /**
     * Get full name of the doctor
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return null;
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (averageRating == null) {
            averageRating = 0.0;
        }
        if (totalReviews == null) {
            totalReviews = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
















