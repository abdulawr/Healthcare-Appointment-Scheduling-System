package com.basit.cz.dto;

import com.basit.cz.entity.DoctorReview;

public class ReviewMapper {

    /**
     * Convert DoctorReview entity to ReviewDTO
     */
    public static ReviewDTO toDTO(DoctorReview review) {
        if (review == null) {
            return null;
        }

        return new ReviewDTO(
                review.id,
                review.doctor != null ? review.doctor.id : null,
                review.doctor != null ? review.doctor.getFullName() : null,
                review.patientId,
                review.patientName,
                review.rating,
                review.comment,
                review.isVerified,
                review.appointmentDate,
                review.createdAt,
                review.updatedAt,
                review.getRatingCategory()
        );
    }
}










