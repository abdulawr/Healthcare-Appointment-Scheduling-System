package com.basit.cz.dto;

import java.time.LocalTime;

/**
 * DTO for Doctor Availability
 */
public class AvailabilityDTO {

    public Long id;
    public Long doctorId;
    public String doctorName;
    public String dayOfWeek;
    public LocalTime startTime;
    public LocalTime endTime;
    public Boolean isActive;

    // Empty constructor
    public AvailabilityDTO() {
    }

    // Full constructor
    public AvailabilityDTO(Long id, Long doctorId, String doctorName, String dayOfWeek,
                           LocalTime startTime, LocalTime endTime, Boolean isActive) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isActive = isActive;
    }
}










