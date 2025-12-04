package com.basit.cz.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/**
 * Create Doctor Request DTO
 *
 * Used when registering a new doctor.
 * Contains validation rules for required fields.
 */
public class CreateDoctorRequest {

    @NotBlank(message = "First name is required")
    public String firstName;

    @NotBlank(message = "Last name is required")
    public String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    public String email;

    @NotBlank(message = "Phone number is required")
    public String phoneNumber;

    @NotBlank(message = "Specialization is required")
    public String specialization;

    @NotNull(message = "Years of experience is required")
    @Min(value = 0, message = "Years of experience cannot be negative")
    public Integer yearsOfExperience;

    public String licenseNumber;
    public String qualifications;
    public String bio;

    @Min(value = 0, message = "Consultation fee cannot be negative")
    public Double consultationFee;
}









