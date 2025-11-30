package com.basit.cz.service;

import com.basit.cz.dto.PatientDTO;
import com.basit.cz.entity.Patient;
import com.basit.cz.exception.DuplicateEmailException;
import com.basit.cz.exception.PatientNotFoundException;
import com.basit.cz.repository.PatientRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Patient operations.
 *
 * This layer contains business logic and orchestrates:
 * - Validation
 * - Repository calls
 * - DTO conversions
 * - Exception handling
 */
@ApplicationScoped
public class PatientService {

    private static final Logger LOG = Logger.getLogger(PatientService.class);

    @Inject
    PatientRepository patientRepository;

    /**
     * Register a new patient.
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
     * Get a patient by ID.
     * Only returns active patients.
     *
     * @param id Patient ID
     * @return Response DTO with patient data
     * @throws PatientNotFoundException if patient not found or inactive
     */
    public PatientDTO.Response getPatient(Long id) {
        LOG.infof("Getting patient with id: %d", id);

        Patient patient = patientRepository.findActiveById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        return PatientDTO.Response.fromEntity(patient);
    }

    /**
     * Update an existing patient.
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
     * Deactivate a patient (soft delete).
     * The patient record remains in database but is marked as inactive.
     *
     * @param id Patient ID to deactivate
     * @throws PatientNotFoundException if patient not found
     */
    @Transactional
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
     * Search for patients by name.
     * Searches both first and last names (case-insensitive).
     * Only returns active patients.
     *
     * @param searchTerm Name to search for
     * @return List of matching patients
     */
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
     * Get all active patients.
     *
     * @return List of all active patients
     */
    public List<PatientDTO.Response> getAllActivePatients() {
        LOG.info("Getting all active patients");

        List<Patient> patients = patientRepository.findAllActive();

        return patients.stream()
                .map(PatientDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get count of active patients.
     *
     * @return Number of active patients
     */
    public long getActivePatientCount() {
        return patientRepository.countActive();
    }
}



