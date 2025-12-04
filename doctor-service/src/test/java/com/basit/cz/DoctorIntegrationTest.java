package com.basit.cz;


import com.basit.cz.dto.CreateDoctorRequest;
import com.basit.cz.entity.Doctor;
import com.basit.cz.entity.DoctorAvailability;
import com.basit.cz.entity.DoctorReview;
import com.basit.cz.entity.DoctorSchedule;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *                  INTEGRATION TESTS - STEP 5
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * End-to-end integration tests that verify complete workflows.
 * Tests multiple endpoints in realistic scenarios.
 *
 * TEST SCENARIOS:
 * 1. Complete Doctor Lifecycle (register → update → deactivate → activate)
 * 2. Search and Filter Workflow (register multiple → search → filter)
 * 3. Doctor Management Workflow (bulk operations)
 * 4. Error Handling Workflow (invalid operations)
 * 5. Statistics and Analytics (register → verify stats)
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Integration Tests - End-to-End Workflows")
public class DoctorIntegrationTest {

    private static Long doctorId1;
    private static Long doctorId2;
    private static Long doctorId3;

    @BeforeEach
    @Transactional
    public void setup() {
        // Clean database before each test
        DoctorSchedule.deleteAll();
        DoctorReview.deleteAll();
        DoctorAvailability.deleteAll();
        Doctor.deleteAll();
    }

    // ═══════════════════════════════════════════════════════════════
    // SCENARIO 1: Complete Doctor Lifecycle
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("SCENARIO 1: Complete Doctor Lifecycle")
    public void testCompleteDoctorLifecycle() {
        // Step 1: Register a new doctor
        CreateDoctorRequest request = new CreateDoctorRequest();
        request.firstName = "John";
        request.lastName = "Smith";
        request.email = "john.smith@hospital.com";
        request.phoneNumber = "+420111222333";
        request.specialization = "Cardiology";
        request.yearsOfExperience = 15;
        request.licenseNumber = "MED-12345";
        request.consultationFee = 100.0;
        request.bio = "Experienced cardiologist";

        Response registerResponse = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/doctors/register")
                .then()
                .statusCode(201)
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Smith"))
                .body("isActive", equalTo(true))
                .extract().response();

        Long doctorId = registerResponse.jsonPath().getLong("id");

        // Step 2: Retrieve the doctor
        given()
                .when()
                .get("/api/doctors/" + doctorId)
                .then()
                .statusCode(200)
                .body("id", equalTo(doctorId.intValue()))
                .body("fullName", equalTo("John Smith"))
                .body("email", equalTo("john.smith@hospital.com"))
                .body("specialization", equalTo("Cardiology"));

        // Step 3: Update the doctor
        given()
                .contentType(ContentType.JSON)
                .body("{\"consultationFee\": 120.0, \"bio\": \"Updated bio\"}")
                .when()
                .put("/api/doctors/" + doctorId)
                .then()
                .statusCode(200)
                .body("consultationFee", equalTo(120.0f))
                .body("bio", equalTo("Updated bio"));

        // Step 4: Verify update persisted
        given()
                .when()
                .get("/api/doctors/" + doctorId)
                .then()
                .statusCode(200)
                .body("consultationFee", equalTo(120.0f));

        // Step 5: Deactivate the doctor
        given()
                .when()
                .delete("/api/doctors/" + doctorId)
                .then()
                .statusCode(204);

        // Step 6: Verify deactivation
        given()
                .when()
                .get("/api/doctors/" + doctorId)
                .then()
                .statusCode(200)
                .body("isActive", equalTo(false));

        // Step 7: Verify doctor not in active list
        given()
                .when()
                .get("/api/doctors")
                .then()
                .statusCode(200)
                .body("$", hasSize(0)); // No active doctors

        // Step 8: Reactivate the doctor
        given()
                .when()
                .post("/api/doctors/" + doctorId + "/activate")
                .then()
                .statusCode(204);

        // Step 9: Verify reactivation
        given()
                .when()
                .get("/api/doctors/" + doctorId)
                .then()
                .statusCode(200)
                .body("isActive", equalTo(true));

        // Step 10: Verify doctor back in active list
        given()
                .when()
                .get("/api/doctors")
                .then()
                .statusCode(200)
                .body("$", hasSize(1));
    }

