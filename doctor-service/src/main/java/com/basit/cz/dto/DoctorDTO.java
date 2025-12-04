package com.basit.cz.dto;

import java.time.LocalDateTime;

/**
 * Doctor Response DTO
 *
 * Used for returning doctor information to clients.
 * Contains all doctor details including ratings and status.
 */
public class DoctorDTO {

    public Long id;
    public String firstName;
    public String lastName;
    public String fullName;
    public String email;
    public String phoneNumber;
    public String specialization;
    public Integer yearsOfExperience;
    public String licenseNumber;
    public String qualifications;
    public String bio;
    public Double consultationFee;
    public Double averageRating;
    public Integer totalReviews;
    public Boolean isActive;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    // Constructors
    public DoctorDTO() {}

    public DoctorDTO(Long id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.fullName = firstName + " " + lastName;
    }
}









