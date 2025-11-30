package com.basit.cz.repository;

import com.basit.cz.entity.Patient;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Patient entity with custom query methods.
 *
 * PanacheRepository provides built-in methods:
 * - findAll() - Get all patients
 * - findById(Long id) - Find by ID
 * - persist(Patient) - Save patient
 * - delete(Patient) - Delete patient
 * - count() - Count all patients
 *
 * This repository adds custom queries for specific needs.
 */
@ApplicationScoped
public class PatientRepository implements PanacheRepository<Patient> {

    /**
     * Find a patient by their email address.
     * Email is unique in the database.
     *
     * @param email - Patient's email
     * @return Optional containing patient if found
     */
    public Optional<Patient> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    /**
     * Find a patient by their phone number.
     *
     * @param phoneNumber - Patient's phone number
     * @return Optional containing patient if found
     */
    public Optional<Patient> findByPhoneNumber(String phoneNumber) {
        return find("phoneNumber", phoneNumber).firstResultOptional();
    }

    /**
     * Search for patients by name (first or last name).
     * Uses case-insensitive LIKE query.
     *
     * Example: searchByName("john") finds "John Doe", "Johnny", etc.
     *
     * @param searchTerm - Name to search for
     * @return List of matching patients
     */
    public List<Patient> searchByName(String searchTerm) {
        String pattern = "%" + searchTerm.toLowerCase() + "%";
        return list("LOWER(firstName) LIKE ?1 OR LOWER(lastName) LIKE ?1", pattern);
    }

    /**
     * Find all active patients only.
     * Active patients have isActive = true.
     *
     * @return List of active patients
     */
    public List<Patient> findAllActive() {
        return list("isActive", true);
    }

    /**
     * Find all inactive patients.
     * Inactive patients have isActive = false (soft deleted).
     *
     * @return List of inactive patients
     */
    public List<Patient> findAllInactive() {
        return list("isActive", false);
    }

    /**
     * Find active patient by ID.
     * Only returns the patient if they are active.
     *
     * @param id - Patient ID
     * @return Optional containing active patient if found
     */
    public Optional<Patient> findActiveById(Long id) {
        return find("id = ?1 AND isActive = true", id).firstResultOptional();
    }

    /**
     * Count active patients.
     *
     * @return Number of active patients
     */
    public long countActive() {
        return count("isActive", true);
    }

    /**
     * Count inactive patients.
     *
     * @return Number of inactive patients
     */
    public long countInactive() {
        return count("isActive", false);
    }

    /**
     * Check if email already exists.
     * Useful for validation before creating new patient.
     *
     * @param email - Email to check
     * @return true if email exists
     */
    public boolean emailExists(String email) {
        return count("email", email) > 0;
    }

    /**
     * Check if phone number already exists.
     *
     * @param phoneNumber - Phone number to check
     * @return true if phone number exists
     */
    public boolean phoneNumberExists(String phoneNumber) {
        return count("phoneNumber", phoneNumber) > 0;
    }

    /**
     * Find patients by gender.
     *
     * @param gender - Gender to search for
     * @return List of patients with specified gender
     */
    public List<Patient> findByGender(Patient.Gender gender) {
        return list("gender", gender);
    }
}
