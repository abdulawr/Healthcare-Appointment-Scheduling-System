package com.example.event;

import com.example.constant.AppointmentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for appointment event classes and event producer
 * Tests: 10 test cases covering event creation, serialization, and publishing
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppointmentEventTest {

    @Inject
    AppointmentEventProducer eventProducer;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // ==================== TEST 1: AppointmentCreatedEvent ====================

    @Test
    @Order(1)
    @DisplayName("Test 1: Should create AppointmentCreatedEvent with all fields")
    void shouldCreateAppointmentCreatedEvent() {
        // Given
        Long appointmentId = 1L;
        Long patientId = 100L;
        Long doctorId = 200L;
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(1);
        AppointmentType type = AppointmentType.CONSULTATION;
        String reason = "Annual checkup";

        // When
        AppointmentCreatedEvent event = new AppointmentCreatedEvent(
                appointmentId, patientId, doctorId, startTime, endTime, type, reason
        );

        // Then
        assertThat(event.eventId).isNotNull();
        assertThat(event.eventType).isEqualTo("APPOINTMENT_CREATED");
        assertThat(event.timestamp).isNotNull();
        assertThat(event.appointmentId).isEqualTo(appointmentId);
        assertThat(event.patientId).isEqualTo(patientId);
        assertThat(event.doctorId).isEqualTo(doctorId);
        assertThat(event.startTime).isEqualTo(startTime);
        assertThat(event.endTime).isEqualTo(endTime);
        assertThat(event.type).isEqualTo(type);
        assertThat(event.reason).isEqualTo(reason);
    }

    // ==================== TEST 2: AppointmentConfirmedEvent ====================

    @Test
    @Order(2)
    @DisplayName("Test 2: Should create AppointmentConfirmedEvent with all fields")
    void shouldCreateAppointmentConfirmedEvent() {
        // Given
        Long appointmentId = 1L;
        Long patientId = 100L;
        Long doctorId = 200L;
        LocalDateTime confirmedAt = LocalDateTime.now();
        LocalDateTime scheduledTime = LocalDateTime.now().plusDays(1);

        // When
        AppointmentConfirmedEvent event = new AppointmentConfirmedEvent(
                appointmentId, patientId, doctorId, confirmedAt, scheduledTime
        );

        // Then
        assertThat(event.eventId).isNotNull();
        assertThat(event.eventType).isEqualTo("APPOINTMENT_CONFIRMED");
        assertThat(event.timestamp).isNotNull();
        assertThat(event.appointmentId).isEqualTo(appointmentId);
        assertThat(event.patientId).isEqualTo(patientId);
        assertThat(event.doctorId).isEqualTo(doctorId);
        assertThat(event.confirmedAt).isEqualTo(confirmedAt);
        assertThat(event.scheduledTime).isEqualTo(scheduledTime);
    }

    // ==================== TEST 3: AppointmentCancelledEvent ====================

    @Test
    @Order(3)
    @DisplayName("Test 3: Should create AppointmentCancelledEvent with all fields")
    void shouldCreateAppointmentCancelledEvent() {
        // Given
        Long appointmentId = 1L;
        Long patientId = 100L;
        Long doctorId = 200L;
        LocalDateTime cancelledAt = LocalDateTime.now();
        String reason = "Patient sick";
        LocalDateTime originalScheduledTime = LocalDateTime.now().plusDays(1);

        // When
        AppointmentCancelledEvent event = new AppointmentCancelledEvent(
                appointmentId, patientId, doctorId, cancelledAt, reason, originalScheduledTime
        );

        // Then
        assertThat(event.eventId).isNotNull();
        assertThat(event.eventType).isEqualTo("APPOINTMENT_CANCELLED");
        assertThat(event.timestamp).isNotNull();
        assertThat(event.appointmentId).isEqualTo(appointmentId);
        assertThat(event.patientId).isEqualTo(patientId);
        assertThat(event.doctorId).isEqualTo(doctorId);
        assertThat(event.cancelledAt).isEqualTo(cancelledAt);
        assertThat(event.cancellationReason).isEqualTo(reason);
        assertThat(event.originalScheduledTime).isEqualTo(originalScheduledTime);
    }

    // ==================== TEST 4: AppointmentRescheduledEvent ====================

    @Test
    @Order(4)
    @DisplayName("Test 4: Should create AppointmentRescheduledEvent with all fields")
    void shouldCreateAppointmentRescheduledEvent() {
        // Given
        Long appointmentId = 1L;
        Long patientId = 100L;
        Long doctorId = 200L;
        LocalDateTime oldStart = LocalDateTime.now().plusDays(1);
        LocalDateTime oldEnd = oldStart.plusHours(1);
        LocalDateTime newStart = LocalDateTime.now().plusDays(2);
        LocalDateTime newEnd = newStart.plusHours(1);
        LocalDateTime rescheduledAt = LocalDateTime.now();

        // When
        AppointmentRescheduledEvent event = new AppointmentRescheduledEvent(
                appointmentId, patientId, doctorId, oldStart, oldEnd, newStart, newEnd, rescheduledAt
        );

        // Then
        assertThat(event.eventId).isNotNull();
        assertThat(event.eventType).isEqualTo("APPOINTMENT_RESCHEDULED");
        assertThat(event.timestamp).isNotNull();
        assertThat(event.appointmentId).isEqualTo(appointmentId);
        assertThat(event.patientId).isEqualTo(patientId);
        assertThat(event.doctorId).isEqualTo(doctorId);
        assertThat(event.oldStartTime).isEqualTo(oldStart);
        assertThat(event.oldEndTime).isEqualTo(oldEnd);
        assertThat(event.newStartTime).isEqualTo(newStart);
        assertThat(event.newEndTime).isEqualTo(newEnd);
        assertThat(event.rescheduledAt).isEqualTo(rescheduledAt);
    }

    // ==================== TEST 5: AppointmentCompletedEvent ====================

    @Test
    @Order(5)
    @DisplayName("Test 5: Should create AppointmentCompletedEvent with all fields")
    void shouldCreateAppointmentCompletedEvent() {
        // Given
        Long appointmentId = 1L;
        Long patientId = 100L;
        Long doctorId = 200L;
        LocalDateTime completedAt = LocalDateTime.now();
        LocalDateTime scheduledTime = LocalDateTime.now().minusHours(1);
        Integer duration = 60;

        // When
        AppointmentCompletedEvent event = new AppointmentCompletedEvent(
                appointmentId, patientId, doctorId, completedAt, scheduledTime, duration
        );

        // Then
        assertThat(event.eventId).isNotNull();
        assertThat(event.eventType).isEqualTo("APPOINTMENT_COMPLETED");
        assertThat(event.timestamp).isNotNull();
        assertThat(event.appointmentId).isEqualTo(appointmentId);
        assertThat(event.patientId).isEqualTo(patientId);
        assertThat(event.doctorId).isEqualTo(doctorId);
        assertThat(event.completedAt).isEqualTo(completedAt);
        assertThat(event.scheduledTime).isEqualTo(scheduledTime);
        assertThat(event.durationMinutes).isEqualTo(duration);
    }

    // ==================== TEST 6: Event ID Generation ====================

    @Test
    @Order(6)
    @DisplayName("Test 6: Should generate unique event IDs")
    void shouldGenerateUniqueEventIds() {
        // When
        AppointmentCreatedEvent event1 = new AppointmentCreatedEvent(
                1L, 100L, 200L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1),
                AppointmentType.CONSULTATION,
                "Test"
        );

        AppointmentCreatedEvent event2 = new AppointmentCreatedEvent(
                2L, 100L, 200L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(1),
                AppointmentType.CONSULTATION,
                "Test"
        );

        // Then
        assertThat(event1.eventId).isNotEqualTo(event2.eventId);
    }

    // ==================== TEST 7: JSON Serialization - Created Event ====================

    @Test
    @Order(7)
    @DisplayName("Test 7: Should serialize AppointmentCreatedEvent to JSON")
    void shouldSerializeCreatedEventToJson() throws Exception {
        // Given
        AppointmentCreatedEvent event = new AppointmentCreatedEvent(
                1L, 100L, 200L,
                LocalDateTime.of(2025, 12, 10, 10, 0),
                LocalDateTime.of(2025, 12, 10, 11, 0),
                AppointmentType.CONSULTATION,
                "Annual checkup"
        );

        // When
        String json = objectMapper.writeValueAsString(event);

        // Then
        assertThat(json).isNotNull();
        assertThat(json).contains("\"eventType\":\"APPOINTMENT_CREATED\"");
        assertThat(json).contains("\"appointmentId\":1");
        assertThat(json).contains("\"patientId\":100");
        assertThat(json).contains("\"doctorId\":200");
        assertThat(json).contains("\"reason\":\"Annual checkup\"");
    }

    // ==================== TEST 8: JSON Deserialization ====================

    @Test
    @Order(8)
    @DisplayName("Test 8: Should deserialize JSON to AppointmentCreatedEvent")
    void shouldDeserializeJsonToCreatedEvent() throws Exception {
        // Given
        String json = "{"
                + "\"eventId\":\"test-123\","
                + "\"eventType\":\"APPOINTMENT_CREATED\","
                + "\"timestamp\":\"2025-12-05T10:00:00\","
                + "\"appointmentId\":1,"
                + "\"patientId\":100,"
                + "\"doctorId\":200,"
                + "\"startTime\":\"2025-12-10T10:00:00\","
                + "\"endTime\":\"2025-12-10T11:00:00\","
                + "\"type\":\"CONSULTATION\","
                + "\"reason\":\"Test\""
                + "}";

        // When
        AppointmentEvent event = objectMapper.readValue(json, AppointmentEvent.class);

        // Then
        assertThat(event).isInstanceOf(AppointmentCreatedEvent.class);
        AppointmentCreatedEvent createdEvent = (AppointmentCreatedEvent) event;
        assertThat(createdEvent.appointmentId).isEqualTo(1L);
        assertThat(createdEvent.patientId).isEqualTo(100L);
        assertThat(createdEvent.doctorId).isEqualTo(200L);
        assertThat(createdEvent.type).isEqualTo(AppointmentType.CONSULTATION);
    }

    // ==================== TEST 9: Event Producer - Publish Created Event ====================

    @Test
    @Order(9)
    @DisplayName("Test 9: Should publish AppointmentCreatedEvent without errors")
    void shouldPublishCreatedEvent() {
        // Given
        AppointmentCreatedEvent event = new AppointmentCreatedEvent(
                1L, 100L, 200L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1),
                AppointmentType.CONSULTATION,
                "Test appointment"
        );

        // When/Then - Should not throw exception
        // In test mode, uses in-memory connector
        eventProducer.publishCreatedEvent(event);

        // If we reach here, publishing was successful
        assertThat(event.eventId).isNotNull();
    }

    // ==================== TEST 10: Event Producer - Publish All Event Types ====================

    @Test
    @Order(10)
    @DisplayName("Test 10: Should publish all event types without errors")
    void shouldPublishAllEventTypes() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(1);

        AppointmentCreatedEvent createdEvent = new AppointmentCreatedEvent(
                1L, 100L, 200L, future, future.plusHours(1), AppointmentType.CONSULTATION, "Test"
        );

        AppointmentConfirmedEvent confirmedEvent = new AppointmentConfirmedEvent(
                1L, 100L, 200L, now, future
        );

        AppointmentCancelledEvent cancelledEvent = new AppointmentCancelledEvent(
                1L, 100L, 200L, now, "Patient sick", future
        );

        AppointmentRescheduledEvent rescheduledEvent = new AppointmentRescheduledEvent(
                1L, 100L, 200L, future, future.plusHours(1),
                future.plusDays(1), future.plusDays(1).plusHours(1), now
        );

        AppointmentCompletedEvent completedEvent = new AppointmentCompletedEvent(
                1L, 100L, 200L, now, now.minusHours(1), 60
        );

        // When/Then - Should not throw exceptions
        eventProducer.publishCreatedEvent(createdEvent);
        eventProducer.publishConfirmedEvent(confirmedEvent);
        eventProducer.publishCancelledEvent(cancelledEvent);
        eventProducer.publishRescheduledEvent(rescheduledEvent);
        eventProducer.publishCompletedEvent(completedEvent);

        // If we reach here, all publishing was successful
        assertThat(createdEvent.eventId).isNotNull();
        assertThat(confirmedEvent.eventId).isNotNull();
        assertThat(cancelledEvent.eventId).isNotNull();
        assertThat(rescheduledEvent.eventId).isNotNull();
        assertThat(completedEvent.eventId).isNotNull();
    }
}


