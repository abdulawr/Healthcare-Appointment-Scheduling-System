package com.basit.cz.repository;

import com.basit.cz.entity.Doctor;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

/**
 * Doctor Repository
 *
 * Provides custom queries for Doctor entity beyond basic CRUD.
 * Uses Panache for simplified data access.
 *
 * Custom Queries:
 * - Find by specialization
 * - Search by name
 * - Find active doctors
 * - Find by minimum rating
 * - Find by email
 * - And more...
 */
@ApplicationScoped
public class DoctorRepository implements PanacheRepository<Doctor> {

    @Inject
    EntityManager entityManager;

    /**
     * Find all active doctors
     *
     * @return List of active doctors
     */
    public List<Doctor> findAllActive() {
        return list("isActive", true);
    }

    /**
     * Find doctor by email
     *
     * @param email Doctor's email address
     * @return Optional containing doctor if found
     */
    public Optional<Doctor> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    /**
     * Find doctors by specialization
     *
     * @param specialization Medical specialization
     * @return List of doctors with matching specialization
     */
    public List<Doctor> findBySpecialization(String specialization) {
        return list("LOWER(specialization) = LOWER(?1) AND isActive = true", specialization);
    }

    /**
     * Search doctors by name (first name or last name)
     * Case-insensitive search
     *
     * @param searchTerm Search term
     * @return List of matching doctors
     */
    public List<Doctor> searchByName(String searchTerm) {
        String pattern = "%" + searchTerm.toLowerCase() + "%";
        return list("(LOWER(firstName) LIKE ?1 OR LOWER(lastName) LIKE ?1) AND isActive = true",
                pattern);
    }

    /**
     * Find doctors with minimum rating
     *
     * @param minRating Minimum average rating (e.g., 4.0)
     * @return List of doctors with rating >= minRating
     */
    public List<Doctor> findByMinimumRating(double minRating) {
        return list("averageRating >= ?1 AND isActive = true ORDER BY averageRating DESC",
                minRating);
    }

    /**
     * Find doctors by specialization with minimum rating
     *
     * @param specialization Medical specialization
     * @param minRating Minimum average rating
     * @return List of doctors matching both criteria
     */
    public List<Doctor> findBySpecializationAndRating(String specialization, double minRating) {
        return list("LOWER(specialization) = LOWER(?1) AND averageRating >= ?2 AND isActive = true " +
                        "ORDER BY averageRating DESC",
                specialization, minRating);
    }

    /**
     * Find top-rated doctors (rating >= 4.0)
     *
     * @return List of top-rated doctors
     */
    public List<Doctor> findTopRated() {
        return findByMinimumRating(4.0);
    }

    /**
     * Find doctors by minimum years of experience
     *
     * @param minYears Minimum years of experience
     * @return List of experienced doctors
     */
    public List<Doctor> findByMinimumExperience(int minYears) {
        return list("yearsOfExperience >= ?1 AND isActive = true ORDER BY yearsOfExperience DESC",
                minYears);
    }

    /**
     * Find doctors with reviews (has at least one review)
     *
     * @return List of doctors who have been reviewed
     */
    public List<Doctor> findDoctorsWithReviews() {
        return entityManager.createQuery(
                        "SELECT DISTINCT d FROM Doctor d " +
                                "WHERE d.totalReviews > 0 AND d.isActive = true " +
                                "ORDER BY d.averageRating DESC",
                        Doctor.class)
                .getResultList();
    }

    /**
     * Find doctors available on specific day
     * (Have availability slots for that day)
     *
     * @param dayOfWeek Day of week (e.g., MONDAY)
     * @return List of available doctors
     */
    public List<Doctor> findAvailableOnDay(String dayOfWeek) {
        return entityManager.createQuery(
                        "SELECT DISTINCT d FROM Doctor d " +
                                "JOIN d.availabilitySlots a " +
                                "WHERE a.dayOfWeek = :day AND a.isActive = true AND d.isActive = true " +
                                "ORDER BY d.averageRating DESC",
                        Doctor.class)
                .setParameter("day", java.time.DayOfWeek.valueOf(dayOfWeek.toUpperCase()))
                .getResultList();
    }

    /**
     * Count doctors by specialization
     *
     * @param specialization Medical specialization
     * @return Number of active doctors in this specialization
     */
    public long countBySpecialization(String specialization) {
        return count("LOWER(specialization) = LOWER(?1) AND isActive = true", specialization);
    }

    /**
     * Get all unique specializations
     *
     * @return List of distinct specializations
     */
    public List<String> getAllSpecializations() {
        return entityManager.createQuery(
                        "SELECT DISTINCT d.specialization FROM Doctor d " +
                                "WHERE d.isActive = true " +
                                "ORDER BY d.specialization",
                        String.class)
                .getResultList();
    }

    /**
     * Find doctors by license number
     *
     * @param licenseNumber Medical license number
     * @return Optional containing doctor if found
     */
    public Optional<Doctor> findByLicenseNumber(String licenseNumber) {
        return find("licenseNumber", licenseNumber).firstResultOptional();
    }

    /**
     * Find doctors with availability slots
     * (Have at least one availability slot defined)
     *
     * @return List of doctors with availability
     */
    public List<Doctor> findDoctorsWithAvailability() {
        return entityManager.createQuery(
                        "SELECT DISTINCT d FROM Doctor d " +
                                "LEFT JOIN FETCH d.availabilitySlots a " +
                                "WHERE a IS NOT NULL AND a.isActive = true AND d.isActive = true",
                        Doctor.class)
                .getResultList();
    }

    /**
     * Find doctors by consultation fee range
     *
     * @param minFee Minimum consultation fee
     * @param maxFee Maximum consultation fee
     * @return List of doctors within fee range
     */
    public List<Doctor> findByConsultationFeeRange(double minFee, double maxFee) {
        return list("consultationFee >= ?1 AND consultationFee <= ?2 AND isActive = true " +
                        "ORDER BY consultationFee ASC",
                minFee, maxFee);
    }

    /**
     * Get doctor statistics
     * Returns array: [totalDoctors, averageRating, averageExperience]
     *
     * @return Object array with statistics
     */
    public Object[] getDoctorStatistics() {
        return entityManager.createQuery(
                        "SELECT COUNT(d), AVG(d.averageRating), AVG(d.yearsOfExperience) " +
                                "FROM Doctor d WHERE d.isActive = true",
                        Object[].class)
                .getSingleResult();
    }
}




