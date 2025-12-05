package com.basit.cz.repository;

import com.basit.cz.entity.Doctor;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repository for Doctor entity with custom query methods
 */
@ApplicationScoped
public class DoctorRepository implements PanacheRepository<Doctor> {

    // ===============================================
    // EXISTENCE CHECKS
    // ===============================================

    /**
     * Check if email already exists
     */
    public boolean existsByEmail(String email) {
        return count("LOWER(email) = LOWER(?1)", email) > 0;
    }

    /**
     * Check if license number exists
     */
    public boolean existsByLicenseNumber(String licenseNumber) {
        return count("licenseNumber = ?1", licenseNumber) > 0;
    }

    // ===============================================
    // BASIC QUERIES
    // ===============================================

    /**
     * Find doctor by email
     */
    public Doctor findByEmail(String email) {
        return find("LOWER(email) = LOWER(?1)", email).firstResult();
    }

    /**
     * Find all active doctors
     */
    public List<Doctor> findActiveDoctors() {
        return find("isActive = true ORDER BY firstName, lastName").list();
    }

    /**
     * Find all inactive doctors
     */
    public List<Doctor> findInactiveDoctors() {
        return find("isActive = false ORDER BY firstName, lastName").list();
    }

    // ===============================================
    // SEARCH QUERIES
    // ===============================================

    /**
     * Search doctors by name (first name or last name)
     */
    public List<Doctor> searchByName(String searchTerm) {
        String pattern = "%" + searchTerm.toLowerCase() + "%";
        return find("LOWER(firstName) LIKE ?1 OR LOWER(lastName) LIKE ?1 AND isActive = true ORDER BY firstName",
                pattern).list();
    }

    /**
     * Find doctors by specialization
     */
    public List<Doctor> findBySpecialization(String specialization) {
        return find("specialization = ?1 AND isActive = true ORDER BY averageRating DESC",
                specialization).list();
    }

    /**
     * Find doctors by specialization (case-insensitive)
     */
    public List<Doctor> findBySpecializationIgnoreCase(String specialization) {
        return find("LOWER(specialization) = LOWER(?1) AND isActive = true ORDER BY averageRating DESC",
                specialization).list();
    }

    // ===============================================
    // RATING QUERIES
    // ===============================================

    /**
     * Find top-rated doctors (rating >= 4.0)
     */
    public List<Doctor> findTopRated() {
        return find("averageRating >= 4.0 AND isActive = true ORDER BY averageRating DESC, totalReviews DESC")
                .list();
    }

    /**
     * Find doctors with minimum rating
     */
    public List<Doctor> findByMinimumRating(double minRating) {
        return find("averageRating >= ?1 AND isActive = true ORDER BY averageRating DESC", minRating).list();
    }

    /**
     * Find doctors by specialization and minimum rating
     */
    public List<Doctor> findBySpecializationAndRating(String specialization, double minRating) {
        return find("specialization = ?1 AND averageRating >= ?2 AND isActive = true ORDER BY averageRating DESC",
                specialization, minRating).list();
    }

    // ===============================================
    // EXPERIENCE QUERIES
    // ===============================================

    /**
     * Find doctors by minimum years of experience
     */
    public List<Doctor> findByMinimumExperience(int minYears) {
        return find("yearsOfExperience >= ?1 AND isActive = true ORDER BY yearsOfExperience DESC", minYears).list();
    }

    /**
     * Find doctors by experience range
     */
    public List<Doctor> findByExperienceRange(int minYears, int maxYears) {
        return find("yearsOfExperience >= ?1 AND yearsOfExperience <= ?2 AND isActive = true ORDER BY yearsOfExperience DESC",
                minYears, maxYears).list();
    }

    // ===============================================
    // CONSULTATION FEE QUERIES
    // ===============================================

    /**
     * Find doctors by consultation fee range
     */
    public List<Doctor> findByConsultationFeeRange(double minFee, double maxFee) {
        return find("consultationFee >= ?1 AND consultationFee <= ?2 AND isActive = true ORDER BY consultationFee",
                minFee, maxFee).list();
    }

    /**
     * Find doctors with fee less than
     */
    public List<Doctor> findByMaxConsultationFee(double maxFee) {
        return find("consultationFee <= ?1 AND isActive = true ORDER BY consultationFee", maxFee).list();
    }

    // ===============================================
    // AVAILABILITY QUERIES
    // ===============================================

