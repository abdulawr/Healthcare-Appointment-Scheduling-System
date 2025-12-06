package com.example.service;

import com.example.constant.AppointmentStatus;
import com.example.constant.AppointmentType;
import com.example.dto.AppointmentResponse;
import com.example.dto.CreateAppointmentRequest;
import com.example.entity.Appointment;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for AppointmentService business logic
 * Tests: 20 test cases covering all service methods and business rules
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppointmentServiceTest {

    @Inject
    AppointmentService appointmentService;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean database before each test
        Appointment.deleteAll();
    }

    // ==================== CREATE APPOINTMENT TESTS ====================

    @Test
    @Order(1)
    @DisplayName("Test 1: Should create appointment with valid data")
    void shouldCreateAppointmentWithValidData() {
        // Given
        CreateAppointmentRequest request = createValidRequest();

        // When
        AppointmentResponse response = appointmentService.createAppointment(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id).isNotNull();
        assertThat(response.patientId).isEqualTo(1L);
        assertThat(response.doctorId).isEqualTo(2L);
        assertThat(response.status).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(response.type).isEqualTo(AppointmentType.CONSULTATION);
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Should throw exception for missing patient ID")
    void shouldThrowExceptionForMissingPatientId() {
        // Given
        CreateAppointmentRequest request = createValidRequest();
        request.patientId = null;

        // When/Then
        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Patient ID is required");
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Should throw exception for missing doctor ID")
    void shouldThrowExceptionForMissingDoctorId() {
        // Given
        CreateAppointmentRequest request = createValidRequest();
        request.doctorId = null;

        // When/Then
        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Doctor ID is required");
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Should throw exception for past start time")
    void shouldThrowExceptionForPastStartTime() {
        // Given
        CreateAppointmentRequest request = createValidRequest();
        request.startTime = LocalDateTime.now().minusDays(1);

        // When/Then
        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start time must be in the future");
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Should prevent double booking (doctor unavailable)")
    void shouldPreventDoubleBooking() {
        // Given - Create first appointment
        CreateAppointmentRequest first = createValidRequest();
        appointmentService.createAppointment(first);

        // When/Then - Try to book overlapping appointment
        CreateAppointmentRequest overlapping = createValidRequest();
        overlapping.patientId = 999L; // Different patient
        // Same time slot and same doctor

        assertThatThrownBy(() -> appointmentService.createAppointment(overlapping))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available");
    }

    // ==================== GET APPOINTMENT TESTS ====================

    @Test
    @Order(6)
    @DisplayName("Test 6: Should get appointment by ID")
    void shouldGetAppointmentById() {
        // Given
        CreateAppointmentRequest request = createValidRequest();
        AppointmentResponse created = appointmentService.createAppointment(request);

        // When
        AppointmentResponse retrieved = appointmentService.getAppointment(created.id);

        // Then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.id).isEqualTo(created.id);
        assertThat(retrieved.patientId).isEqualTo(created.patientId);
        assertThat(retrieved.doctorId).isEqualTo(created.doctorId);
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Should throw NotFoundException for non-existent ID")
    void shouldThrowNotFoundExceptionForInvalidId() {
        // When/Then
        assertThatThrownBy(() -> appointmentService.getAppointment(99999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Appointment not found");
    }

    // ==================== RESCHEDULE TESTS ====================

    @Test
    @Order(8)
    @DisplayName("Test 8: Should reschedule appointment to valid time")
    void shouldRescheduleToValidTime() {
        // Given
        CreateAppointmentRequest request = createValidRequest();
        AppointmentResponse created = appointmentService.createAppointment(request);

        LocalDateTime newStart = LocalDateTime.now().plusDays(5).withHour(14);
        LocalDateTime newEnd = LocalDateTime.now().plusDays(5).withHour(15);

        // When
        AppointmentResponse rescheduled = appointmentService.rescheduleAppointment(
                created.id, newStart, newEnd
        );

        // Then
        assertThat(rescheduled.startTime).isEqualTo(newStart);
        assertThat(rescheduled.endTime).isEqualTo(newEnd);
    }

    @Test
    @Order(9)
    @DisplayName("Test 9: Should check availability when rescheduling")
    void shouldCheckAvailabilityWhenRescheduling() {
        // Given - Create two appointments
        CreateAppointmentRequest first = createValidRequest();
        AppointmentResponse firstApp = appointmentService.createAppointment(first);

        CreateAppointmentRequest second = createValidRequest();
        second.startTime = LocalDateTime.now().plusDays(5);
        second.endTime = LocalDateTime.now().plusDays(5).plusHours(1);
        second.patientId = 999L;
        appointmentService.createAppointment(second);

        // When/Then - Try to reschedule first to overlap with second
        assertThatThrownBy(() ->
                appointmentService.rescheduleAppointment(
                        firstApp.id,
                        second.startTime,
                        second.endTime
                )
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @Order(10)
    @DisplayName("Test 10: Should not reschedule cancelled appointment")
    void shouldNotRescheduleCancelledAppointment() {
        // Given
        CreateAppointmentRequest request = createValidRequest();
        AppointmentResponse created = appointmentService.createAppointment(request);
        appointmentService.cancelAppointment(created.id, "Patient request");

        // When/Then
        assertThatThrownBy(() ->
                appointmentService.rescheduleAppointment(
                        created.id,
                        LocalDateTime.now().plusDays(5),
                        LocalDateTime.now().plusDays(5).plusHours(1)
                )
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancelled");
    }

    // ==================== CANCEL TESTS ====================

    @Test
    @Order(11)
    @DisplayName("Test 11: Should cancel appointment with reason")
    void shouldCancelAppointmentWithReason() {
        // Given
        CreateAppointmentRequest request = createValidRequest();
        AppointmentResponse created = appointmentService.createAppointment(request);

        // When
        appointmentService.cancelAppointment(created.id, "Patient is sick");

        // Then
        AppointmentResponse cancelled = appointmentService.getAppointment(created.id);
        assertThat(cancelled.status).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(cancelled.cancellationReason).isEqualTo("Patient is sick");
        assertThat(cancelled.cancelledAt).isNotNull();
    }

    @Test
    @Order(12)
    @DisplayName("Test 12: Should set cancellation timestamp")
    void shouldSetCancellationTimestamp() {
        // Given
        CreateAppointmentRequest request = createValidRequest();
        AppointmentResponse created = appointmentService.createAppointment(request);
        LocalDateTime beforeCancel = LocalDateTime.now();

        // When
        appointmentService.cancelAppointment(created.id, "Test");

        // Then
        AppointmentResponse cancelled = appointmentService.getAppointment(created.id);
        assertThat(cancelled.cancelledAt).isAfterOrEqualTo(beforeCancel);
    }

    // ==================== CONFIRM TESTS ====================

    @Test
    @Order(13)
    @DisplayName("Test 13: Should confirm scheduled appointment")
    void shouldConfirmScheduledAppointment() {
        // Given
        CreateAppointmentRequest request = createValidRequest();
        AppointmentResponse created = appointmentService.createAppointment(request);

        // When
        AppointmentResponse confirmed = appointmentService.confirmAppointment(created.id);

        // Then
        assertThat(confirmed.status).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(confirmed.confirmationSent).isTrue();
    }

    @Test
    @Order(14)
    @DisplayName("Test 14: Should not confirm non-scheduled appointment")
    void shouldNotConfirmNonScheduledAppointment() {
        // Given
        CreateAppointmentRequest request = createValidRequest();
        AppointmentResponse created = appointmentService.createAppointment(request);
        appointmentService.cancelAppointment(created.id, "Test");

        // When/Then
        assertThatThrownBy(() -> appointmentService.confirmAppointment(created.id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only SCHEDULED appointments can be confirmed");
    }

    // ==================== CHECK-IN TESTS ====================

    @Test
    @Order(15)
    @DisplayName("Test 15: Should check-in confirmed appointment")
    void shouldCheckInConfirmedAppointment() {
        // Given
        CreateAppointmentRequest request = createValidRequest();
        AppointmentResponse created = appointmentService.createAppointment(request);
        appointmentService.confirmAppointment(created.id);

        // When
        AppointmentResponse checkedIn = appointmentService.checkInAppointment(created.id);

        // Then
        assertThat(checkedIn.status).isEqualTo(AppointmentStatus.CHECKED_IN);
        assertThat(checkedIn.checkedInAt).isNotNull();
    }

    @Test
    @Order(16)
    @DisplayName("Test 16: Should check-in scheduled appointment")
    void shouldCheckInScheduledAppointment() {
        // Given
        CreateAppointmentRequest request = createValidRequest();
        AppointmentResponse created = appointmentService.createAppointment(request);

        // When
        AppointmentResponse checkedIn = appointmentService.checkInAppointment(created.id);

        // Then
        assertThat(checkedIn.status).isEqualTo(AppointmentStatus.CHECKED_IN);
        assertThat(checkedIn.checkedInAt).isNotNull();
    }

    // ==================== COMPLETE TESTS ====================

    @Test
    @Order(17)
    @DisplayName("Test 17: Should complete appointment")
    void shouldCompleteAppointment() {
        // Given
        CreateAppointmentRequest request = createValidRequest();
        AppointmentResponse created = appointmentService.createAppointment(request);

        // When
        AppointmentResponse completed = appointmentService.completeAppointment(created.id);

        // Then
        assertThat(completed.status).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(completed.completedAt).isNotNull();
    }

    @Test
    @Order(18)
    @DisplayName("Test 18: Should not complete cancelled appointment")
    void shouldNotCompleteCancelledAppointment() {
        // Given
        CreateAppointmentRequest request = createValidRequest();
        AppointmentResponse created = appointmentService.createAppointment(request);
        appointmentService.cancelAppointment(created.id, "Test");

        // When/Then
        assertThatThrownBy(() -> appointmentService.completeAppointment(created.id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancelled");
    }

    // ==================== QUERY TESTS ====================

    @Test
    @Order(19)
    @DisplayName("Test 19: Should get patient appointments")
    void shouldGetPatientAppointments() {
        // Given
        Long patientId = 100L;
        CreateAppointmentRequest req1 = createValidRequest();
        req1.patientId = patientId;
        appointmentService.createAppointment(req1);

        CreateAppointmentRequest req2 = createValidRequest();
        req2.patientId = patientId;
        req2.startTime = LocalDateTime.now().plusDays(2);
        req2.endTime = LocalDateTime.now().plusDays(2).plusHours(1);
        appointmentService.createAppointment(req2);

        CreateAppointmentRequest req3 = createValidRequest();
        req3.patientId = 999L; // Different patient
        req3.startTime = LocalDateTime.now().plusDays(3);
        req3.endTime = LocalDateTime.now().plusDays(3).plusHours(1);
        appointmentService.createAppointment(req3);

        // When
        List<AppointmentResponse> appointments = appointmentService.getPatientAppointments(patientId);

        // Then
        assertThat(appointments).hasSize(2);
        assertThat(appointments).allMatch(a -> a.patientId.equals(patientId));
    }

    @Test
    @Order(20)
    @DisplayName("Test 20: Should get doctor appointments")
    void shouldGetDoctorAppointments() {
        // Given
        Long doctorId = 200L;
        CreateAppointmentRequest req1 = createValidRequest();
        req1.doctorId = doctorId;
        appointmentService.createAppointment(req1);

        CreateAppointmentRequest req2 = createValidRequest();
        req2.doctorId = doctorId;
        req2.startTime = LocalDateTime.now().plusDays(2);
        req2.endTime = LocalDateTime.now().plusDays(2).plusHours(1);
        appointmentService.createAppointment(req2);

        // When
        List<AppointmentResponse> appointments = appointmentService.getDoctorAppointments(doctorId);

        // Then
        assertThat(appointments).hasSize(2);
        assertThat(appointments).allMatch(a -> a.doctorId.equals(doctorId));
    }

    // ==================== Helper Methods ====================

    private CreateAppointmentRequest createValidRequest() {
        return new CreateAppointmentRequest(
                1L, // patientId
                2L, // doctorId
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(11).withMinute(0),
                AppointmentType.CONSULTATION,
                "Regular checkup"
        );
    }
}


