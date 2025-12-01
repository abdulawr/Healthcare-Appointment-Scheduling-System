package com.basit.cz.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Doctor Availability Entity
 *
 * Represents availability slots for a doctor.
 * Defines when a doctor is available for appointments.
 *
 * Relationship:
 * - Many-to-One with Doctor
 */
@Entity
@Table(name = "doctor_availability", indexes = {
        @Index(name = "idx_availability_doctor_id", columnList = "doctor_id"),
        @Index(name = "idx_availability_day", columnList = "day_of_week"),
        @Index(name = "idx_availability_active", columnList = "is_active")
})
public class DoctorAvailability extends PanacheEntity {

    @NotNull(message = "Doctor is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    public Doctor doctor;

    @NotNull(message = "Day of week is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    public DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    public LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    public LocalTime endTime;

    @Column(name = "slot_duration_minutes", nullable = false)
    public Integer slotDurationMinutes = 30; // Default 30-minute slots

    @Column(name = "is_active", nullable = false)
    public Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        validate();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        validate();
    }

    /**
     * Validate that end time is after start time
     */
    private void validate() {
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    /**
     * Check if a specific time falls within this availability slot
     */
    public boolean isAvailableAt(LocalTime time) {
        return !time.isBefore(startTime) && time.isBefore(endTime);
    }

    /**
     * Get the number of slots available in this time range
     */
    public int getTotalSlots() {
        if (startTime == null || endTime == null || slotDurationMinutes == null) {
            return 0;
        }

        int totalMinutes = (endTime.getHour() * 60 + endTime.getMinute()) -
                (startTime.getHour() * 60 + startTime.getMinute());

        return totalMinutes / slotDurationMinutes;
    }

    /**
     * Get duration in hours
     */
    public double getDurationInHours() {
        if (startTime == null || endTime == null) {
            return 0;
        }

        int totalMinutes = (endTime.getHour() * 60 + endTime.getMinute()) -
                (startTime.getHour() * 60 + startTime.getMinute());

        return totalMinutes / 60.0;
    }
}





