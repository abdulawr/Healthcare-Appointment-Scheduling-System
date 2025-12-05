package com.basit.cz.resource;

import com.basit.cz.dto.CreateDoctorRequest;
import com.basit.cz.dto.UpdateDoctorRequest;
import com.basit.cz.entity.Doctor;
import com.basit.cz.entity.DoctorAvailability;
import com.basit.cz.entity.DoctorReview;
import com.basit.cz.entity.DoctorSchedule;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *                  DOCTOR RESOURCE TESTS - STEP 4
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Tests all REST API endpoints for DoctorResource.
 * Uses REST-assured for HTTP testing.
 *
 * ENDPOINTS TESTED (16):
 * 1. POST   /api/doctors/register
 * 2. GET    /api/doctors/{id}
 * 3. PUT    /api/doctors/{id}
 * 4. DELETE /api/doctors/{id}
 * 5. GET    /api/doctors
 * 6. GET    /api/doctors/search?q=
 * 7. GET    /api/doctors/specialization/{specialization}
 * 8. GET    /api/doctors/top-rated
 * 9. GET    /api/doctors/rating/{rating}
 * 10. GET   /api/doctors/experience/{years}
 * 11. GET   /api/doctors/available/{day}
 * 12. GET   /api/doctors/fee-range?min=&max=
 * 13. GET   /api/doctors/specializations
 * 14. GET   /api/doctors/statistics
 * 15. POST  /api/doctors/{id}/activate
 * 16. POST  /api/doctors/{id}/deactivate
 */
@QuarkusTest
@DisplayName("REST API Tests - DoctorResource")
public class DoctorResourceTest {

    private Long testDoctorId;

