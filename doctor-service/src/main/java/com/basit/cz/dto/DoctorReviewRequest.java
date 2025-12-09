package com.basit.cz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for submitting a review to Doctor Service
 */
public class DoctorReviewRequest {

    @NotNull(message = "Patient ID is required")
    @JsonProperty("patientId")
    private Long patientId;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5.0")
    @JsonProperty("rating")
    private Double rating;

    @NotNull(message = "Comment is required")
    @Size(min = 10, max = 500, message = "Comment must be between 10 and 500 characters")
    @JsonProperty("comment")
    private String comment;

    // Constructors
    public DoctorReviewRequest() {
    }

    public DoctorReviewRequest(Long patientId, Double rating, String comment) {
        this.patientId = patientId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "DoctorReviewRequest{" +
                "patientId=" + patientId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                '}';
    }
}
