package com.example.service;

import com.example.constant.AppointmentStatus;
import com.example.dto.AppointmentResponse;
import com.example.dto.CreateAppointmentRequest;
import com.example.entity.Appointment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AppointmentService with all business logic.
 */
@ApplicationScoped
public class AppointmentServiceImpl implements AppointmentService {

    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        // Validate input
        validateCreateRequest(request);

        // Check doctor availability
        if (!isDoctorAvailable(request.doctorId, request.startTime, request.endTime)) {
            throw new IllegalStateException(
                    String.format("Doctor %d is not available from %s to %s",
                            request.doctorId, request.startTime, request.endTime)
            );
        }

        // Create appointment entity
        Appointment appointment = new Appointment();
        appointment.patientId = request.patientId;
        appointment.doctorId = request.doctorId;
        appointment.startTime = request.startTime;
        appointment.endTime = request.endTime;
        appointment.type = request.type;
        appointment.reason = request.reason;
        appointment.notes = request.notes;
        appointment.status = AppointmentStatus.SCHEDULED;

        // Persist
        appointment.persist();

        return new AppointmentResponse(appointment);
    }

    @Override
    public AppointmentResponse getAppointment(Long id) {
        Appointment appointment = findAppointmentById(id);
        return new AppointmentResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse rescheduleAppointment(Long id, LocalDateTime newStartTime,
                                                     LocalDateTime newEndTime) {
        Appointment appointment = findAppointmentById(id);

        // Validate state
        if (appointment.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot reschedule a cancelled appointment");
        }
        if (appointment.status == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot reschedule a completed appointment");
        }

        // Validate new time is in future
        if (newStartTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("New start time must be in the future");
        }

        // Check if new time is available (excluding this appointment)
        if (Appointment.hasOverlappingAppointment(appointment.doctorId, newStartTime, newEndTime, id)) {
            throw new IllegalStateException(
                    String.format("Doctor %d is not available from %s to %s",
                            appointment.doctorId, newStartTime, newEndTime)
            );
        }

        // Update times
        appointment.startTime = newStartTime;
        appointment.endTime = newEndTime;
        appointment.persist();

        return new AppointmentResponse(appointment);
    }

    @Override
    @Transactional
    public void cancelAppointment(Long id, String reason) {
        Appointment appointment = findAppointmentById(id);

        appointment.status = AppointmentStatus.CANCELLED;
        appointment.cancelledAt = LocalDateTime.now();
        appointment.cancellationReason = reason;
        appointment.persist();
    }

    @Override
    @Transactional
    public AppointmentResponse confirmAppointment(Long id) {
        Appointment appointment = findAppointmentById(id);

        // Only SCHEDULED appointments can be confirmed
        if (appointment.status != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException(
                    String.format("Cannot confirm appointment with status %s. Only SCHEDULED appointments can be confirmed.",
                            appointment.status)
            );
        }

        appointment.status = AppointmentStatus.CONFIRMED;
        appointment.confirmationSent = true;
        appointment.persist();

        return new AppointmentResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse checkInAppointment(Long id) {
        Appointment appointment = findAppointmentById(id);

        // Can check-in SCHEDULED or CONFIRMED appointments
        if (appointment.status != AppointmentStatus.SCHEDULED &&
                appointment.status != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException(
                    String.format("Cannot check-in appointment with status %s", appointment.status)
            );
        }

        appointment.status = AppointmentStatus.CHECKED_IN;
        appointment.checkedInAt = LocalDateTime.now();
        appointment.persist();

        return new AppointmentResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse completeAppointment(Long id) {
        Appointment appointment = findAppointmentById(id);

        // Can complete any non-cancelled appointment
        if (appointment.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot complete a cancelled appointment");
        }
        if (appointment.status == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Appointment is already completed");
        }

        appointment.status = AppointmentStatus.COMPLETED;
        appointment.completedAt = LocalDateTime.now();
        appointment.persist();

        return new AppointmentResponse(appointment);
    }

    @Override
    public List<AppointmentResponse> getPatientAppointments(Long patientId) {
        return Appointment.findByPatientId(patientId).stream()
                .map(AppointmentResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> getDoctorAppointments(Long doctorId) {
        return Appointment.findByDoctorId(doctorId).stream()
                .map(AppointmentResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> getUpcomingAppointments() {
        return Appointment.findUpcoming(LocalDateTime.now()).stream()
                .map(AppointmentResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isDoctorAvailable(Long doctorId, LocalDateTime startTime, LocalDateTime endTime) {
        return !Appointment.hasOverlappingAppointment(doctorId, startTime, endTime, null);
    }

    @Override
    public List<AppointmentResponse> getAppointmentsByStatus(AppointmentStatus status) {
        return Appointment.findByStatus(status).stream()
                .map(AppointmentResponse::new)
                .collect(Collectors.toList());
    }

    // ==================== Private Helper Methods ====================

    /**
     * Find appointment by ID or throw NotFoundException
     */
    private Appointment findAppointmentById(Long id) {
        Appointment appointment = Appointment.findById(id);
        if (appointment == null) {
            throw new NotFoundException("Appointment not found with id: " + id);
        }
        return appointment;
    }

    /**
     * Validate create appointment request
     */
    private void validateCreateRequest(CreateAppointmentRequest request) {
        if (request.patientId == null) {
            throw new IllegalArgumentException("Patient ID is required");
        }
        if (request.doctorId == null) {
            throw new IllegalArgumentException("Doctor ID is required");
        }
        if (request.startTime == null) {
            throw new IllegalArgumentException("Start time is required");
        }
        if (request.endTime == null) {
            throw new IllegalArgumentException("End time is required");
        }
        if (request.type == null) {
            throw new IllegalArgumentException("Appointment type is required");
        }
        if (request.startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time must be in the future");
        }
        if (request.endTime.isBefore(request.startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }
}