    @BeforeEach
    @Transactional
    public void setup() {
        // Clean database
        DoctorSchedule.deleteAll();
        DoctorReview.deleteAll();
        DoctorAvailability.deleteAll();
        Doctor.deleteAll();

        // Create test doctors and store ID
        testDoctorId = createTestDoctors();
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 1: POST /api/doctors/register - Register Doctor
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 1: Register new doctor - Success")
    public void testRegisterDoctor_Success() {
        CreateDoctorRequest request = new CreateDoctorRequest();
        request.firstName = "Alice";
        request.lastName = "Johnson";
        request.email = "alice.johnson@hospital.com";
        request.phoneNumber = "+420999888777";
        request.specialization = "Dermatology";
        request.yearsOfExperience = 8;
        request.licenseNumber = "LIC-999";
        request.consultationFee = 95.0;

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/doctors/register")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("firstName", equalTo("Alice"))
                .body("lastName", equalTo("Johnson"))
                .body("fullName", equalTo("Alice Johnson"))
                .body("email", equalTo("alice.johnson@hospital.com"))
                .body("specialization", equalTo("Dermatology"))
                .body("yearsOfExperience", equalTo(8))
                .body("consultationFee", equalTo(95.0f))
                .body("averageRating", equalTo(0.0f))
                .body("totalReviews", equalTo(0))
                .body("isActive", equalTo(true));
    }

    @Test
    @DisplayName("TEST 2: Register doctor - Duplicate email")
    public void testRegisterDoctor_DuplicateEmail() {
        CreateDoctorRequest request = new CreateDoctorRequest();
        request.firstName = "Test";
        request.lastName = "Doctor";
        request.email = "john.cardio@hospital.com"; // Already exists
        request.phoneNumber = "+420999888777";
        request.specialization = "Dermatology";
        request.yearsOfExperience = 5;

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/doctors/register")
                .then()
                .statusCode(400)
                .body("message", containsString("Email already registered"));
    }

    @Test
    @DisplayName("TEST 3: Register doctor - Invalid input")
    public void testRegisterDoctor_InvalidInput() {
        CreateDoctorRequest request = new CreateDoctorRequest();
        // Missing required fields
        request.firstName = "Test";
        // Missing lastName, email, etc.

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/doctors/register")
                .then()
                .statusCode(400);
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 4-6: GET /api/doctors/{id} - Get Doctor by ID
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 4: Get doctor by ID - Success")
    public void testGetDoctorById_Success() {
        given()
                .when()
                .get("/api/doctors/" + testDoctorId)
                .then()
                .statusCode(200)
                .body("id", equalTo(testDoctorId.intValue()))
                .body("firstName", notNullValue())
                .body("lastName", notNullValue())
                .body("fullName", notNullValue())
                .body("email", notNullValue())
                .body("specialization", notNullValue());
    }

    @Test
    @DisplayName("TEST 5: Get doctor by ID - Not found")
    public void testGetDoctorById_NotFound() {
        given()
                .when()
                .get("/api/doctors/9999")
                .then()
                .statusCode(404)
                .body("message", containsString("Doctor not found"));
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 6-8: PUT /api/doctors/{id} - Update Doctor
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 6: Update doctor - Success")
    public void testUpdateDoctor_Success() {
        UpdateDoctorRequest request = new UpdateDoctorRequest();
        request.consultationFee = 150.0;
        request.bio = "Updated bio";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/api/doctors/" + testDoctorId)
                .then()
                .statusCode(200)
                .body("id", equalTo(testDoctorId.intValue()))
                .body("consultationFee", equalTo(150.0f))
                .body("bio", equalTo("Updated bio"));
    }

    @Test
    @DisplayName("TEST 7: Update doctor - Not found")
    public void testUpdateDoctor_NotFound() {
        UpdateDoctorRequest request = new UpdateDoctorRequest();
        request.consultationFee = 150.0;

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/api/doctors/9999")
                .then()
                .statusCode(404);
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 8-9: DELETE /api/doctors/{id} - Deactivate Doctor
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 8: Deactivate doctor - Success")
    public void testDeactivateDoctor_Success() {
        given()
                .when()
                .delete("/api/doctors/" + testDoctorId)
                .then()
                .statusCode(204);

        // Verify doctor is deactivated
        given()
                .when()
                .get("/api/doctors/" + testDoctorId)
                .then()
                .statusCode(200)
                .body("isActive", equalTo(false));
    }

    @Test
    @DisplayName("TEST 9: Deactivate doctor - Not found")
    public void testDeactivateDoctor_NotFound() {
        given()
                .when()
                .delete("/api/doctors/9999")
                .then()
                .statusCode(404);
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 10: GET /api/doctors - Get All Active Doctors
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 10: Get all active doctors")
    public void testGetAllActiveDoctors() {
        given()
                .when()
                .get("/api/doctors")
                .then()
                .statusCode(200)
                .body("$", hasSize(4)) // 4 active doctors from setup
                .body("[0].id", notNullValue())
                .body("[0].firstName", notNullValue())
                .body("[0].isActive", equalTo(true));
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 11-12: GET /api/doctors/search - Search by Name
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 11: Search doctors by name - Success")
    public void testSearchByName_Success() {
        given()
                .queryParam("q", "john")
                .when()
                .get("/api/doctors/search")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].firstName", anyOf(
                        containsString("John"),
                        containsString("john")
                ));
    }

    @Test
    @DisplayName("TEST 12: Search doctors - Missing query")
    public void testSearchByName_MissingQuery() {
        given()
                .when()
                .get("/api/doctors/search")
                .then()
                .statusCode(400)
                .body(containsString("Search query is required"));
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 13: GET /api/doctors/specialization/{spec}
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 13: Find by specialization")
    public void testFindBySpecialization() {
        given()
                .when()
                .get("/api/doctors/specialization/Cardiology")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].specialization", equalTo("Cardiology"))
                .body("[1].specialization", equalTo("Cardiology"));
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 14: GET /api/doctors/top-rated
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 14: Get top-rated doctors")
    public void testFindTopRated() {
        given()
                .when()
                .get("/api/doctors/top-rated")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].averageRating", greaterThan(4.0f));
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 15: GET /api/doctors/rating/{rating}
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 15: Find by minimum rating")
    public void testFindByMinimumRating() {
        given()
                .when()
                .get("/api/doctors/rating/4.5")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)));
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 16: GET /api/doctors/experience/{years}
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 16: Find by minimum experience")
    public void testFindByMinimumExperience() {
        given()
                .when()
                .get("/api/doctors/experience/15")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].yearsOfExperience", greaterThan(14));
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 17-18: GET /api/doctors/available/{day}
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 17: Find available on day - Success")
    public void testFindAvailableOnDay_Success() {
        given()
                .when()
                .get("/api/doctors/available/MONDAY")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)));
    }

    @Test
    @DisplayName("TEST 18: Find available on day - Invalid day")
    public void testFindAvailableOnDay_InvalidDay() {
        given()
                .when()
                .get("/api/doctors/available/INVALIDDAY")
                .then()
                .statusCode(400)
                .body(containsString("Invalid day of week"));
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 19-20: GET /api/doctors/fee-range
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 19: Find by fee range - Success")
    public void testFindByFeeRange_Success() {
        given()
                .queryParam("min", 80)
                .queryParam("max", 120)
                .when()
                .get("/api/doctors/fee-range")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)));
    }

    @Test
    @DisplayName("TEST 20: Find by fee range - Invalid range")
    public void testFindByFeeRange_InvalidRange() {
        given()
                .queryParam("min", 150)
                .queryParam("max", 100) // max < min
                .when()
                .get("/api/doctors/fee-range")
                .then()
                .statusCode(400)
                .body(containsString("Invalid fee range"));
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 21: GET /api/doctors/specializations
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 21: Get all specializations")
    public void testGetAllSpecializations() {
        given()
                .when()
                .get("/api/doctors/specializations")
                .then()
                .statusCode(200)
                .body("$", hasSize(3))
                .body("$", hasItems("Cardiology", "Neurology", "Pediatrics"));
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 22: GET /api/doctors/statistics
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 22: Get doctor statistics")
    public void testGetStatistics() {
        given()
                .when()
                .get("/api/doctors/statistics")
                .then()
                .statusCode(200)
                .body("totalDoctors", equalTo(4))
                .body("averageRating", notNullValue())
                .body("averageExperience", notNullValue());
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 23-24: POST /api/doctors/{id}/activate
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 23: Activate doctor - Success")
    public void testActivateDoctor_Success() {
        // First verify doctor exists and is active
        given()
                .when()
                .get("/api/doctors/" + testDoctorId)
                .then()
                .statusCode(200)
                .body("isActive", equalTo(true));

        // Deactivate via POST endpoint
        given()
                .when()
                .post("/api/doctors/" + testDoctorId + "/deactivate")
                .then()
                .statusCode(204);

        // Verify deactivated
        given()
                .when()
                .get("/api/doctors/" + testDoctorId)
                .then()
                .statusCode(200)
                .body("isActive", equalTo(false));

        // Then activate
        given()
                .when()
                .post("/api/doctors/" + testDoctorId + "/activate")
                .then()
                .statusCode(204);

        // Verify activated
        given()
                .when()
                .get("/api/doctors/" + testDoctorId)
                .then()
                .statusCode(200)
                .body("isActive", equalTo(true));
    }

    @Test
    @DisplayName("TEST 24: Activate doctor - Not found")
    public void testActivateDoctor_NotFound() {
        given()
                .when()
                .post("/api/doctors/9999/activate")
                .then()
                .statusCode(404);
    }

    // ═══════════════════════════════════════════════════════════
    // TEST 25: POST /api/doctors/{id}/deactivate (alternative)
    // ═══════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 25: Deactivate via POST endpoint")
    public void testDeactivateDoctorViaPost_Success() {
        // Verify doctor is initially active
        given()
                .when()
                .get("/api/doctors/" + testDoctorId)
                .then()
                .statusCode(200)
                .body("isActive", equalTo(true));

        // Deactivate
        given()
                .when()
                .post("/api/doctors/" + testDoctorId + "/deactivate")
                .then()
                .statusCode(204);

        // Verify deactivated
        given()
                .when()
                .get("/api/doctors/" + testDoctorId)
                .then()
                .statusCode(200)
                .body("isActive", equalTo(false));
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHOD: Create Test Data
    // ═══════════════════════════════════════════════════════════
    @Transactional
    Long createTestDoctors() {
        // Doctor 1: Cardiologist, experienced, top-rated
        Doctor d1 = new Doctor();
        d1.firstName = "John";
        d1.lastName = "Cardio";
        d1.email = "john.cardio@hospital.com";
        d1.phoneNumber = "+420111111111";
        d1.specialization = "Cardiology";
        d1.yearsOfExperience = 20;
        d1.licenseNumber = "LIC-001";
        d1.consultationFee = 100.0;
        d1.averageRating = 4.8;
        d1.totalReviews = 50;
        d1.isActive = true;
        d1.persist();

        // Add availability for Monday
        DoctorAvailability avail1 = new DoctorAvailability();
        avail1.doctor = d1;
        avail1.dayOfWeek = "MONDAY";
        avail1.startTime = LocalTime.of(9, 0);
        avail1.endTime = LocalTime.of(17, 0);
        avail1.persist();

        // Doctor 2: Cardiologist, less experienced
        Doctor d2 = new Doctor();
        d2.firstName = "Jane";
        d2.lastName = "Smith";
        d2.email = "jane.smith@hospital.com";
        d2.phoneNumber = "+420222222222";
        d2.specialization = "Cardiology";
        d2.yearsOfExperience = 10;
        d2.licenseNumber = "LIC-002";
        d2.consultationFee = 85.0;
        d2.averageRating = 4.2;
        d2.totalReviews = 30;
        d2.isActive = true;
        d2.persist();

        // Doctor 3: Neurologist
        Doctor d3 = new Doctor();
        d3.firstName = "Bob";
        d3.lastName = "Neuro";
        d3.email = "bob.neuro@hospital.com";
        d3.phoneNumber = "+420333333333";
        d3.specialization = "Neurology";
        d3.yearsOfExperience = 15;
        d3.licenseNumber = "LIC-003";
        d3.consultationFee = 110.0;
        d3.averageRating = 4.5;
        d3.totalReviews = 40;
        d3.isActive = true;
        d3.persist();

        // Doctor 4: Pediatrician
        Doctor d4 = new Doctor();
        d4.firstName = "Alice";
        d4.lastName = "Pediatrics";
        d4.email = "alice.pediatrics@hospital.com";
        d4.phoneNumber = "+420444444444";
        d4.specialization = "Pediatrics";
        d4.yearsOfExperience = 8;
        d4.licenseNumber = "LIC-004";
        d4.consultationFee = 75.0;
        d4.averageRating = 3.9;
        d4.totalReviews = 25;
        d4.isActive = true;
        d4.persist();

        // Return the first doctor's ID for tests
        return d1.id;
    }
}