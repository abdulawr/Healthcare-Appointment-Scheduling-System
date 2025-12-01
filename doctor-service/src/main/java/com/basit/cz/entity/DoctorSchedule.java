package com.basit.cz.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Doctor Schedule Entity
 *
 * Represents special schedules, vacations, and time-off for doctors.
 * Used to block out unavailable periods.
 *
 * Relationship:
 * - Many-to-One with Doctor
 */
@Entity
@Table(name = "doctor_schedules", indexes = {
        @Index(name = "idx_schedules_doctor_id", columnList = "doctor_id"),
        @Index(name = "idx_schedules_dates", columnList = "start_date, end_date"),
        @Index(name = "idx_schedules_type", columnList = "schedule_type")
})
public class DoctorSchedule extends PanacheEntity {

    /**
     * Schedule Type Enum
     */
    public enum ScheduleType {
        VACATION,       // Planned vacation
        SICK_LEAVE,     // Sick leave
        CONFERENCE,     // Medical conference
        TRAINING,       // Training or education
        EMERGENCY_LEAVE,// Emergency time off
        OTHER          // Other reasons
    }

    /**
     * Schedule Status Enum
     */
    public enum ScheduleStatus {
        PENDING,    // Awaiting approval
        APPROVED,   // Approved
        REJECTED,   // Rejected
        CANCELLED   // Cancelled by doctor
    }

    @NotNull(message = "Doctor is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    public Doctor doctor;

    @NotNull(message = "Schedule type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 20)
    public ScheduleType scheduleType;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    public LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    public LocalDate endDate;

    @Column(name = "reason", length = 500)
    public String reason;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public ScheduleStatus status = ScheduleStatus.PENDING;

    @Column(name = "approved_by")
    public String approvedBy; // Admin user who approved

    @Column(name = "approval_date")
    public LocalDateTime approvalDate;

    @Column(name = "notes", length = 1000)
    public String notes;

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
     * Validate that end date is not before start date
     */
    private void validate() {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }

    /**
     * Check if schedule is active (covers current date)
     */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return status == ScheduleStatus.APPROVED &&
                !today.isBefore(startDate) &&
                !today.isAfter(endDate);
    }

    /**
     * Check if a specific date is covered by this schedule
     */
    public boolean coversDate(LocalDate date) {
        return status == ScheduleStatus.APPROVED &&
                !date.isBefore(startDate) &&
                !date.isAfter(endDate);
    }

    /**
     * Get duration in days
     */
    public long getDurationInDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Approve this schedule
     */
    public void approve(String approver) {
        this.status = ScheduleStatus.APPROVED;
        this.approvedBy = approver;
        this.approvalDate = LocalDateTime.now();
    }

    /**
     * Reject this schedule
     */
    public void reject(String rejectedBy) {
        this.status = ScheduleStatus.REJECTED;
        this.approvedBy = rejectedBy;
        this.approvalDate = LocalDateTime.now();
    }

    /**
     * Cancel this schedule
     */
    public void cancel() {
        this.status = ScheduleStatus.CANCELLED;
    }

    /**
     * Check if schedule is pending approval
     */
    public boolean isPending() {
        return status == ScheduleStatus.PENDING;
    }

    /**
     * Check if schedule is approved
     */
    public boolean isApproved() {
        return status == ScheduleStatus.APPROVED;
    }
}



