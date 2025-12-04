package com.basit.cz.repository;

import com.healthcare.doctor.entity.DoctorReview;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Doctor Review Repository
 *
 * Provides custom queries for DoctorReview entity.
 * Handles queries related to patient reviews and ratings.
 */
@ApplicationScoped
public class DoctorReviewRepository implements PanacheRepository<DoctorReview> {

    @Inject
    EntityManager entityManager;

    /**
     * Find all reviews for a doctor
     *
     * @param doctorId Doctor's ID
     * @return List of reviews ordered by creation date (newest first)
     */
    public List<DoctorReview> findByDoctorId(Long doctorId) {
        return list("doctor.id = ?1 ORDER BY createdAt DESC", doctorId);
    }

    /**
     * Find reviews by patient
     *
     * @param patientId Patient's ID
     * @return List of reviews by this patient
     */
    public List<DoctorReview> findByPatientId(Long patientId) {
        return list("patientId = ?1 ORDER BY createdAt DESC", patientId);
    }

    /**
     * Find reviews by rating
     *
     * @param doctorId Doctor's ID
     * @param rating Rating (1-5)
     * @return List of reviews with this rating
     */
    public List<DoctorReview> findByDoctorAndRating(Long doctorId, int rating) {
        return list("doctor.id = ?1 AND rating = ?2 ORDER BY createdAt DESC",
                doctorId, rating);
    }

    /**
     * Find verified reviews for a doctor
     *
     * @param doctorId Doctor's ID
     * @return List of verified reviews
     */
    public List<DoctorReview> findVerifiedByDoctor(Long doctorId) {
        return list("doctor.id = ?1 AND isVerified = true ORDER BY createdAt DESC",
                doctorId);
    }

    /**
     * Find positive reviews (rating >= 4)
     *
     * @param doctorId Doctor's ID
     * @return List of positive reviews
     */
    public List<DoctorReview> findPositiveReviews(Long doctorId) {
        return list("doctor.id = ?1 AND rating >= 4 ORDER BY createdAt DESC",
                doctorId);
    }

    /**
     * Find negative reviews (rating <= 2)
     *
     * @param doctorId Doctor's ID
     * @return List of negative reviews
     */
    public List<DoctorReview> findNegativeReviews(Long doctorId) {
        return list("doctor.id = ?1 AND rating <= 2 ORDER BY createdAt DESC",
                doctorId);
    }

    /**
     * Get average rating for a doctor
     *
     * @param doctorId Doctor's ID
     * @return Average rating (or 0.0 if no reviews)
     */
    public Double getAverageRating(Long doctorId) {
        Double avg = entityManager.createQuery(
                        "SELECT AVG(r.rating) FROM DoctorReview r WHERE r.doctor.id = :doctorId",
                        Double.class)
                .setParameter("doctorId", doctorId)
                .getSingleResult();

        return avg != null ? avg : 0.0;
    }

    /**
     * Count reviews by rating for a doctor
     * Returns array: [1-star count, 2-star, 3-star, 4-star, 5-star]
     *
     * @param doctorId Doctor's ID
     * @return Array of counts by rating
     */
    public long[] countByRating(Long doctorId) {
        long[] counts = new long[5];
        for (int rating = 1; rating <= 5; rating++) {
            counts[rating - 1] = count("doctor.id = ?1 AND rating = ?2", doctorId, rating);
        }
        return counts;
    }

    /**
     * Find recent reviews (last N reviews)
     *
     * @param doctorId Doctor's ID
     * @param limit Number of reviews to return
     * @return List of recent reviews
     */
    public List<DoctorReview> findRecentReviews(Long doctorId, int limit) {
        return entityManager.createQuery(
                        "SELECT r FROM DoctorReview r " +
                                "WHERE r.doctor.id = :doctorId " +
                                "ORDER BY r.createdAt DESC",
                        DoctorReview.class)
                .setParameter("doctorId", doctorId)
                .setMaxResults(limit)
                .getResultList();
    }
}
