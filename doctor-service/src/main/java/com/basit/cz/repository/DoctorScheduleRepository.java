package com.basit.cz.repository;

import com.basit.cz.entity.DoctorSchedule;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;

/**
 * Doctor Schedule Repository
 *
 * Provides custom queries for DoctorSchedule entity.
 * Handles queries related to doctor schedules, time-off, and vacations.
 */
@ApplicationScoped
public class DoctorScheduleRepository implements PanacheRepository<DoctorSchedule> {

    @Inject
    EntityManager entityManager;

    /**
     * Find all schedules for a doctor
     *
     * @param doctorId Doctor's ID
     * @return List of schedules ordered by start date
     */
    public List<DoctorSchedule> findByDoctorId(Long doctorId) {
        return list("doctor.id = ?1 ORDER BY startDate DESC", doctorId);
    }

    /**
     * Find schedules by type
     *
     * @param doctorId Doctor's ID
     * @param type Schedule type (VACATION, SICK_LEAVE, etc.)
     * @return List of schedules of this type
     */
    public List<DoctorSchedule> findByDoctorAndType(Long doctorId, DoctorSchedule.ScheduleType type) {
        return list("doctor.id = ?1 AND scheduleType = ?2 ORDER BY startDate DESC",
                doctorId, type);
    }

    /**
     * Find schedules by status
     *
     * @param doctorId Doctor's ID
     * @param status Schedule status (PENDING, APPROVED, etc.)
     * @return List of schedules with this status
     */
    public List<DoctorSchedule> findByDoctorAndStatus(Long doctorId, DoctorSchedule.ScheduleStatus status) {
        return list("doctor.id = ?1 AND status = ?2 ORDER BY startDate DESC",
                doctorId, status);
    }

    /**
     * Find pending schedules (awaiting approval)
     *
     * @param doctorId Doctor's ID
     * @return List of pending schedules
     */
    public List<DoctorSchedule> findPendingSchedules(Long doctorId) {
        return findByDoctorAndStatus(doctorId, DoctorSchedule.ScheduleStatus.PENDING);
    }

    /**
     * Find approved schedules
     *
     * @param doctorId Doctor's ID
     * @return List of approved schedules
     */
    public List<DoctorSchedule> findApprovedSchedules(Long doctorId) {
        return findByDoctorAndStatus(doctorId, DoctorSchedule.ScheduleStatus.APPROVED);
    }

    /**
     * Find active schedules (approved and covering current date)
     *
     * @param doctorId Doctor's ID
     * @param date Date to check (usually today)
     * @return List of active schedules
     */
    public List<DoctorSchedule> findActiveSchedules(Long doctorId, LocalDate date) {
        return entityManager.createQuery(
                        "SELECT s FROM DoctorSchedule s " +
                                "WHERE s.doctor.id = :doctorId " +
                                "AND s.status = :status " +
                                "AND s.startDate <= :date " +
                                "AND s.endDate >= :date " +
                                "ORDER BY s.startDate",
                        DoctorSchedule.class)
                .setParameter("doctorId", doctorId)
                .setParameter("status", DoctorSchedule.ScheduleStatus.APPROVED)
                .setParameter("date", date)
                .getResultList();
    }

    /**
     * Find schedules overlapping with a date range
     *
     * @param doctorId Doctor's ID
     * @param startDate Start of range
     * @param endDate End of range
     * @return List of overlapping schedules
     */
    public List<DoctorSchedule> findOverlappingSchedules(Long doctorId, LocalDate startDate, LocalDate endDate) {
        return entityManager.createQuery(
                        "SELECT s FROM DoctorSchedule s " +
                                "WHERE s.doctor.id = :doctorId " +
                                "AND s.status = :status " +
                                "AND ((s.startDate <= :endDate AND s.endDate >= :startDate)) " +
                                "ORDER BY s.startDate",
                        DoctorSchedule.class)
                .setParameter("doctorId", doctorId)
                .setParameter("status", DoctorSchedule.ScheduleStatus.APPROVED)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    /**
     * Find upcoming schedules (start date in the future)
     *
     * @param doctorId Doctor's ID
     * @return List of upcoming schedules
     */
    public List<DoctorSchedule> findUpcomingSchedules(Long doctorId) {
        return entityManager.createQuery(
                        "SELECT s FROM DoctorSchedule s " +
                                "WHERE s.doctor.id = :doctorId " +
                                "AND s.startDate > :today " +
                                "AND s.status = :status " +
                                "ORDER BY s.startDate",
                        DoctorSchedule.class)
                .setParameter("doctorId", doctorId)
                .setParameter("today", LocalDate.now())
                .setParameter("status", DoctorSchedule.ScheduleStatus.APPROVED)
                .getResultList();
    }

    /**
     * Find all pending schedules (any doctor)
     * For admin approval workflow
     *
     * @return List of all pending schedules
     */
    public List<DoctorSchedule> findAllPending() {
        return list("status = ?1 ORDER BY createdAt ASC",
                DoctorSchedule.ScheduleStatus.PENDING);
    }

    /**
     * Count schedules by type for a doctor
     *
     * @param doctorId Doctor's ID
     * @param type Schedule type
     * @return Count of schedules of this type
     */
    public long countByType(Long doctorId, DoctorSchedule.ScheduleType type) {
        return count("doctor.id = ?1 AND scheduleType = ?2", doctorId, type);
    }

    /**
     * Find schedules within date range
     *
     * @param doctorId Doctor's ID
     * @param startDate Start of range
     * @param endDate End of range
     * @return List of schedules within range
     */
    public List<DoctorSchedule> findWithinDateRange(Long doctorId, LocalDate startDate, LocalDate endDate) {
        return entityManager.createQuery(
                        "SELECT s FROM DoctorSchedule s " +
                                "WHERE s.doctor.id = :doctorId " +
                                "AND s.startDate >= :startDate " +
                                "AND s.endDate <= :endDate " +
                                "ORDER BY s.startDate",
                        DoctorSchedule.class)
                .setParameter("doctorId", doctorId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }
}





