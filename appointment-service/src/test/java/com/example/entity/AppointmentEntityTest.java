package com.example.entity;

import com.example.constant.AppointmentStatus;
import com.example.constant.AppointmentType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Appointment entity
 * Tests: 12 test cases covering entity persistence, queries, and business logic
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppointmentEntityTest {

    @Inject
    EntityManager entityManager;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean database before each test
        Appointment.deleteAll();
    }

    // ==================== TEST 1: Entity Persistence ====================

    @Test
    @Order(1)
    @DisplayName("Test 1: Should persist appointment with all fields")
    @Transactional
    void shouldPersistAppointmentWithAllFields() {
        // Given
        Appointment appointment = new Appointment();
        appointment.patientId = 101L;
        appointment.doctorId = 201L;
        appointment.startTime = LocalDateTime.now().plusDays(1);
        appointment.endTime = LocalDateTime.now().plusDays(1).plusHours(1);
        appointment.type = AppointmentType.CONSULTATION;
        appointment.reason = "Annual checkup";
        appointment.notes = "Patient has allergies";

        // When
        appointment.persist();
        entityManager.flush();
        entityManager.clear();

        // Then
        Appointment found = Appointment.findById(appointment.id);
        assertThat(found).isNotNull();
        assertThat(found.id).isNotNull();
        assertThat(found.patientId).isEqualTo(101L);
        assertThat(found.doctorId).isEqualTo(201L);
        assertThat(found.type).isEqualTo(AppointmentType.CONSULTATION);
        assertThat(found.reason).isEqualTo("Annual checkup");
        assertThat(found.notes).isEqualTo("Patient has allergies");
    }

    // ==================== TEST 2: Default Status ====================

    @Test
    @Order(2)
    @DisplayName("Test 2: Should set default status to SCHEDULED on persist")
    @Transactional
    void shouldSetDefaultStatusOnPersist() {
        // Given
        Appointment appointment = createBasicAppointment(1L, 1L);

        // When
        appointment.persist();

        // Then
        assertThat(appointment.status).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(appointment.createdAt).isNotNull();
        assertThat(appointment.updatedAt).isNotNull();
    }

    // ==================== TEST 3: Timestamps ====================

    @Test
    @Order(3)
    @DisplayName("Test 3: Should auto-generate timestamps on persist and update")
    @Transactional
    void shouldAutoGenerateTimestamps() throws InterruptedException {
        // Given
        Appointment appointment = createBasicAppointment(2L, 2L);
        appointment.persist();
        LocalDateTime originalCreatedAt = appointment.createdAt;
        LocalDateTime originalUpdatedAt = appointment.updatedAt;

        // Wait a bit to ensure timestamp difference
        Thread.sleep(100);

        // When
        appointment.notes = "Updated notes";
        appointment.persist();
        entityManager.flush();

        // Then
        assertThat(appointment.createdAt).isEqualTo(originalCreatedAt);
        assertThat(appointment.updatedAt).isAfter(originalUpdatedAt);
    }

    // ==================== TEST 4: Find by Patient ID ====================

    @Test
    @Order(4)
    @DisplayName("Test 4: Should find all appointments by patient ID")
    @Transactional
    void shouldFindAppointmentsByPatientId() {
        // Given
        Long patientId = 100L;
        createAndPersistAppointment(patientId, 1L);
        createAndPersistAppointment(patientId, 2L);
        createAndPersistAppointment(999L, 3L); // Different patient

        // When
        List<Appointment> appointments = Appointment.findByPatientId(patientId);

        // Then
        assertThat(appointments).hasSize(2);
        assertThat(appointments).allMatch(a -> a.patientId.equals(patientId));
    }

    // ==================== TEST 5: Find by Doctor ID ====================

    @Test
    @Order(5)
    @DisplayName("Test 5: Should find all appointments by doctor ID")
    @Transactional
    void shouldFindAppointmentsByDoctorId() {
        // Given
        Long doctorId = 200L;
        createAndPersistAppointment(1L, doctorId);
        createAndPersistAppointment(2L, doctorId);
        createAndPersistAppointment(3L, 999L); // Different doctor

        // When
        List<Appointment> appointments = Appointment.findByDoctorId(doctorId);

        // Then
        assertThat(appointments).hasSize(2);
        assertThat(appointments).allMatch(a -> a.doctorId.equals(doctorId));
    }

    // ==================== TEST 6: Find by Status ====================

    @Test
    @Order(6)
    @DisplayName("Test 6: Should find appointments by status")
    @Transactional
    void shouldFindAppointmentsByStatus() {
        // Given
        Appointment scheduled = createAndPersistAppointment(1L, 1L);
        scheduled.status = AppointmentStatus.SCHEDULED;
        scheduled.persist();

        Appointment confirmed = createAndPersistAppointment(2L, 1L);
        confirmed.status = AppointmentStatus.CONFIRMED;
        confirmed.persist();

        Appointment cancelled = createAndPersistAppointment(3L, 1L);
        cancelled.status = AppointmentStatus.CANCELLED;
        cancelled.persist();

        // When
        List<Appointment> scheduledAppointments = Appointment.findByStatus(AppointmentStatus.SCHEDULED);
        List<Appointment> confirmedAppointments = Appointment.findByStatus(AppointmentStatus.CONFIRMED);

        // Then
        assertThat(scheduledAppointments).hasSize(1);
        assertThat(confirmedAppointments).hasSize(1);
        assertThat(scheduledAppointments.get(0).status).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(confirmedAppointments.get(0).status).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    // ==================== TEST 7: Find Upcoming Appointments ====================

    @Test
    @Order(7)
    @DisplayName("Test 7: Should find only upcoming appointments")
    @Transactional
    void shouldFindUpcomingAppointments() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // Past appointment
        Appointment past = createBasicAppointment(1L, 1L);
        past.startTime = now.minusDays(1);
        past.endTime = now.minusDays(1).plusHours(1);
        past.status = AppointmentStatus.COMPLETED;
        past.persist();

        // Future scheduled appointment
        Appointment futureScheduled = createBasicAppointment(2L, 1L);
        futureScheduled.startTime = now.plusDays(1);
        futureScheduled.endTime = now.plusDays(1).plusHours(1);
        futureScheduled.status = AppointmentStatus.SCHEDULED;
        futureScheduled.persist();

        // Future confirmed appointment
        Appointment futureConfirmed = createBasicAppointment(3L, 1L);
        futureConfirmed.startTime = now.plusDays(2);
        futureConfirmed.endTime = now.plusDays(2).plusHours(1);
        futureConfirmed.status = AppointmentStatus.CONFIRMED;
        futureConfirmed.persist();

        // Future cancelled appointment (should not appear)
        Appointment futureCancelled = createBasicAppointment(4L, 1L);
        futureCancelled.startTime = now.plusDays(3);
        futureCancelled.endTime = now.plusDays(3).plusHours(1);
        futureCancelled.status = AppointmentStatus.CANCELLED;
        futureCancelled.persist();

        // When
        List<Appointment> upcomingAppointments = Appointment.findUpcoming(now);

        // Then
        assertThat(upcomingAppointments).hasSize(2);
        assertThat(upcomingAppointments).allMatch(a -> a.startTime.isAfter(now));
        assertThat(upcomingAppointments).noneMatch(a ->
                a.status == AppointmentStatus.CANCELLED ||
                        a.status == AppointmentStatus.COMPLETED
        );
    }

    // ==================== TEST 8: Date Range Query ====================

    @Test
    @Order(8)
    @DisplayName("Test 8: Should find appointments within date range")
    @Transactional
    void shouldFindAppointmentsInDateRange() {
        // Given
        Long doctorId = 300L;
        LocalDateTime rangeStart = LocalDateTime.of(2025, 12, 10, 0, 0);
        LocalDateTime rangeEnd = LocalDateTime.of(2025, 12, 20, 23, 59);

        // Before range
        Appointment before = createBasicAppointment(1L, doctorId);
        before.startTime = LocalDateTime.of(2025, 12, 5, 10, 0);
        before.endTime = LocalDateTime.of(2025, 12, 5, 11, 0);
        before.persist();

        // Within range
        Appointment within1 = createBasicAppointment(2L, doctorId);
        within1.startTime = LocalDateTime.of(2025, 12, 15, 10, 0);
        within1.endTime = LocalDateTime.of(2025, 12, 15, 11, 0);
        within1.persist();

        Appointment within2 = createBasicAppointment(3L, doctorId);
        within2.startTime = LocalDateTime.of(2025, 12, 18, 14, 0);
        within2.endTime = LocalDateTime.of(2025, 12, 18, 15, 0);
        within2.persist();

        // After range
        Appointment after = createBasicAppointment(4L, doctorId);
        after.startTime = LocalDateTime.of(2025, 12, 25, 10, 0);
        after.endTime = LocalDateTime.of(2025, 12, 25, 11, 0);
        after.persist();

        // When
        List<Appointment> appointments = Appointment.findByDoctorIdAndDateRange(
                doctorId, rangeStart, rangeEnd
        );

        // Then
        assertThat(appointments).hasSize(2);
        assertThat(appointments).allMatch(a ->
                !a.startTime.isBefore(rangeStart) && !a.startTime.isAfter(rangeEnd)
        );
    }

    // ==================== TEST 9: Count by Status ====================

    @Test
    @Order(9)
    @DisplayName("Test 9: Should count appointments by doctor and status")
    @Transactional
    void shouldCountAppointmentsByDoctorAndStatus() {
        // Given
        Long doctorId = 400L;

        createAppointmentWithStatus(1L, doctorId, AppointmentStatus.SCHEDULED);
        createAppointmentWithStatus(2L, doctorId, AppointmentStatus.SCHEDULED);
        createAppointmentWithStatus(3L, doctorId, AppointmentStatus.CONFIRMED);
        createAppointmentWithStatus(4L, doctorId, AppointmentStatus.CANCELLED);

        // When
        long scheduledCount = Appointment.countByDoctorIdAndStatus(doctorId, AppointmentStatus.SCHEDULED);
        long confirmedCount = Appointment.countByDoctorIdAndStatus(doctorId, AppointmentStatus.CONFIRMED);

        // Then
        assertThat(scheduledCount).isEqualTo(2);
        assertThat(confirmedCount).isEqualTo(1);
    }

    // ==================== TEST 10: Overlapping Appointments ====================

    @Test
    @Order(10)
    @DisplayName("Test 10: Should detect overlapping appointments")
    @Transactional
    void shouldDetectOverlappingAppointments() {
        // Given
        Long doctorId = 500L;
        LocalDateTime start = LocalDateTime.of(2025, 12, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 15, 11, 0);

        Appointment existing = createBasicAppointment(1L, doctorId);
        existing.startTime = start;
        existing.endTime = end;
        existing.status = AppointmentStatus.SCHEDULED;
        existing.persist();

        // When - Check for overlap with same time
        boolean hasOverlap1 = Appointment.hasOverlappingAppointment(
                doctorId, start, end, null
        );

        // When - Check for overlap with partial overlap
        boolean hasOverlap2 = Appointment.hasOverlappingAppointment(
                doctorId,
                start.plusMinutes(30),
                end.plusMinutes(30),
                null
        );

        // When - Check for no overlap (different time)
        boolean hasOverlap3 = Appointment.hasOverlappingAppointment(
                doctorId,
                end.plusHours(1),
                end.plusHours(2),
                null
        );

        // Then
        assertThat(hasOverlap1).isTrue();
        assertThat(hasOverlap2).isTrue();
        assertThat(hasOverlap3).isFalse();
    }

    // ==================== TEST 11: Filter Appointments ====================

    @Test
    @Order(11)
    @DisplayName("Test 11: Should filter appointments by multiple criteria")
    @Transactional
    void shouldFilterAppointmentsByMultipleCriteria() {
        // Given
        LocalDateTime date1 = LocalDateTime.of(2025, 12, 10, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2025, 12, 15, 10, 0);
        LocalDateTime date3 = LocalDateTime.of(2025, 12, 20, 10, 0);

        Appointment app1 = createBasicAppointment(1L, 1L);
        app1.startTime = date1;
        app1.status = AppointmentStatus.SCHEDULED;
        app1.persist();

        Appointment app2 = createBasicAppointment(2L, 1L);
        app2.startTime = date2;
        app2.status = AppointmentStatus.CONFIRMED;
        app2.persist();

        Appointment app3 = createBasicAppointment(3L, 1L);
        app3.startTime = date3;
        app3.status = AppointmentStatus.SCHEDULED;
        app3.persist();

        // When - Filter by status only
        List<Appointment> scheduledOnly = Appointment.findWithFilters(
                AppointmentStatus.SCHEDULED, null, null
        );

        // When - Filter by date range only
        List<Appointment> inRange = Appointment.findWithFilters(
                null,
                LocalDateTime.of(2025, 12, 12, 0, 0),
                LocalDateTime.of(2025, 12, 18, 23, 59)
        );

        // Then
        assertThat(scheduledOnly).hasSize(2);
        assertThat(inRange).hasSize(1);
        assertThat(inRange.get(0).startTime).isEqualTo(date2);
    }

    // ==================== TEST 12: Delete Appointment ====================

    @Test
    @Order(12)
    @DisplayName("Test 12: Should delete appointment")
    @Transactional
    void shouldDeleteAppointment() {
        // Given
        Appointment appointment = createAndPersistAppointment(1L, 1L);
        Long appointmentId = appointment.id;

        // When
        appointment.delete();
        entityManager.flush();

        // Then
        Appointment found = Appointment.findById(appointmentId);
        assertThat(found).isNull();
        assertThat(Appointment.count()).isEqualTo(0);
    }

    // ==================== Helper Methods ====================

    private Appointment createBasicAppointment(Long patientId, Long doctorId) {
        Appointment appointment = new Appointment();
        appointment.patientId = patientId;
        appointment.doctorId = doctorId;
        appointment.startTime = LocalDateTime.now().plusDays(1);
        appointment.endTime = LocalDateTime.now().plusDays(1).plusHours(1);
        appointment.type = AppointmentType.CONSULTATION;
        appointment.reason = "Test appointment";
        return appointment;
    }

    private Appointment createAndPersistAppointment(Long patientId, Long doctorId) {
        Appointment appointment = createBasicAppointment(patientId, doctorId);
        appointment.persist();
        return appointment;
    }

    private void createAppointmentWithStatus(Long patientId, Long doctorId, AppointmentStatus status) {
        Appointment appointment = createBasicAppointment(patientId, doctorId);
        appointment.status = status;
        appointment.persist();
    }
}



