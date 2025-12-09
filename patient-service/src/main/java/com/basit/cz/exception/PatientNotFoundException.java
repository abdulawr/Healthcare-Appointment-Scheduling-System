package com.basit.cz.exception;

/**
 * Exception thrown when a patient is not found.
 * Enhanced to support custom messages for fallback scenarios.
 */
public class PatientNotFoundException extends RuntimeException {

    private final Long patientId;

    public PatientNotFoundException(Long patientId) {
        super(String.format("Patient not found with id: %d", patientId));
        this.patientId = patientId;
    }

    public PatientNotFoundException(Long patientId, String customMessage) {
        super(customMessage);
        this.patientId = patientId;
    }

    public Long getPatientId() {
        return patientId;
    }
}