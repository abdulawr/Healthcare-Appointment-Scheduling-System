package com.example.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO for updating (rescheduling) an appointment
 */
public class UpdateAppointmentRequest {

    @NotNull(message = "New start time is required")
    public LocalDateTime newStartTime;

    @NotNull(message = "New end time is required")
    public LocalDateTime newEndTime;

    public String notes;

    // Default constructor
    public UpdateAppointmentRequest() {}

    // Constructor for testing
    public UpdateAppointmentRequest(LocalDateTime newStartTime, LocalDateTime newEndTime) {
        this.newStartTime = newStartTime;
        this.newEndTime = newEndTime;
    }

    public UpdateAppointmentRequest(LocalDateTime newStartTime, LocalDateTime newEndTime, String notes) {
        this.newStartTime = newStartTime;
        this.newEndTime = newEndTime;
        this.notes = notes;
    }
}



