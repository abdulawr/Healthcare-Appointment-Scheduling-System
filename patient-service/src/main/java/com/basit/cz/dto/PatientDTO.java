package com.basit.cz.dto;

import com.basit.cz.entity.Patient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTOs (Data Transfer Objects) for Patient API.
 *
 * DTOs separate the API layer from the database entities.
 * This allows us to:
 * - Control what data is exposed in the API
 * - Add validation specific to API requests
 * - Change the database structure without breaking the API
 */
public class PatientDTO {

    /**
     * Request DTO for registering a new patient.
     * Contains all required fields for patient registration.
     */
    public static class RegistrationRequest {

        @NotBlank(message = "First name is required")
        public String firstName;

        @NotBlank(message = "Last name is required")
        public String lastName;

        @Email(message = "Email must be valid")
        @NotBlank(message = "Email is required")
        public String email;

        @NotBlank(message = "Phone number is required")
        public String phoneNumber;

        @Past(message = "Date of birth must be in the past")
        public LocalDate dateOfBirth;

        public Patient.Gender gender;

        // Optional fields
        public String address;
        public String emergencyContactName;
        public String emergencyContactPhone;

        @Override
        public String toString() {
            return "RegistrationRequest{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }

    /**
     * Request DTO for updating an existing patient.
     * Similar to RegistrationRequest but for updates.
     */
    public static class UpdateRequest {

        @NotBlank(message = "First name is required")
        public String firstName;

        @NotBlank(message = "Last name is required")
        public String lastName;

        @Email(message = "Email must be valid")
        @NotBlank(message = "Email is required")
        public String email;

        @NotBlank(message = "Phone number is required")
        public String phoneNumber;

        @Past(message = "Date of birth must be in the past")
        public LocalDate dateOfBirth;

        public Patient.Gender gender;
        public String address;
        public String emergencyContactName;
        public String emergencyContactPhone;

        @Override
        public String toString() {
            return "UpdateRequest{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }

    /**
     * Response DTO for returning patient data.
     * Contains all patient information for API responses.
     */
    public static class Response {

        public Long id;
        public String firstName;
        public String lastName;
        public String email;
        public String phoneNumber;
        public LocalDate dateOfBirth;
        public Patient.Gender gender;
        public String address;
        public String emergencyContactName;
        public String emergencyContactPhone;
        public Boolean isActive;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        /**
         * Convert Patient entity to Response DTO.
         * This is a static factory method.
         */
        public static Response fromEntity(Patient patient) {
            Response response = new Response();
            response.id = patient.id;
            response.firstName = patient.firstName;
            response.lastName = patient.lastName;
            response.email = patient.email;
            response.phoneNumber = patient.phoneNumber;
            response.dateOfBirth = patient.dateOfBirth;
            response.gender = patient.gender;
            response.address = patient.address;
            response.emergencyContactName = patient.emergencyContactName;
            response.emergencyContactPhone = patient.emergencyContactPhone;
            response.isActive = patient.isActive;
            response.createdAt = patient.createdAt;
            response.updatedAt = patient.updatedAt;
            return response;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "id=" + id +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", email='" + email + '\'' +
                    ", isActive=" + isActive +
                    '}';
        }
    }
}



