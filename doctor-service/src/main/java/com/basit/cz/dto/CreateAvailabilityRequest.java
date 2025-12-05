package com.basit.cz.dto;  // Or com.healthcare.doctor.dto

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

/**
 * Request DTO for creating/updating doctor availability
 */
public class CreateAvailabilityRequest {

    @NotBlank(message = "Day of week is required")
    public String dayOfWeek;  // String, not DayOfWeek enum!

    @NotNull(message = "Start time is required")
    public LocalTime startTime;

    @NotNull(message = "End time is required")
    public LocalTime endTime;

    public Boolean isActive = true;

    // Empty constructor
    public CreateAvailabilityRequest() {
    }

    // Constructor with 3 parameters
    public CreateAvailabilityRequest(String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}