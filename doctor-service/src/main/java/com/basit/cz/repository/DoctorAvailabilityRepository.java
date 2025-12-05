package com.basit.cz.repository;

import com.basit.cz.entity.DoctorAvailability;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalTime;
import java.util.List;

/**
 * Repository for DoctorAvailability entity
 */
@ApplicationScoped
public class DoctorAvailabilityRepository implements PanacheRepository<DoctorAvailability> {

    /**
     * Find all availability slots for a specific doctor
     */
    public List<DoctorAvailability> findByDoctorId(Long doctorId) {
        return find("doctor.id = ?1 ORDER BY dayOfWeek, startTime", doctorId).list();
    }

    /**
     * Find active availability slots for a doctor
     */
    public List<DoctorAvailability> findActiveByDoctorId(Long doctorId) {
        return find("doctor.id = ?1 AND isActive = true ORDER BY dayOfWeek, startTime", doctorId).list();
    }

    /**
     * Find availability slots by doctor and day of week
     */
    public List<DoctorAvailability> findByDoctorIdAndDayOfWeek(Long doctorId, String dayOfWeek) {
        return find("doctor.id = ?1 AND dayOfWeek = ?2 ORDER BY startTime",
                doctorId, dayOfWeek.toUpperCase()).list();
    }

    /**
     * Find active availability slots by doctor and day
     */
    public List<DoctorAvailability> findActiveByDoctorIdAndDayOfWeek(Long doctorId, String dayOfWeek) {
        return find("doctor.id = ?1 AND dayOfWeek = ?2 AND isActive = true ORDER BY startTime",
                doctorId, dayOfWeek.toUpperCase()).list();
    }

    /**
     * Find availability by time range
     */
    public List<DoctorAvailability> findByDoctorIdAndTimeRange(Long doctorId, LocalTime startTime, LocalTime endTime) {
        return find("doctor.id = ?1 AND startTime >= ?2 AND endTime <= ?3 ORDER BY dayOfWeek, startTime",
                doctorId, startTime, endTime).list();
    }

    /**
     * Check if doctor is available on a specific day
     */
    public boolean isDoctorAvailableOnDay(Long doctorId, String dayOfWeek) {
        return count("doctor.id = ?1 AND dayOfWeek = ?2 AND isActive = true",
                doctorId, dayOfWeek.toUpperCase()) > 0;
    }

    /**
     * Delete all availability slots for a doctor
     */
    public long deleteByDoctorId(Long doctorId) {
        return delete("doctor.id = ?1", doctorId);
    }

    /**
     * Deactivate all availability slots for a doctor
     */
    public long deactivateByDoctorId(Long doctorId) {
        return update("isActive = false WHERE doctor.id = ?1", doctorId);
    }
}












