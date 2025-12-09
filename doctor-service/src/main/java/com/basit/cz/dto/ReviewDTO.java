package com.basit.cz.dto;

import java.time.LocalDateTime;

/**
 * DTO for Doctor Review response
 */
public class ReviewDTO {

    public Long id;
    public Long doctorId;
    public String doctorName;
    public Long patientId;
    public String patientName;
    public Integer rating;
    public String comment;
    public Boolean isVerified;
    public LocalDateTime appointmentDate;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public String ratingCategory; // POSITIVE, NEUTRAL, NEGATIVE

    // Default constructor
    public ReviewDTO() {
    }

    // Full constructor
    public ReviewDTO(Long id, Long doctorId, String doctorName, Long patientId,
                     String patientName, Integer rating, String comment,
                     Boolean isVerified, LocalDateTime appointmentDate,
                     LocalDateTime createdAt, LocalDateTime updatedAt,
                     String ratingCategory) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.rating = rating;
        this.comment = comment;
        this.isVerified = isVerified;
        this.appointmentDate = appointmentDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.ratingCategory = ratingCategory;
    }
}
