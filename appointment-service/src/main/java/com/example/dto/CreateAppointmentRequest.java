package com.example.dto;

import com.example.constant.AppointmentType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO for creating a new appointment
 */
public class CreateAppointmentRequest {

    @NotNull(message = "Patient ID is required")
    public Long patientId;

    @NotNull(message = "Doctor ID is required")
    public Long doctorId;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    public LocalDateTime startTime;

    @NotNull(message = "End time is required")
    public LocalDateTime endTime;

    @NotNull(message = "Appointment type is required")
    public AppointmentType type;

    public String reason;

    public String notes;

    // Default constructor for JSON deserialization
    public CreateAppointmentRequest() {}

    // Constructor for testing
    public CreateAppointmentRequest(Long patientId, Long doctorId,
                                    LocalDateTime startTime, LocalDateTime endTime,
                                    AppointmentType type, String reason) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.reason = reason;
    }

    // Full constructor
    public CreateAppointmentRequest(Long patientId, Long doctorId,
                                    LocalDateTime startTime, LocalDateTime endTime,
                                    AppointmentType type, String reason, String notes) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.reason = reason;
        this.notes = notes;
    }
}



