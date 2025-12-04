package com.basit.cz.dto;

import com.basit.cz.entity.Doctor;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Doctor Mapper
 *
 * Converts between Doctor entity and DTOs.
 * Handles all mapping logic in one place.
 */
@ApplicationScoped
public class DoctorMapper {

    /**
     * Convert Doctor entity to DoctorDTO
     */
    public DoctorDTO toDTO(Doctor doctor) {
        if (doctor == null) {
            return null;
        }

        DoctorDTO dto = new DoctorDTO();
        dto.id = doctor.id;
        dto.firstName = doctor.firstName;
        dto.lastName = doctor.lastName;
        dto.fullName = doctor.getFullName();
        dto.email = doctor.email;
        dto.phoneNumber = doctor.phoneNumber;
        dto.specialization = doctor.specialization;
        dto.yearsOfExperience = doctor.yearsOfExperience;
        dto.licenseNumber = doctor.licenseNumber;
        dto.qualifications = doctor.qualifications;
        dto.bio = doctor.bio;
        dto.consultationFee = doctor.consultationFee;
        dto.averageRating = doctor.averageRating;
        dto.totalReviews = doctor.totalReviews;
        dto.isActive = doctor.isActive;
        dto.createdAt = doctor.createdAt;
        dto.updatedAt = doctor.updatedAt;

        return dto;
    }

    /**
     * Convert CreateDoctorRequest to Doctor entity
     */
    public Doctor toEntity(CreateDoctorRequest request) {
        if (request == null) {
            return null;
        }

        Doctor doctor = new Doctor();
        doctor.firstName = request.firstName;
        doctor.lastName = request.lastName;
        doctor.email = request.email;
        doctor.phoneNumber = request.phoneNumber;
        doctor.specialization = request.specialization;
        doctor.yearsOfExperience = request.yearsOfExperience;
        doctor.licenseNumber = request.licenseNumber;
        doctor.qualifications = request.qualifications;
        doctor.bio = request.bio;
        doctor.consultationFee = request.consultationFee;

        return doctor;
    }

    /**
     * Update Doctor entity from UpdateDoctorRequest
     * Only updates non-null fields
     */
    public void updateEntity(Doctor doctor, UpdateDoctorRequest request) {
        if (doctor == null || request == null) {
            return;
        }

        if (request.firstName != null) {
            doctor.firstName = request.firstName;
        }
        if (request.lastName != null) {
            doctor.lastName = request.lastName;
        }
        if (request.email != null) {
            doctor.email = request.email;
        }
        if (request.phoneNumber != null) {
            doctor.phoneNumber = request.phoneNumber;
        }
        if (request.specialization != null) {
            doctor.specialization = request.specialization;
        }
        if (request.yearsOfExperience != null) {
            doctor.yearsOfExperience = request.yearsOfExperience;
        }
        if (request.licenseNumber != null) {
            doctor.licenseNumber = request.licenseNumber;
        }
        if (request.qualifications != null) {
            doctor.qualifications = request.qualifications;
        }
        if (request.bio != null) {
            doctor.bio = request.bio;
        }
        if (request.consultationFee != null) {
            doctor.consultationFee = request.consultationFee;
        }
    }
}