    /**
     * Find doctors available on a specific day
     */
    public List<Doctor> findAvailableOnDay(String dayOfWeek) {
        return find("SELECT DISTINCT d FROM Doctor d " +
                "JOIN d.availabilities a " +
                "WHERE a.dayOfWeek = ?1 AND a.isActive = true AND d.isActive = true " +
                "ORDER BY d.firstName", dayOfWeek).list();
    }

    /**
     * Find doctors with availability
     */
    public List<Doctor> findDoctorsWithAvailability() {
        return find("SELECT DISTINCT d FROM Doctor d " +
                "JOIN d.availabilities a " +
                "WHERE a.isActive = true AND d.isActive = true " +
                "ORDER BY d.firstName").list();
    }

    // ===============================================
    // AGGREGATION QUERIES
    // ===============================================

    /**
     * Count active doctors
     */
    public long countActiveDoctors() {
        return count("isActive = true");
    }

    /**
     * Count doctors by specialization
     */
    public long countBySpecialization(String specialization) {
        return count("specialization = ?1 AND isActive = true", specialization);
    }

    /**
     * Get all unique specializations
     */
    public List<String> getAllSpecializations() {
        return find("SELECT DISTINCT d.specialization FROM Doctor d WHERE d.isActive = true ORDER BY d.specialization")
                .project(String.class)
                .list();
    }

    /**
     * Calculate average rating of all doctors
     */
    public double calculateAverageRating() {
        Double avg = find("SELECT AVG(d.averageRating) FROM Doctor d WHERE d.isActive = true")
                .project(Double.class)
                .firstResult();
        return avg != null ? avg : 0.0;
    }

    /**
     * Calculate average experience
     */
    public double calculateAverageExperience() {
        Double avg = find("SELECT AVG(d.yearsOfExperience) FROM Doctor d WHERE d.isActive = true")
                .project(Double.class)
                .firstResult();
        return avg != null ? avg : 0.0;
    }

    /**
     * Get doctor statistics (total, avg rating, avg experience)
     */
    public Object[] getDoctorStatistics() {
        return (Object[]) find("SELECT " +
                "COUNT(d), " +
                "AVG(d.averageRating), " +
                "AVG(d.yearsOfExperience) " +
                "FROM Doctor d WHERE d.isActive = true")
                .project(Object[].class)
                .firstResult();
    }

    // ===============================================
    // COMPLEX QUERIES
    // ===============================================

    /**
     * Find doctors by multiple criteria
     */
    public List<Doctor> findByCriteria(String specialization, Double minRating, Integer minExperience,
                                       Double maxFee) {
        StringBuilder query = new StringBuilder("isActive = true");

        if (specialization != null && !specialization.isEmpty()) {
            query.append(" AND specialization = '").append(specialization).append("'");
        }
        if (minRating != null) {
            query.append(" AND averageRating >= ").append(minRating);
        }
        if (minExperience != null) {
            query.append(" AND yearsOfExperience >= ").append(minExperience);
        }
        if (maxFee != null) {
            query.append(" AND consultationFee <= ").append(maxFee);
        }

        query.append(" ORDER BY averageRating DESC, yearsOfExperience DESC");

        return find(query.toString()).list();
    }

    /**
     * Search doctors with filters
     */
    public List<Doctor> searchWithFilters(String searchTerm, String specialization, Double minRating) {
        StringBuilder query = new StringBuilder();
        query.append("isActive = true");

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            query.append(" AND (LOWER(firstName) LIKE '").append(pattern).append("'")
                    .append(" OR LOWER(lastName) LIKE '").append(pattern).append("')");
        }

        if (specialization != null && !specialization.isEmpty()) {
            query.append(" AND specialization = '").append(specialization).append("'");
        }

        if (minRating != null) {
            query.append(" AND averageRating >= ").append(minRating);
        }

        query.append(" ORDER BY averageRating DESC");

        return find(query.toString()).list();
    }

    // ===============================================
    // SORTING QUERIES
    // ===============================================

    /**
     * Find all doctors sorted by rating
     */
    public List<Doctor> findAllSortedByRating() {
        return find("isActive = true ORDER BY averageRating DESC, totalReviews DESC").list();
    }

    /**
     * Find all doctors sorted by experience
     */
    public List<Doctor> findAllSortedByExperience() {
        return find("isActive = true ORDER BY yearsOfExperience DESC, averageRating DESC").list();
    }

    /**
     * Find all doctors sorted by name
     */
    public List<Doctor> findAllSortedByName() {
        return find("isActive = true ORDER BY lastName, firstName").list();
    }

    /**
     * Find all doctors sorted by consultation fee
     */
    public List<Doctor> findAllSortedByFee() {
        return find("isActive = true ORDER BY consultationFee, averageRating DESC").list();
    }
}












