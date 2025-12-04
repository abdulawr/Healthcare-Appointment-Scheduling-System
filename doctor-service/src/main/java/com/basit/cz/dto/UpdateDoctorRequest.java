package com.basit.cz.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;

/**
 * Update Doctor Request DTO
 *
 * Used when updating doctor information.
 * All fields are optional (only provided fields will be updated).
 */
public class UpdateDoctorRequest {

    public String firstName;
    public String lastName;

    @Email(message = "Email should be valid")
    public String email;

    public String phoneNumber;
    public String specialization;

    @Min(value = 0, message = "Years of experience cannot be negative")
    public Integer yearsOfExperience;

    public String licenseNumber;
    public String qualifications;
    public String bio;

    @Min(value = 0, message = "Consultation fee cannot be negative")
    public Double consultationFee;
}