    // ═══════════════════════════════════════════════════════════════
    // SCENARIO 2: Search and Filter Workflow
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(2)
    @DisplayName("SCENARIO 2: Search and Filter Workflow")
    public void testSearchAndFilterWorkflow() {
        // Step 1: Register multiple doctors with different attributes

        // Doctor 1: Cardiologist, experienced, high rating
        CreateDoctorRequest req1 = new CreateDoctorRequest();
        req1.firstName = "John";
        req1.lastName = "Cardio";
        req1.email = "john.cardio@hospital.com";
        req1.phoneNumber = "+420111111111";
        req1.specialization = "Cardiology";
        req1.yearsOfExperience = 20;
        req1.consultationFee = 100.0;

        doctorId1 = given()
                .contentType(ContentType.JSON)
                .body(req1)
                .when()
                .post("/api/doctors/register")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // Doctor 2: Neurologist, less experienced
        CreateDoctorRequest req2 = new CreateDoctorRequest();
        req2.firstName = "Jane";
        req2.lastName = "Neuro";
        req2.email = "jane.neuro@hospital.com";
        req2.phoneNumber = "+420222222222";
        req2.specialization = "Neurology";
        req2.yearsOfExperience = 10;
        req2.consultationFee = 85.0;

        doctorId2 = given()
                .contentType(ContentType.JSON)
                .body(req2)
                .when()
                .post("/api/doctors/register")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // Doctor 3: Another Cardiologist
        CreateDoctorRequest req3 = new CreateDoctorRequest();
        req3.firstName = "Bob";
        req3.lastName = "Heart";
        req3.email = "bob.heart@hospital.com";
        req3.phoneNumber = "+420333333333";
        req3.specialization = "Cardiology";
        req3.yearsOfExperience = 15;
        req3.consultationFee = 110.0;

        doctorId3 = given()
                .contentType(ContentType.JSON)
                .body(req3)
                .when()
                .post("/api/doctors/register")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // Step 2: Get all doctors
        given()
                .when()
                .get("/api/doctors")
                .then()
                .statusCode(200)
                .body("$", hasSize(3));

        // Step 3: Search by name
        given()
                .queryParam("q", "john")
                .when()
                .get("/api/doctors/search")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].firstName", equalTo("John"));

        // Step 4: Filter by specialization
        given()
                .when()
                .get("/api/doctors/specialization/Cardiology")
                .then()
                .statusCode(200)
                .body("$", hasSize(2));

        // Step 5: Filter by experience
        given()
                .when()
                .get("/api/doctors/experience/15")
                .then()
                .statusCode(200)
                .body("$", hasSize(2)); // John (20) and Bob (15)

        // Step 6: Filter by fee range
        given()
                .queryParam("min", 90)
                .queryParam("max", 120)
                .when()
                .get("/api/doctors/fee-range")
                .then()
                .statusCode(200)
                .body("$", hasSize(2)); // John (100) and Bob (110)

        // Step 7: Get all specializations
        given()
                .when()
                .get("/api/doctors/specializations")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("$", hasItems("Cardiology", "Neurology"));

