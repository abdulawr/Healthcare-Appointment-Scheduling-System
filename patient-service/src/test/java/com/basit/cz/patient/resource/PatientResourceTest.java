package com.basit.cz.patient.resource;

import com.basit.cz.dto.PatientDTO;
import com.basit.cz.entity.Patient;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

/**
 * REST API tests for PatientResource.
 *
 * These are integration tests that:
 * - Start the full Quarkus application
 * - Use a real H2 database
 * - Test complete HTTP request/response cycles
 *
 * Note: Tests run in order and share the same database instance.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PatientResourceTest {

    private static Long createdPatientId;

    @BeforeAll
    void setupClass() {
        System.out.println("\n🚀 Starting PatientResourceTest");
        System.out.println("Database will persist across all tests in this class");
    }

    /**
     * TEST 1: Register a new patient
     * POST /api/patients/register
     */
    @Test
    @Order(1)
    void testRegisterPatient_WithValidData_ShouldReturn201() {
        System.out.println("\n✅ TEST 1: POST /api/patients/register (valid data)");

        PatientDTO.RegistrationRequest request = new PatientDTO.RegistrationRequest();
        request.firstName = "Integration";
        request.lastName = "Test";
        request.email = "integration.test@email.com";
        request.phoneNumber = "+420999888777";
        request.dateOfBirth = LocalDate.of(1995, 6, 15);
        request.gender = Patient.Gender.FEMALE;
        request.address = "Test Street 123, Brno";
        request.emergencyContactName = "Emergency Contact";
        request.emergencyContactPhone = "+420111222333";

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/patients/register")
                .then()
                .statusCode(201)
                .body("firstName", equalTo("Integration"))
                .body("lastName", equalTo("Test"))
                .body("email", equalTo("integration.test@email.com"))
                .body("isActive", equalTo(true))
                .body("id", notNullValue())
                .extract()
                .path("id");

        createdPatientId = id.longValue();  // Convert Integer to Long

        System.out.println("✅ Patient registered with ID: " + createdPatientId);
    }

    /**
     * TEST 2: Register patient with duplicate email
     * POST /api/patients/register
     */
    @Test
    @Order(2)
    void testRegisterPatient_WithDuplicateEmail_ShouldReturn409() {
        System.out.println("\n❌ TEST 2: POST /api/patients/register (duplicate email)");

        PatientDTO.RegistrationRequest request = new PatientDTO.RegistrationRequest();
        request.firstName = "Duplicate";
        request.lastName = "User";
        request.email = "integration.test@email.com";  // Same as TEST 1
        request.phoneNumber = "+420111111111";
        request.dateOfBirth = LocalDate.of(1990, 1, 1);
        request.gender = Patient.Gender.MALE;

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/patients/register")
                .then()
                .statusCode(409)
                .body("error", equalTo("Conflict"))
                .body("message", containsString("integration.test@email.com"));

        System.out.println("✅ Correctly returned 409 Conflict");
    }

    /**
     * TEST 3: Register patient with invalid data
     * POST /api/patients/register
     */
    @Test
    @Order(3)
    void testRegisterPatient_WithInvalidData_ShouldReturn400() {
        System.out.println("\n❌ TEST 3: POST /api/patients/register (invalid data)");

        PatientDTO.RegistrationRequest request = new PatientDTO.RegistrationRequest();
        // Missing required fields!
        request.firstName = "";  // Blank
        request.email = "not-an-email";  // Invalid email

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/patients/register")
                .then()
                .statusCode(400);  // Bad Request due to validation

        System.out.println("✅ Correctly returned 400 Bad Request");
    }

    /**
     * TEST 4: Get patient by ID
     * GET /api/patients/{id}
     */
    @Test
    @Order(4)
    void testGetPatient_WhenPatientExists_ShouldReturn200() {
        System.out.println("\n🔍 TEST 4: GET /api/patients/{id} (exists)");

        given()
                .when()
                .get("/api/patients/" + createdPatientId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdPatientId.intValue()))
                .body("firstName", equalTo("Integration"))
                .body("email", equalTo("integration.test@email.com"))
                .body("isActive", equalTo(true));

        System.out.println("✅ Found patient with ID: " + createdPatientId);
    }

    /**
     * TEST 5: Get patient that doesn't exist
     * GET /api/patients/{id}
     */
    @Test
    @Order(5)
    void testGetPatient_WhenPatientNotFound_ShouldReturn404() {
        System.out.println("\n❌ TEST 5: GET /api/patients/{id} (not found)");

        given()
                .when()
                .get("/api/patients/99999")
                .then()
                .statusCode(404)
                .body("error", equalTo("Not Found"))
                .body("message", containsString("99999"));

        System.out.println("✅ Correctly returned 404 Not Found");
    }

    /**
     * TEST 6: Update patient
     * PUT /api/patients/{id}
     */
    @Test
    @Order(6)
    void testUpdatePatient_WithValidData_ShouldReturn200() {
        System.out.println("\n📝 TEST 6: PUT /api/patients/{id} (valid data)");

        PatientDTO.UpdateRequest request = new PatientDTO.UpdateRequest();
        request.firstName = "Updated";
        request.lastName = "Name";
        request.email = "updated.email@email.com";  // New email
        request.phoneNumber = "+420555444333";
        request.dateOfBirth = LocalDate.of(1995, 6, 15);
        request.gender = Patient.Gender.FEMALE;

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/api/patients/" + createdPatientId)
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Updated"))
                .body("email", equalTo("updated.email@email.com"));

        System.out.println("✅ Patient updated successfully");
    }

    /**
     * TEST 7: Update patient with duplicate email
     * PUT /api/patients/{id}
     */
    @Test
    @Order(7)
    void testUpdatePatient_WithDuplicateEmail_ShouldReturn409() {
        System.out.println("\n❌ TEST 7: PUT /api/patients/{id} (duplicate email)");

        // First, create another patient
        PatientDTO.RegistrationRequest registerRequest = new PatientDTO.RegistrationRequest();
        registerRequest.firstName = "Another";
        registerRequest.lastName = "Patient";
        registerRequest.email = "another@email.com";
        registerRequest.phoneNumber = "+420666777888";
        registerRequest.dateOfBirth = LocalDate.of(1990, 1, 1);
        registerRequest.gender = Patient.Gender.MALE;

        Integer anotherId = given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .when()
                .post("/api/patients/register")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Try to update first patient with second patient's email
        PatientDTO.UpdateRequest updateRequest = new PatientDTO.UpdateRequest();
        updateRequest.firstName = "Updated";
        updateRequest.lastName = "Name";
        updateRequest.email = "another@email.com";  // Duplicate!
        updateRequest.phoneNumber = "+420555444333";
        updateRequest.dateOfBirth = LocalDate.of(1995, 6, 15);
        updateRequest.gender = Patient.Gender.FEMALE;

        given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/api/patients/" + createdPatientId)
                .then()
                .statusCode(409)
                .body("error", equalTo("Conflict"));

        System.out.println("✅ Correctly returned 409 Conflict");
    }

    /**
     * TEST 8: Search patients
     * GET /api/patients/search?q={term}
     */
    @Test
    @Order(8)
    void testSearchPatients_ShouldReturnMatchingPatients() {
        System.out.println("\n🔍 TEST 8: GET /api/patients/search?q=update");

        given()
                .queryParam("q", "updated")
                .when()
                .get("/api/patients/search")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].firstName", equalTo("Updated"));

        System.out.println("✅ Search returned matching patients");
    }

    /**
     * TEST 9: Search without query parameter
     * GET /api/patients/search
     */
    @Test
    @Order(9)
    void testSearchPatients_WithoutQueryParam_ShouldReturn400() {
        System.out.println("\n❌ TEST 9: GET /api/patients/search (no query)");

        given()
                .when()
                .get("/api/patients/search")
                .then()
                .statusCode(400)
                .body(containsString("Search term"));

        System.out.println("✅ Correctly returned 400 Bad Request");
    }

    /**
     * TEST 10: Get all active patients
     * GET /api/patients
     */
    @Test
    @Order(10)
    void testGetAllActivePatients_ShouldReturnList() {
        System.out.println("\n📋 TEST 10: GET /api/patients");

        given()
                .when()
                .get("/api/patients")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));

        System.out.println("✅ Retrieved all active patients");
    }

    /**
     * TEST 11: Get active patient count
     * GET /api/patients/count
     */
    @Test
    @Order(11)
    void testGetActivePatientCount_ShouldReturnCount() {
        System.out.println("\n📊 TEST 11: GET /api/patients/count");

        given()
                .when()
                .get("/api/patients/count")
                .then()
                .statusCode(200)
                .body("count", greaterThan(0));

        System.out.println("✅ Retrieved patient count");
    }

    /**
     * TEST 12: Deactivate patient
     * DELETE /api/patients/{id}
     */
    @Test
    @Order(12)
    void testDeactivatePatient_ShouldReturn204() {
        System.out.println("\n🗑️ TEST 12: DELETE /api/patients/{id}");

        given()
                .when()
                .delete("/api/patients/" + createdPatientId)
                .then()
                .statusCode(204);

        // Verify patient is now inactive (should return 404)
        given()
                .when()
                .get("/api/patients/" + createdPatientId)
                .then()
                .statusCode(404);

        System.out.println("✅ Patient deactivated successfully");
    }

    /**
     * TEST 13: Health check
     * GET /api/patients/health
     */
    @Test
    @Order(13)
    void testHealthCheck_ShouldReturn200() {
        System.out.println("\n💚 TEST 13: GET /api/patients/health");

        given()
                .when()
                .get("/api/patients/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("service", equalTo("patient-service"));

        System.out.println("✅ Health check passed");
    }
}


