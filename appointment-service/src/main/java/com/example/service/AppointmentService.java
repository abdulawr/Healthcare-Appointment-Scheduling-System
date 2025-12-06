package com.example.service;

import java.time.LocalDateTime;
import java.util.List;

import com.example.constant.AppointmentStatus;
import com.example.dto.*;

/**
 * Service interface for appointment management operations.
 * Defines the contract for all appointment-related business logic.
 */
public interface AppointmentService {

    /**
     * Create a new appointment
     * @param request appointment details
     * @return created appointment
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if doctor is not available
     */
    AppointmentResponse createAppointment(CreateAppointmentRequest request);

    /**
     * Get appointment by ID
     * @param id appointment ID
     * @return appointment details
     * @throws jakarta.ws.rs.NotFoundException if appointment not found
     */
    AppointmentResponse getAppointment(Long id);

    /**
     * Reschedule an existing appointment
     * @param id appointment ID
     * @param newStartTime new start time
     * @param newEndTime new end time
     * @return updated appointment
     * @throws jakarta.ws.rs.NotFoundException if appointment not found
     * @throws IllegalStateException if appointment cannot be rescheduled or time not available
     */
    AppointmentResponse rescheduleAppointment(Long id, LocalDateTime newStartTime, LocalDateTime newEndTime);

    /**
     * Cancel an appointment
     * @param id appointment ID
     * @param reason cancellation reason
     * @throws jakarta.ws.rs.NotFoundException if appointment not found
     */
    void cancelAppointment(Long id, String reason);

    /**
     * Confirm an appointment
     * @param id appointment ID
     * @return updated appointment
     * @throws jakarta.ws.rs.NotFoundException if appointment not found
     * @throws IllegalStateException if appointment cannot be confirmed
     */
    AppointmentResponse confirmAppointment(Long id);

    /**
     * Check-in patient for appointment
     * @param id appointment ID
     * @return updated appointment
     * @throws jakarta.ws.rs.NotFoundException if appointment not found
     * @throws IllegalStateException if appointment not ready for check-in
     */
    AppointmentResponse checkInAppointment(Long id);

    /**
     * Complete an appointment
     * @param id appointment ID
     * @return updated appointment
     * @throws jakarta.ws.rs.NotFoundException if appointment not found
     * @throws IllegalStateException if appointment not ready for completion
     */
    AppointmentResponse completeAppointment(Long id);

    /**
     * Get all appointments for a patient
     * @param patientId patient ID
     * @return list of appointments
     */
    List<AppointmentResponse> getPatientAppointments(Long patientId);

    /**
     * Get all appointments for a doctor
     * @param doctorId doctor ID
     * @return list of appointments
     */
    List<AppointmentResponse> getDoctorAppointments(Long doctorId);

    /**
     * Get all upcoming appointments
     * @return list of future appointments
     */
    List<AppointmentResponse> getUpcomingAppointments();

    /**
     * Check if doctor is available for a time slot
     * @param doctorId doctor ID
     * @param startTime slot start time
     * @param endTime slot end time
     * @return true if available, false otherwise
     */
    boolean isDoctorAvailable(Long doctorId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Get appointments by status
     * @param status appointment status
     * @return list of appointments
     */
    List<AppointmentResponse> getAppointmentsByStatus(AppointmentStatus status);
}



