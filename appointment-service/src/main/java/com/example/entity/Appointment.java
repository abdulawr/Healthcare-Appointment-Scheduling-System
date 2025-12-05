package com.example.entity;

import com.example.constant.AppointmentStatus;
import com.example.constant.AppointmentType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Appointment entity representing a scheduled medical appointment.
 * Uses Panache for simplified data access.
 */
@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_patient_id", columnList = "patient_id"),
        @Index(name = "idx_doctor_id", columnList = "doctor_id"),
        @Index(name = "idx_start_time", columnList = "start_time"),
        @Index(name = "idx_status", columnList = "status")
})
public class Appointment extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    public Long patientId;

    @NotNull(message = "Doctor ID is required")
    @Column(name = "doctor_id", nullable = false)
    public Long doctorId;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    public LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    public LocalDateTime endTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public AppointmentStatus status;

    @NotNull(message = "Appointment type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false, length = 30)
    public AppointmentType type;

    @Column(length = 1000)
    public String notes;

    @Column(length = 500)
    public String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Column(name = "checked_in_at")
    public LocalDateTime checkedInAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    public LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    public String cancellationReason;

    @Column(name = "reminder_sent")
    public boolean reminderSent = false;

    @Column(name = "confirmation_sent")
    public boolean confirmationSent = false;

    /**
     * Set timestamps before persisting
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = AppointmentStatus.SCHEDULED;
        }
    }

    /**
     * Update timestamp before updating
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Custom Query Methods ====================

    /**
     * Find all appointments for a specific patient
     */
    public static List<Appointment> findByPatientId(Long patientId) {
        return list("patientId", patientId);
    }

    /**
     * Find all appointments for a specific doctor
     */
    public static List<Appointment> findByDoctorId(Long doctorId) {
        return list("doctorId", doctorId);
    }

    /**
     * Find appointments by status
     */
    public static List<Appointment> findByStatus(AppointmentStatus status) {
        return list("status", status);
    }

    /**
     * Find upcoming appointments (future, not cancelled/completed)
     */
    public static List<Appointment> findUpcoming(LocalDateTime fromDate) {
        return list("startTime >= ?1 and status in (?2, ?3) order by startTime asc",
                fromDate,
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.CONFIRMED);
    }

    /**
     * Find appointments by patient and status
     */
    public static List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status) {
        return list("patientId = ?1 and status = ?2", patientId, status);
    }

    /**
     * Find appointments for a doctor within a date range
     */
    public static List<Appointment> findByDoctorIdAndDateRange(Long doctorId,
                                                               LocalDateTime start,
                                                               LocalDateTime end) {
        return list("doctorId = ?1 and startTime >= ?2 and startTime <= ?3 order by startTime asc",
                doctorId, start, end);
    }

    /**
     * Count appointments by doctor and status
     */
    public static long countByDoctorIdAndStatus(Long doctorId, AppointmentStatus status) {
        return count("doctorId = ?1 and status = ?2", doctorId, status);
    }

    /**
     * Find appointments with filters
     */
    public static List<Appointment> findWithFilters(AppointmentStatus status,
                                                    LocalDateTime startDate,
                                                    LocalDateTime endDate) {
        StringBuilder query = new StringBuilder();
        List<Object> params = new ArrayList<>();

        // Build dynamic query
        boolean hasCondition = false;

        if (status != null) {
            query.append("status = ?").append(params.size() + 1);
            params.add(status);
            hasCondition = true;
        }

        if (startDate != null) {
            if (hasCondition) query.append(" and ");
            query.append("startTime >= ?").append(params.size() + 1);
            params.add(startDate);
            hasCondition = true;
        }

        if (endDate != null) {
            if (hasCondition) query.append(" and ");
            query.append("startTime <= ?").append(params.size() + 1);
            params.add(endDate);
        }

        // If no filters, return all
        if (params.isEmpty()) {
            return listAll();
        }

        return list(query.toString(), params.toArray());
    }




    /**
     * Check if time slots overlap
     */
    public static boolean hasOverlappingAppointment(Long doctorId,
                                                    LocalDateTime start,
                                                    LocalDateTime end,
                                                    Long excludeId) {
        String query = "doctorId = ?1 and status not in (?2, ?3) and " +
                "((startTime < ?5 and endTime > ?4))";

        var appointments = excludeId != null
                ? list(query + " and id != ?6",
                doctorId, AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED,
                start, end, excludeId)
                : list(query,
                doctorId, AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED,
                start, end);

        return !appointments.isEmpty();
    }
}



