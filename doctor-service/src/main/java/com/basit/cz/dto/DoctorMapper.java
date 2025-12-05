package com.basit.cz.dto;

import com.basit.cz.entity.Doctor;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper class to convert between Doctor entity and DTOs
 */
public class DoctorMapper {

    /**
     * Convert Doctor entity to DoctorDTO
     */
    public static DoctorDTO toDTO(Doctor doctor) {
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
        dto.consultationFee = doctor.consultationFee;
        dto.bio = doctor.bio;
        dto.qualifications = doctor.qualifications;
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
    public static Doctor toEntity(CreateDoctorRequest request) {
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
        doctor.consultationFee = request.consultationFee;
        doctor.bio = request.bio;
        doctor.qualifications = request.qualifications;

        return doctor;
    }

    /**
     * Update Doctor entity from UpdateDoctorRequest
     */
    public static void updateEntity(Doctor doctor, UpdateDoctorRequest request) {
        if (doctor == null || request == null) {
            return;
        }

        if (request.phoneNumber != null) {
            doctor.phoneNumber = request.phoneNumber;
        }
        if (request.consultationFee != null) {
            doctor.consultationFee = request.consultationFee;
        }
        if (request.bio != null) {
            doctor.bio = request.bio;
        }
        if (request.qualifications != null) {
            doctor.qualifications = request.qualifications;
        }
    }
}












