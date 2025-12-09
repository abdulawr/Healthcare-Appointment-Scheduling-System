package com.basit.cz.dto;

import jakarta.validation.constraints.*;

/**
 * Request DTO for creating a doctor review
 */
public class CreateReviewRequest {

    @NotNull(message = "Patient ID is required")
    @Positive(message = "Patient ID must be positive")
    public Long patientId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    public Integer rating;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    public String comment;

    public String patientName; // Optional: for caching patient name

    // Default constructor
    public CreateReviewRequest() {
    }

    // Constructor with all fields
    public CreateReviewRequest(Long patientId, Integer rating, String comment, String patientName) {
        this.patientId = patientId;
        this.rating = rating;
        this.comment = comment;
        this.patientName = patientName;
    }
}



