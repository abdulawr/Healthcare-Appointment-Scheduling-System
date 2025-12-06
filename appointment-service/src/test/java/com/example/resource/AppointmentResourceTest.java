package com.example.resource;

import com.example.constant.AppointmentType;
import com.example.dto.CreateAppointmentRequest;
import com.example.dto.UpdateAppointmentRequest;
import com.example.entity.Appointment;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Appointment REST API endpoints
 * Tests: 22 test cases covering all 14 endpoints
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppointmentResourceTest {

    // Counter to ensure unique appointment times
    private static int appointmentCounter = 0;

    // No @BeforeEach cleanup needed
    // Each test uses unique IDs to avoid conflicts
    // QuarkusTest handles transaction isolation per test method

    // ==================== ENDPOINT 1: POST /api/appointments ====================

    @Test
    @Order(1)
    @DisplayName("Test 1: POST - Should create appointment and return 201")
    void shouldCreateAppointmentAndReturn201() {
        CreateAppointmentRequest request = createValidRequest();

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/appointments")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("patientId", notNullValue())
                .body("doctorId", notNullValue())
                .body("status", equalTo("SCHEDULED"))
                .body("type", equalTo("CONSULTATION"));
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: POST - Should return 400 for missing patient ID")
    void shouldReturn400ForMissingPatientId() {
        CreateAppointmentRequest request = createValidRequest();
        request.patientId = null;

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/appointments")
                .then()
                .statusCode(400)
                .body("error", notNullValue());
    }



    @Test
    @Order(3)
    @DisplayName("Test 3: POST - Should return 409 for unavailable doctor")
    void shouldReturn409ForUnavailableDoctor() {
        // Create first appointment
        CreateAppointmentRequest first = createValidRequest();
        createAppointment(first);

        // Try to book SAME doctor at SAME time (different patient)
        CreateAppointmentRequest overlapping = new CreateAppointmentRequest(
                999L,  // Different patient
                first.doctorId,  // SAME doctor
                first.startTime,  // SAME time
                first.endTime,
                first.type,
                "Conflicting appointment"
        );

        given()
                .contentType(ContentType.JSON)
                .body(overlapping)
                .when()
                .post("/api/appointments")
                .then()
                .statusCode(409)
                .body("error", equalTo("Conflict"));
    }

    // ==================== ENDPOINT 2: GET /api/appointments/{id} ====================

    @Test
    @Order(4)
    @DisplayName("Test 4: GET - Should get appointment by ID and return 200")
    void shouldGetAppointmentByIdAndReturn200() {
        // Create appointment first
        CreateAppointmentRequest request = createValidRequest();
        Long id = createAppointment(request);

        given()
                .when()
                .get("/api/appointments/" + id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id.intValue()))
                .body("patientId", notNullValue())
                .body("doctorId", notNullValue());
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: GET - Should return 404 for non-existent ID")
    void shouldReturn404ForNonExistentId() {
        given()
                .when()
                .get("/api/appointments/99999")
                .then()
                .statusCode(404)
                .body("error", equalTo("Not Found"));
    }

    // ==================== ENDPOINT 3: PUT /api/appointments/{id} ====================

    @Test
    @Order(6)
    @DisplayName("Test 6: PUT - Should reschedule appointment and return 200")
    void shouldRescheduleAppointmentAndReturn200() {
        // Create appointment
        Long id = createAppointment(createValidRequest());

        // Reschedule
        UpdateAppointmentRequest update = new UpdateAppointmentRequest(
                LocalDateTime.now().plusDays(5).withHour(14).withMinute(0),
                LocalDateTime.now().plusDays(5).withHour(15).withMinute(0)
        );

        given()
                .contentType(ContentType.JSON)
                .body(update)
                .when()
                .put("/api/appointments/" + id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id.intValue()));
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: PUT - Should return 404 for non-existent appointment")
    void shouldReturn404WhenReschedulingNonExistent() {
        UpdateAppointmentRequest update = new UpdateAppointmentRequest(
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(5).plusHours(1)
        );

        given()
                .contentType(ContentType.JSON)
                .body(update)
                .when()
                .put("/api/appointments/99999")
                .then()
                .statusCode(404);
    }

    // ==================== ENDPOINT 4: DELETE /api/appointments/{id} ====================

    @Test
    @Order(8)
    @DisplayName("Test 8: DELETE - Should cancel appointment and return 204")
    void shouldCancelAppointmentAndReturn204() {
        // Create appointment
        Long id = createAppointment(createValidRequest());

        given()
                .queryParam("reason", "Patient is sick")
                .when()
                .delete("/api/appointments/" + id)
                .then()
                .statusCode(204);

        // Verify it's cancelled
        given()
                .when()
                .get("/api/appointments/" + id)
                .then()
                .statusCode(200)
                .body("status", equalTo("CANCELLED"));
    }

    // ==================== ENDPOINT 5: GET /api/appointments ====================

    @Test
    @Order(9)
    @DisplayName("Test 9: GET - Should list all appointments")
    void shouldListAllAppointments() {
        // Create multiple appointments
        createAppointment(createValidRequest());

        CreateAppointmentRequest second = createValidRequest();
        second.startTime = LocalDateTime.now().plusDays(2);
        second.endTime = LocalDateTime.now().plusDays(2).plusHours(1);
        createAppointment(second);

        given()
                .when()
                .get("/api/appointments")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }

    // ==================== ENDPOINT 6: GET /api/appointments/upcoming ====================

    @Test
    @Order(10)
    @DisplayName("Test 10: GET - Should get upcoming appointments")
    void shouldGetUpcomingAppointments() {
        createAppointment(createValidRequest());

        given()
                .when()
                .get("/api/appointments/upcoming")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    // ==================== ENDPOINT 7: GET /api/appointments/patient/{id} ====================

    @Test
    @Order(11)
    @DisplayName("Test 11: GET - Should get patient appointments")
    void shouldGetPatientAppointments() {
        Long patientId = 100L;
        CreateAppointmentRequest request = createValidRequest();
        request.patientId = patientId;
        createAppointment(request);

        given()
                .when()
                .get("/api/appointments/patient/" + patientId)
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].patientId", equalTo(patientId.intValue()));
    }

    // ==================== ENDPOINT 8: GET /api/appointments/doctor/{id} ====================

    @Test
    @Order(12)
    @DisplayName("Test 12: GET - Should get doctor appointments")
    void shouldGetDoctorAppointments() {
        Long doctorId = 200L;
        CreateAppointmentRequest request = createValidRequest();
        request.doctorId = doctorId;
        createAppointment(request);

        given()
                .when()
                .get("/api/appointments/doctor/" + doctorId)
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].doctorId", equalTo(doctorId.intValue()));
    }

    // ==================== ENDPOINT 9: POST /api/appointments/{id}/confirm ====================

    @Test
    @Order(13)
    @DisplayName("Test 13: POST - Should confirm appointment")
    void shouldConfirmAppointment() {
        Long id = createAppointment(createValidRequest());

        // Verify appointment exists first
        given()
                .when()
                .get("/api/appointments/" + id)
                .then()
                .statusCode(200);

        // Now try to confirm
        given()
                .when()
                .post("/api/appointments/" + id + "/confirm")
                .then()
                .log().ifError()  // Log response if error
                .statusCode(200)
                .body("status", equalTo("CONFIRMED"))
                .body("confirmationSent", equalTo(true));
    }

    @Test
    @Order(14)
    @DisplayName("Test 14: POST - Should return 400 when confirming cancelled appointment")
    void shouldReturn400WhenConfirmingCancelled() {
        Long id = createAppointment(createValidRequest());

        // Cancel it first
        given()
                .queryParam("reason", "Test")
                .when()
                .delete("/api/appointments/" + id);

        // Try to confirm
        given()
                .when()
                .post("/api/appointments/" + id + "/confirm")
                .then()
                .statusCode(400);
    }

    // ==================== ENDPOINT 10: POST /api/appointments/{id}/check-in ====================

    @Test
    @Order(15)
    @DisplayName("Test 15: POST - Should check-in appointment")
    void shouldCheckInAppointment() {
        Long id = createAppointment(createValidRequest());

        given()
                .when()
                .post("/api/appointments/" + id + "/check-in")
                .then()
                .statusCode(200)
                .body("status", equalTo("CHECKED_IN"))
                .body("checkedInAt", notNullValue());
    }

    // ==================== ENDPOINT 11: POST /api/appointments/{id}/complete ====================

    @Test
    @Order(16)
    @DisplayName("Test 16: POST - Should complete appointment")
    void shouldCompleteAppointment() {
        Long id = createAppointment(createValidRequest());

        given()
                .when()
                .post("/api/appointments/" + id + "/complete")
                .then()
                .statusCode(200)
                .body("status", equalTo("COMPLETED"))
                .body("completedAt", notNullValue());
    }

    @Test
    @Order(17)
    @DisplayName("Test 17: POST - Should return 400 when completing cancelled appointment")
    void shouldReturn400WhenCompletingCancelled() {
        Long id = createAppointment(createValidRequest());

        // Cancel it
        given()
                .queryParam("reason", "Test")
                .when()
                .delete("/api/appointments/" + id);

        // Try to complete
        given()
                .when()
                .post("/api/appointments/" + id + "/complete")
                .then()
                .statusCode(400);
    }

    // ==================== ENDPOINT 12: GET /api/appointments/available-slots ====================

    @Test
    @Order(18)
    @DisplayName("Test 18: GET - Should get available slots")
    void shouldGetAvailableSlots() {
        given()
                .queryParam("doctorId", 1)
                .queryParam("date", "2025-12-15")
                .queryParam("duration", 30)
                .when()
                .get("/api/appointments/available-slots")
                .then()
                .statusCode(200)
                .body("doctorId", equalTo(1))
                .body("slots", notNullValue());
    }

    // ==================== ENDPOINT 13: POST /api/appointments/waiting-list ====================

    @Test
    @Order(19)
    @DisplayName("Test 19: POST - Should join waiting list")
    void shouldJoinWaitingList() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"patientId\": 1, \"doctorId\": 2, \"preferredDate\": \"2025-12-15\"}")
                .when()
                .post("/api/appointments/waiting-list")
                .then()
                .statusCode(201)
                .body("message", notNullValue());
    }

    // ==================== ENDPOINT 14: GET /api/appointments/statistics ====================

    @Test
    @Order(20)
    @DisplayName("Test 20: GET - Should get statistics")
    void shouldGetStatistics() {
        given()
                .queryParam("startDate", "2025-01-01")
                .queryParam("endDate", "2025-12-31")
                .when()
                .get("/api/appointments/statistics")
                .then()
                .statusCode(200)
                .body("totalAppointments", notNullValue())
                .body("byStatus", notNullValue());
    }

    // ==================== HEALTH CHECK ====================

    @Test
    @Order(21)
    @DisplayName("Test 21: GET - Health check should return 200")
    void shouldReturnHealthCheck() {
        given()
                .when()
                .get("/api/appointments/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("service", equalTo("appointment-service"));
    }

    // ==================== ERROR HANDLING ====================

    @Test
    @Order(22)
    @DisplayName("Test 22: Should handle validation errors properly")
    void shouldHandleValidationErrors() {
        // Send empty body
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post("/api/appointments")
                .then()
                .statusCode(400);
    }

    // ==================== Helper Methods ====================

    private CreateAppointmentRequest createValidRequest() {
        // Use timestamp-based unique IDs
        long uniqueId = System.currentTimeMillis() % 1000000;

        // Use counter to ensure unique appointment times
        // Each appointment gets a different hour slot
        int hourOffset = appointmentCounter++;

        LocalDateTime startTime = LocalDateTime.now()
                .plusDays(1 + (hourOffset / 8))  // Spread across multiple days if needed
                .withHour(10 + (hourOffset % 8))  // Hours 10-17
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        LocalDateTime endTime = startTime.plusHours(1);

        return new CreateAppointmentRequest(
                uniqueId,      // Unique patient ID
                uniqueId + 1,  // Unique doctor ID
                startTime,
                endTime,
                AppointmentType.CONSULTATION,
                "Regular checkup"
        );
    }

    /**
     * Helper method to create an appointment via REST API
     * Returns the ID of the created appointment
     */
    private Long createAppointment(CreateAppointmentRequest request) {
        Integer id = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/appointments")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        return id.longValue();
    }
}






