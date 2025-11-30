package com.basit.cz.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "patients")
public class Patient extends PanacheEntity {

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    public String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    public String lastName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(unique = true, nullable = false)
    public String email;

    @NotBlank(message = "Phone number is required")
    @Column(name = "phone_number", nullable = false)
    public String phoneNumber;

    @Column(name = "date_of_birth")
    public LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Gender gender = Gender.OTHER;

    @Column(length = 500)
    public String address;

    @Column(name = "emergency_contact_name", length = 100)
    public String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    public String emergencyContactPhone;

    @Column(name = "is_active", nullable = false)
    public Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    // Relationships
    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    public Insurance insurance;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<MedicalRecord> medicalRecords = new java.util.ArrayList<>();

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    public CommunicationPreference communicationPreference;

    public enum Gender {
        MALE, FEMALE, OTHER
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

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}






