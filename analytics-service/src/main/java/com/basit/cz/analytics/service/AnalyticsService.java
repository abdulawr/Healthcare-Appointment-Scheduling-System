package com.basit.cz.analytics.service;

import com.basit.cz.analytics.dto.DailyAppointmentsStatDTO;
import com.basit.cz.analytics.dto.DoctorPerformanceDTO;
import com.basit.cz.analytics.dto.DoctorUtilizationDTO;
import com.basit.cz.analytics.dto.MonthlyAppointmentsStatDTO;
import com.basit.cz.analytics.dto.PeakHoursStatsDTO;
import com.basit.cz.analytics.dto.SystemOverviewDTO;
import com.basit.cz.analytics.dto.WeeklyAppointmentsStatDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AnalyticsService {

    @Inject
    EntityManager em;

    // ------------------------------------------------------------
    // 1. APPOINTMENTS – DAILY / WEEKLY / MONTHLY
    // ------------------------------------------------------------

    /**
     * Daily appointment stats (all time).
     * Maps to: GET /api/analytics/appointments/daily
     */
    public List<DailyAppointmentsStatDTO> getDailyAppointments() {
        String sql = """
            SELECT
                date(start_time) AS day,
                COUNT(*) AS total,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed,
                SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled
            FROM fact_appointment
            GROUP BY date(start_time)
            ORDER BY date(start_time)
            """;

        List<Object[]> rows = em.createNativeQuery(sql).getResultList();

        return rows.stream().map(row -> {
            DailyAppointmentsStatDTO dto = new DailyAppointmentsStatDTO();
            dto.date = ((java.sql.Date) row[0]).toLocalDate();
            dto.total = ((Number) row[1]).longValue();
            dto.completed = row[2] == null ? 0L : ((Number) row[2]).longValue();
            dto.cancelled = row[3] == null ? 0L : ((Number) row[3]).longValue();
            return dto;
        }).toList();
    }

    /**
     * Weekly appointment stats, grouped by week start date.
     * Maps to: GET /api/analytics/appointments/weekly
     */
    public List<WeeklyAppointmentsStatDTO> getWeeklyAppointments() {
        String sql = """
            SELECT
                date_trunc('week', start_time)::date AS week_start,
                COUNT(*) AS total,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed,
                SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled
            FROM fact_appointment
            GROUP BY date_trunc('week', start_time)
            ORDER BY week_start
            """;

        List<Object[]> rows = em.createNativeQuery(sql).getResultList();

        return rows.stream().map(row -> {
            WeeklyAppointmentsStatDTO dto = new WeeklyAppointmentsStatDTO();
            dto.weekStart = ((java.sql.Date) row[0]).toLocalDate();
            dto.total = ((Number) row[1]).longValue();
            dto.completed = row[2] == null ? 0L : ((Number) row[2]).longValue();
            dto.cancelled = row[3] == null ? 0L : ((Number) row[3]).longValue();
            return dto;
        }).toList();
    }

    /**
     * Monthly appointment stats.
     * Maps to: GET /api/analytics/appointments/monthly
     */
    public List<MonthlyAppointmentsStatDTO> getMonthlyAppointments() {
        String sql = """
            SELECT
                date_trunc('month', start_time)::date AS month_start,
                COUNT(*) AS total,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed,
                SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled
            FROM fact_appointment
            GROUP BY date_trunc('month', start_time)
            ORDER BY month_start
            """;

        List<Object[]> rows = em.createNativeQuery(sql).getResultList();

        return rows.stream().map(row -> {
            MonthlyAppointmentsStatDTO dto = new MonthlyAppointmentsStatDTO();
            dto.monthStart = ((java.sql.Date) row[0]).toLocalDate();
            dto.total = ((Number) row[1]).longValue();
            dto.completed = row[2] == null ? 0L : ((Number) row[2]).longValue();
            dto.cancelled = row[3] == null ? 0L : ((Number) row[3]).longValue();
            return dto;
        }).toList();
    }

    // ------------------------------------------------------------
    // 2. DOCTOR UTILIZATION & DOCTOR PERFORMANCE
    // ------------------------------------------------------------

    /**
     * Doctor utilization metrics for a single doctor.
     * Maps to: GET /api/analytics/doctor/{id}
     */
    public DoctorUtilizationDTO getDoctorUtilization(UUID doctorId) {
        String sql = """
            SELECT
                COUNT(*) AS total_appointments,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed,
                SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled,
                SUM(EXTRACT(EPOCH FROM (end_time - start_time)) / 60.0) AS total_minutes
            FROM fact_appointment
            WHERE doctor_id = :doctorId
            """;

        Object[] row = (Object[]) em.createNativeQuery(sql)
                .setParameter("doctorId", doctorId)
                .getSingleResult();

        DoctorUtilizationDTO dto = new DoctorUtilizationDTO();
        dto.doctorId = doctorId;
        dto.totalAppointments = ((Number) row[0]).longValue();
        dto.completedAppointments = row[1] == null ? 0L : ((Number) row[1]).longValue();
        dto.cancelledAppointments = row[2] == null ? 0L : ((Number) row[2]).longValue();

        dto.totalScheduledMinutes = row[3] == null ? 0.0 : ((Number) row[3]).doubleValue();

        dto.completionRate = dto.totalAppointments == 0
                ? 0.0
                : (double) dto.completedAppointments / dto.totalAppointments;

        dto.cancellationRate = dto.totalAppointments == 0
                ? 0.0
                : (double) dto.cancelledAppointments / dto.totalAppointments;

        return dto;
    }

    /**
     * Performance comparison across all doctors.
     * Maps to: GET /api/analytics/doctors/performance
     */
    public List<DoctorPerformanceDTO> getDoctorsPerformance() {
        String sql = """
            SELECT
                doctor_id,
                COUNT(*) AS total_appointments,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed,
                SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled,
                SUM(price_cents) AS total_revenue_cents
            FROM fact_appointment
            GROUP BY doctor_id
            ORDER BY total_appointments DESC
            """;

        List<Object[]> rows = em.createNativeQuery(sql).getResultList();

        return rows.stream().map(row -> {
            DoctorPerformanceDTO dto = new DoctorPerformanceDTO();
            dto.doctorId = (UUID) row[0];
            dto.totalAppointments = ((Number) row[1]).longValue();
            dto.completedAppointments = row[2] == null ? 0L : ((Number) row[2]).longValue();
            dto.cancelledAppointments = row[3] == null ? 0L : ((Number) row[3]).longValue();

            long totalRevenueCents = row[4] == null ? 0L : ((Number) row[4]).longValue();
            dto.totalRevenueCents = totalRevenueCents;
            dto.totalRevenue = BigDecimal.valueOf(totalRevenueCents)
                    .movePointLeft(2); // cents -> currency

            dto.completionRate = dto.totalAppointments == 0
                    ? 0.0
                    : (double) dto.completedAppointments / dto.totalAppointments;
            dto.cancellationRate = dto.totalAppointments == 0
                    ? 0.0
                    : (double) dto.cancelledAppointments / dto.totalAppointments;

            return dto;
        }).toList();
    }

    // ------------------------------------------------------------
    // 4. SYSTEM OVERVIEW
    // ------------------------------------------------------------

    /**
     * Overall system health summary from the analytics perspective.
     * Maps to: GET /api/analytics/system/overview
     */
    public SystemOverviewDTO getSystemOverview() {
        String sql = """
            SELECT
                COUNT(*) AS total_appointments,
                COUNT(DISTINCT doctor_id) AS total_doctors,
                COUNT(DISTINCT patient_id) AS total_patients,
                SUM(price_cents) AS total_revenue_cents,
                MIN(start_time) AS first_appointment,
                MAX(start_time) AS last_appointment
            FROM fact_appointment
            """;

        Object[] row = (Object[]) em.createNativeQuery(sql).getSingleResult();

        SystemOverviewDTO dto = new SystemOverviewDTO();
        dto.totalAppointments = row[0] == null ? 0L : ((Number) row[0]).longValue();
        dto.totalDoctors = row[1] == null ? 0L : ((Number) row[1]).longValue();
        dto.totalPatients = row[2] == null ? 0L : ((Number) row[2]).longValue();

        long totalRevenueCents = row[3] == null ? 0L : ((Number) row[3]).longValue();
        dto.totalRevenueCents = totalRevenueCents;
        dto.totalRevenue = BigDecimal.valueOf(totalRevenueCents).movePointLeft(2);

        dto.firstAppointment = row[4] == null ? null : ((java.sql.Timestamp) row[4]).toInstant();
        dto.lastAppointment = row[5] == null ? null : ((java.sql.Timestamp) row[5]).toInstant();

        // Count appointments in last 24h
        Object last24h = em.createNativeQuery("""
            SELECT COUNT(*)
            FROM fact_appointment
            WHERE event_time >= now() - interval '24 hours'
            """).getSingleResult();
        dto.appointmentsLast24h = last24h == null ? 0L : ((Number) last24h).longValue();

        return dto;
    }

    // ------------------------------------------------------------
    // 4. PEAK HOURS
    // ------------------------------------------------------------

    /**
     * Peak appointment hours of the day.
     * Maps to: GET /api/analytics/peak-hours
     */
    public List<PeakHoursStatsDTO> getPeakHours() {
        String sql = """
            SELECT
                EXTRACT(HOUR FROM start_time)::int AS hour_of_day,
                COUNT(*) AS total
            FROM fact_appointment
            GROUP BY EXTRACT(HOUR FROM start_time)
            ORDER BY hour_of_day
            """;

        List<Object[]> rows = em.createNativeQuery(sql).getResultList();

        return rows.stream().map(row -> {
            PeakHoursStatsDTO dto = new PeakHoursStatsDTO();
            dto.hourOfDay = ((Number) row[0]).intValue();
            dto.totalAppointments = ((Number) row[1]).longValue();
            return dto;
        }).toList();
    }
}
