package com.example.constant;

/**
 * Represents the lifecycle status of an appointment.
 *
 * Status Flow:
 * SCHEDULED → CONFIRMED → CHECKED_IN → IN_PROGRESS → COMPLETED
 * Any status (except COMPLETED) can transition to CANCELLED or NO_SHOW
 */
public enum AppointmentStatus {
    /**
     * Initial state when appointment is created
     */
    SCHEDULED,

    /**
     * Patient has confirmed the appointment
     */
    CONFIRMED,

    /**
     * Patient has checked in at the facility
     */
    CHECKED_IN,

    /**
     * Appointment is currently in progress
     */
    IN_PROGRESS,

    /**
     * Appointment was completed successfully
     */
    COMPLETED,

    /**
     * Appointment was cancelled by patient or doctor
     */
    CANCELLED,

    /**
     * Patient did not show up for the appointment
     */
    NO_SHOW
}



