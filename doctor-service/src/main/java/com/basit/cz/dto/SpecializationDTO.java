package com.basit.cz.dto;

/**
 * DTO for Specialization with doctor count
 */
public class SpecializationDTO {

    public String name;
    public Long doctorCount;
    public Double averageConsultationFee;
    public Double averageRating;

    // Empty constructor
    public SpecializationDTO() {
    }

    // Constructor
    public SpecializationDTO(String name, Long doctorCount,
                             Double averageConsultationFee, Double averageRating) {
        this.name = name;
        this.doctorCount = doctorCount;
        this.averageConsultationFee = averageConsultationFee;
        this.averageRating = averageRating;
    }
}