        // Step 8: Get statistics
        given()
                .when()
                .get("/api/doctors/statistics")
                .then()
                .statusCode(200)
                .body("totalDoctors", equalTo(3))
                .body("averageExperience", notNullValue());
    }

    // ═══════════════════════════════════════════════════════════════
    // SCENARIO 3: Bulk Operations Workflow
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(3)
    @DisplayName("SCENARIO 3: Bulk Operations Workflow")
    public void testBulkOperationsWorkflow() {
        // Register 5 doctors
        for (int i = 1; i <= 5; i++) {
            CreateDoctorRequest req = new CreateDoctorRequest();
            req.firstName = "Doctor" + i;
            req.lastName = "Test" + i;
            req.email = "doctor" + i + "@hospital.com";
            req.phoneNumber = "+42011122233" + i;
            req.specialization = (i % 2 == 0) ? "Cardiology" : "Neurology";
            req.yearsOfExperience = 10 + i;
            req.consultationFee = 80.0 + (i * 10);

            given()
                    .contentType(ContentType.JSON)
                    .body(req)
                    .when()
                    .post("/api/doctors/register")
                    .then()
                    .statusCode(201);
        }

        // Verify all created
        given()
                .when()
                .get("/api/doctors")
                .then()
                .statusCode(200)
                .body("$", hasSize(5));

        // Get cardiologists (should be 2)
        given()
                .when()
                .get("/api/doctors/specialization/Cardiology")
                .then()
                .statusCode(200)
                .body("$", hasSize(2));

        // Get neurologists (should be 3)
        given()
                .when()
                .get("/api/doctors/specialization/Neurology")
                .then()
                .statusCode(200)
                .body("$", hasSize(3));
    }

    // ═══════════════════════════════════════════════════════════════
    // SCENARIO 4: Error Handling Workflow
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(4)
    @DisplayName("SCENARIO 4: Error Handling Workflow")
    public void testErrorHandlingWorkflow() {
        // Step 1: Try to register with missing fields
        given()
                .contentType(ContentType.JSON)
                .body("{\"firstName\": \"John\"}")
                .when()
                .post("/api/doctors/register")
                .then()
                .statusCode(400);

        // Step 2: Register a valid doctor
        CreateDoctorRequest req = new CreateDoctorRequest();
        req.firstName = "John";
        req.lastName = "Smith";
        req.email = "john@hospital.com";
        req.phoneNumber = "+420111222333";
        req.specialization = "Cardiology";
        req.yearsOfExperience = 10;

        Long doctorId = given()
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/api/doctors/register")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        // Step 3: Try to register with same email
        given()
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/api/doctors/register")
                .then()
                .statusCode(400)
                .body("message", containsString("Email already registered"));

        // Step 4: Try to get non-existent doctor
        given()
                .when()
                .get("/api/doctors/99999")
                .then()
                .statusCode(404)
                .body("message", containsString("Doctor not found"));

        // Step 5: Try to update non-existent doctor
        given()
                .contentType(ContentType.JSON)
                .body("{\"consultationFee\": 100.0}")
                .when()
                .put("/api/doctors/99999")
                .then()
                .statusCode(404);

        // Step 6: Try to activate non-existent doctor
        given()
                .when()
                .post("/api/doctors/99999/activate")
                .then()
                .statusCode(404);

        // Step 7: Try invalid search
        given()
                .when()
                .get("/api/doctors/search")
                .then()
                .statusCode(400)
                .body(containsString("Search query is required"));

        // Step 8: Try invalid fee range
        given()
                .queryParam("min", 150)
                .queryParam("max", 100)
                .when()
                .get("/api/doctors/fee-range")
                .then()
                .statusCode(400)
                .body(containsString("Invalid fee range"));
    }

    // ═══════════════════════════════════════════════════════════════
    // SCENARIO 5: Statistics Workflow
    // ═══════════════════════════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("SCENARIO 5: Statistics and Analytics Workflow")
    public void testStatisticsWorkflow() {
        // Initial state: no doctors
        given()
                .when()
                .get("/api/doctors/statistics")
                .then()
                .statusCode(200)
                .body("totalDoctors", equalTo(0));

        // Register first doctor
        CreateDoctorRequest req1 = new CreateDoctorRequest();
        req1.firstName = "Doctor1";
        req1.lastName = "Test1";
        req1.email = "doctor1@hospital.com";
        req1.phoneNumber = "+420111111111";
        req1.specialization = "Cardiology";
        req1.yearsOfExperience = 10;

        given()
                .contentType(ContentType.JSON)
                .body(req1)
                .when()
                .post("/api/doctors/register")
                .then()
                .statusCode(201);

        // Check statistics
        given()
                .when()
                .get("/api/doctors/statistics")
                .then()
                .statusCode(200)
                .body("totalDoctors", equalTo(1))
                .body("averageExperience", equalTo(10.0f));

        // Register second doctor
        CreateDoctorRequest req2 = new CreateDoctorRequest();
        req2.firstName = "Doctor2";
        req2.lastName = "Test2";
        req2.email = "doctor2@hospital.com";
        req2.phoneNumber = "+420222222222";
        req2.specialization = "Neurology";
        req2.yearsOfExperience = 20;

        given()
                .contentType(ContentType.JSON)
                .body(req2)
                .when()
                .post("/api/doctors/register")
                .then()
                .statusCode(201);

        // Check updated statistics
        given()
                .when()
                .get("/api/doctors/statistics")
                .then()
                .statusCode(200)
                .body("totalDoctors", equalTo(2))
                .body("averageExperience", equalTo(15.0f)); // (10+20)/2 = 15
    }
}










