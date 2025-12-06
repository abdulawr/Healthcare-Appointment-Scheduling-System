package com.example.constant;

/**
 * Represents the type/category of medical appointment.
 * Helps in scheduling, resource allocation, and statistics.
 */
public enum AppointmentType {
    /**
     * General medical consultation
     */
    CONSULTATION,

    /**
     * Follow-up appointment after initial consultation or treatment
     */
    FOLLOW_UP,

    /**
     * Routine health checkup or preventive care
     */
    ROUTINE_CHECKUP,

    /**
     * Emergency or urgent care appointment
     */
    EMERGENCY,

    /**
     * Surgical procedure
     */
    SURGERY,

    /**
     * Diagnostic tests (X-ray, MRI, blood tests, etc.)
     */
    DIAGNOSTIC,

    /**
     * Therapy session (physical therapy, counseling, etc.)
     */
    THERAPY,

    /**
     * Vaccination appointment
     */
    VACCINATION
}



