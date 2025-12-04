package com.basit.cz.repository;

import com.basit.cz.entity.DoctorAvailability;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Doctor Availability Repository
 *
 * Provides custom queries for DoctorAvailability entity.
 * Handles queries related to doctor working hours and time slots.
 */
@ApplicationScoped
public class DoctorAvailabilityRepository implements PanacheRepository<DoctorAvailability> {

    @Inject
    EntityManager entityManager;

    /**
     * Find all availability slots for a doctor
     *
     * @param doctorId Doctor's ID
     * @return List of availability slots
     */
    public List<DoctorAvailability> findByDoctorId(Long doctorId) {
        return list("doctor.id = ?1 AND isActive = true ORDER BY dayOfWeek, startTime",
                doctorId);
    }

    /**
     * Find availability by doctor and day of week
     *
     * @param doctorId Doctor's ID
     * @param dayOfWeek Day of week
     * @return List of availability slots for that day
     */
    public List<DoctorAvailability> findByDoctorAndDay(Long doctorId, DayOfWeek dayOfWeek) {
        return list("doctor.id = ?1 AND dayOfWeek = ?2 AND isActive = true ORDER BY startTime",
                doctorId, dayOfWeek);
    }

    /**
     * Find availability by day of week (all doctors)
     *
     * @param dayOfWeek Day of week
     * @return List of availability slots for that day
     */
    public List<DoctorAvailability> findByDay(DayOfWeek dayOfWeek) {
        return list("dayOfWeek = ?1 AND isActive = true ORDER BY startTime", dayOfWeek);
    }

    /**
     * Find availability slots that overlap with a specific time
     *
     * @param doctorId Doctor's ID
     * @param dayOfWeek Day of week
     * @param time Time to check
     * @return List of overlapping availability slots
     */
    public List<DoctorAvailability> findOverlappingSlots(Long doctorId, DayOfWeek dayOfWeek, LocalTime time) {
        return entityManager.createQuery(
                        "SELECT a FROM DoctorAvailability a " +
                                "WHERE a.doctor.id = :doctorId " +
                                "AND a.dayOfWeek = :day " +
                                "AND a.startTime <= :time " +
                                "AND a.endTime > :time " +
                                "AND a.isActive = true",
                        DoctorAvailability.class)
                .setParameter("doctorId", doctorId)
                .setParameter("day", dayOfWeek)
                .setParameter("time", time)
                .getResultList();
    }

    /**
     * Delete all availability slots for a doctor
     *
     * @param doctorId Doctor's ID
     * @return Number of slots deleted
     */
    public long deleteByDoctorId(Long doctorId) {
        return delete("doctor.id", doctorId);
    }

    /**
     * Find active availability slots
     *
     * @return List of all active slots
     */
    public List<DoctorAvailability> findAllActive() {
        return list("isActive = true ORDER BY dayOfWeek, startTime");
    }
}
