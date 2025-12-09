package com.basit.cz.service;

import com.basit.cz.dto.PatientDTO;
import com.basit.cz.entity.Patient;
import com.basit.cz.exception.DuplicateEmailException;
import com.basit.cz.exception.PatientNotFoundException;
import com.basit.cz.repository.PatientRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.faulttolerance.*;
import org.jboss.logging.Logger;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Patient operations with SELF-HEALING capabilities.
 *
 * Self-Healing Features:
 * - @Retry: Automatically retries failed operations
 * - @Timeout: Prevents operations from hanging indefinitely
 * - @CircuitBreaker: Prevents cascading failures
 * - @Fallback: Provides alternative responses when operations fail
 * - @Bulkhead: Limits concurrent executions to prevent resource exhaustion
 *
 * This layer contains business logic and orchestrates:
 * - Validation
 * - Repository calls
 * - DTO conversions
 * - Exception handling
 * - Resilience patterns
 */
@ApplicationScoped
public class PatientService {

    private static final Logger LOG = Logger.getLogger(PatientService.class);

    @Inject
    PatientRepository patientRepository;

    /**
     * Register a new patient with RETRY and TIMEOUT protection.
     *
     * Self-healing:
     * - Retries up to 3 times if database connection fails
     * - Times out after 5 seconds to prevent hanging
     *
     * Business rules:
     * - Email must be unique
     * - All required fields must be provided (validated by Bean Validation)
     *
     * @param request Registration request with patient data
     * @return Response DTO with created patient data
     * @throws DuplicateEmailException if email already exists
     */
    @Transactional
    @Retry(
            maxRetries = 3,
            delay = 500,
            delayUnit = ChronoUnit.MILLIS,
            jitter = 200,
            retryOn = {RuntimeException.class},
            abortOn = {DuplicateEmailException.class}
    )
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    public PatientDTO.Response registerPatient(PatientDTO.RegistrationRequest request) {
        LOG.infof("Registering new patient: %s", request);

        // Business rule: Check if email already exists
        if (patientRepository.emailExists(request.email)) {
            LOG.warnf("Attempted to register patient with duplicate email: %s", request.email);
            throw new DuplicateEmailException(request.email);
        }

        // Create new patient entity from request
        Patient patient = new Patient();
        patient.firstName = request.firstName;
        patient.lastName = request.lastName;
        patient.email = request.email;
        patient.phoneNumber = request.phoneNumber;
        patient.dateOfBirth = request.dateOfBirth;
        patient.gender = request.gender != null ? request.gender : Patient.Gender.OTHER;
        patient.address = request.address;
        patient.emergencyContactName = request.emergencyContactName;
        patient.emergencyContactPhone = request.emergencyContactPhone;
        patient.isActive = true;

        // Save to database
        patientRepository.persist(patient);

        LOG.infof("Patient registered successfully with id: %d", patient.id);
        return PatientDTO.Response.fromEntity(patient);
    }

    /**
     * Get a patient by ID with CIRCUIT BREAKER and FALLBACK.
     * Only returns active patients.
     *
     * Self-healing:
     * - Circuit breaker opens after 4 failures in 10 requests
     * - Falls back to cached/default response when circuit is open
     * - Times out after 3 seconds
     *
     * @param id Patient ID
     * @return Response DTO with patient data
     * @throws PatientNotFoundException if patient not found or inactive
     */
    @CircuitBreaker(
            requestVolumeThreshold = 10,
            failureRatio = 0.4,
            delay = 5000,
            delayUnit = ChronoUnit.MILLIS,
            successThreshold = 3
    )
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "getPatientFallback")
    @Retry(maxRetries = 2, delay = 200, delayUnit = ChronoUnit.MILLIS)
    public PatientDTO.Response getPatient(Long id) {
        LOG.infof("Getting patient with id: %d", id);

        Patient patient = patientRepository.findActiveById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        return PatientDTO.Response.fromEntity(patient);
    }

    /**
     * Fallback method for getPatient when circuit breaker is open
     * or operation fails.
     *
     * Returns a degraded response indicating service issues.
     */
    public PatientDTO.Response getPatientFallback(Long id) {
        LOG.warnf("Fallback activated for getPatient(%d)", id);

        // Return a minimal response or throw exception
        // In production, you might return cached data
        throw new PatientNotFoundException(id, "Patient service temporarily unavailable");
    }

    /**
     * Update an existing patient with RETRY and TIMEOUT.
     *
     * Self-healing:
     * - Retries up to 3 times on transient failures
     * - Times out after 5 seconds
     *
     * Business rules:
     * - Patient must exist and be active
     * - If email is changed, new email must not already exist
     *
     * @param id Patient ID to update
     * @param request Update request with new patient data
     * @return Response DTO with updated patient data
     * @throws PatientNotFoundException if patient not found
     * @throws DuplicateEmailException if new email already exists
     */
    @Transactional
    @Retry(
            maxRetries = 3,
            delay = 500,
            delayUnit = ChronoUnit.MILLIS,
            retryOn = {RuntimeException.class},
            abortOn = {DuplicateEmailException.class, PatientNotFoundException.class}
    )
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    public PatientDTO.Response updatePatient(Long id, PatientDTO.UpdateRequest request) {
        LOG.infof("Updating patient id: %d", id);

        // Find existing patient
        Patient patient = patientRepository.findActiveById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        // Business rule: If email is being changed, check if new email exists
        if (!patient.email.equals(request.email)) {
            if (patientRepository.emailExists(request.email)) {
                LOG.warnf("Attempted to update patient %d with duplicate email: %s", id, request.email);
                throw new DuplicateEmailException(request.email);
            }
        }

        // Update patient fields
        patient.firstName = request.firstName;
        patient.lastName = request.lastName;
        patient.email = request.email;
        patient.phoneNumber = request.phoneNumber;
        patient.dateOfBirth = request.dateOfBirth;
        patient.gender = request.gender != null ? request.gender : patient.gender;
        patient.address = request.address;
        patient.emergencyContactName = request.emergencyContactName;
        patient.emergencyContactPhone = request.emergencyContactPhone;

        // Persist updates (Panache auto-updates in transaction)
        patientRepository.persist(patient);

        LOG.infof("Patient %d updated successfully", id);
        return PatientDTO.Response.fromEntity(patient);
    }

    /**
     * Deactivate a patient (soft delete) with RETRY.
     * The patient record remains in database but is marked as inactive.
     *
     * Self-healing:
     * - Retries up to 2 times on failure
     *
     * @param id Patient ID to deactivate
     * @throws PatientNotFoundException if patient not found
     */
    @Transactional
    @Retry(maxRetries = 2, delay = 300, delayUnit = ChronoUnit.MILLIS)
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    public void deactivatePatient(Long id) {
        LOG.infof("Deactivating patient id: %d", id);

        Patient patient = patientRepository.findById(id);
        if (patient == null) {
            throw new PatientNotFoundException(id);
        }

        patient.isActive = false;
        patientRepository.persist(patient);

        LOG.infof("Patient %d deactivated successfully", id);
    }

    /**
     * Search for patients by name with BULKHEAD, CIRCUIT BREAKER, and FALLBACK.
     * Searches both first and last names (case-insensitive).
     * Only returns active patients.
     *
     * Self-healing:
     * - Bulkhead limits concurrent searches to 10
     * - Circuit breaker protects against search failures
     * - Falls back to empty list on failure
     * - Times out after 5 seconds
     *
     * @param searchTerm Name to search for
     * @return List of matching patients
     */
    @Bulkhead(value = 10, waitingTaskQueue = 20)
    @CircuitBreaker(
            requestVolumeThreshold = 10,
            failureRatio = 0.5,
            delay = 10000,
            delayUnit = ChronoUnit.MILLIS
    )
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "searchPatientsFallback")
    @Retry(maxRetries = 2, delay = 500, delayUnit = ChronoUnit.MILLIS)
    public List<PatientDTO.Response> searchPatients(String searchTerm) {
        LOG.infof("Searching patients with term: %s", searchTerm);

        List<Patient> patients = patientRepository.searchByName(searchTerm);

        // Filter to only active patients and convert to DTOs
        return patients.stream()
                .filter(p -> p.isActive)
                .map(PatientDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Fallback method for searchPatients.
     * Returns empty list when search fails.
     */
    public List<PatientDTO.Response> searchPatientsFallback(String searchTerm) {
        LOG.warnf("Fallback activated for searchPatients('%s')", searchTerm);
        LOG.warn("Returning empty search results due to service issues");
        return new ArrayList<>();
    }

    /**
     * Get all active patients with BULKHEAD and FALLBACK.
     *
     * Self-healing:
     * - Bulkhead limits concurrent requests to 5
     * - Falls back to cached/empty list on failure
     * - Times out after 10 seconds (longer for bulk operation)
     *
     * @return List of all active patients
     */
    @Bulkhead(value = 5, waitingTaskQueue = 10)
    @Timeout(value = 10, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "getAllActivePatientsFallback")
    @CircuitBreaker(
            requestVolumeThreshold = 5,
            failureRatio = 0.6,
            delay = 15000,
            delayUnit = ChronoUnit.MILLIS
    )
    public List<PatientDTO.Response> getAllActivePatients() {
        LOG.info("Getting all active patients");

        List<Patient> patients = patientRepository.findAllActive();

        return patients.stream()
                .map(PatientDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Fallback method for getAllActivePatients.
     * Returns empty list when operation fails.
     */
    public List<PatientDTO.Response> getAllActivePatientsFallback() {
        LOG.warn("Fallback activated for getAllActivePatients()");
        LOG.warn("Returning empty patient list due to service issues");
        return new ArrayList<>();
    }

    /**
     * Get count of active patients with RETRY and TIMEOUT.
     *
     * Self-healing:
     * - Retries up to 3 times
     * - Times out after 3 seconds
     * - Falls back to -1 on failure
     *
     * @return Number of active patients, or -1 if unavailable
     */
    @Retry(maxRetries = 3, delay = 200, delayUnit = ChronoUnit.MILLIS)
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "getActivePatientCountFallback")
    public long getActivePatientCount() {
        return patientRepository.countActive();
    }

    /**
     * Fallback method for getActivePatientCount.
     * Returns -1 to indicate service unavailable.
     */
    public long getActivePatientCountFallback() {
        LOG.warn("Fallback activated for getActivePatientCount()");
        return -1L;
    }
}