package com.basit.cz.patient;

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
 * End-to-End Integration Tests for Patient Service.
 *
 * These tests verify complete user workflows:
 * - Patient registration → update → search → deactivation
 * - Error handling workflows
 * - Data persistence verification
 *
 * Unlike unit tests, these test the entire stack:
 * - REST API layer
 * - Service layer
 * - Repository layer
 * - Database persistence
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PatientWorkflowIntegrationTest {

    private Long patient1Id;
    private Long patient2Id;
    private Long patient3Id;

    @BeforeAll
    void setupWorkflowTests() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚀 INTEGRATION TESTS - Patient Workflow Scenarios");
        System.out.println("=".repeat(60));
    }

    /**
     * WORKFLOW 1: Complete Patient Lifecycle
     * Register → Get → Update → Search → Deactivate
     */
    @Test
    @Order(1)
    void workflow1_CompletePatientLifecycle() {
        System.out.println("\n📋 WORKFLOW 1: Complete Patient Lifecycle");
        System.out.println("-".repeat(60));

        // Step 1: Register new patient
        System.out.println("Step 1: Registering patient...");
        PatientDTO.RegistrationRequest registerRequest = new PatientDTO.RegistrationRequest();
        registerRequest.firstName = "Alice";
        registerRequest.lastName = "Johnson";
        registerRequest.email = "alice.johnson@email.com";
        registerRequest.phoneNumber = "+420111222333";
        registerRequest.dateOfBirth = LocalDate.of(1990, 5, 15);
        registerRequest.gender = Patient.Gender.FEMALE;
        registerRequest.address = "123 Healthcare St, Brno";
        registerRequest.emergencyContactName = "Bob Johnson";
        registerRequest.emergencyContactPhone = "+420444555666";

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .when()
                .post("/api/patients/register")
                .then()
                .statusCode(201)
                .body("firstName", equalTo("Alice"))
                .body("email", equalTo("alice.johnson@email.com"))
                .body("isActive", equalTo(true))
                .extract()
                .path("id");

        patient1Id = id.longValue();
        System.out.println("✅ Patient registered with ID: " + patient1Id);

        // Step 2: Retrieve the patient
        System.out.println("Step 2: Retrieving patient...");
        given()
                .when()
                .get("/api/patients/" + patient1Id)
                .then()
                .statusCode(200)
                .body("id", equalTo(patient1Id.intValue()))
                .body("firstName", equalTo("Alice"))
                .body("address", equalTo("123 Healthcare St, Brno"));

        System.out.println("✅ Patient retrieved successfully");

        // Step 3: Update patient information
        System.out.println("Step 3: Updating patient...");
        PatientDTO.UpdateRequest updateRequest = new PatientDTO.UpdateRequest();
        updateRequest.firstName = "Alice";
        updateRequest.lastName = "Johnson-Smith";  // Changed last name
        updateRequest.email = "alice.johnson@email.com";
        updateRequest.phoneNumber = "+420999888777";  // Changed phone
        updateRequest.dateOfBirth = LocalDate.of(1990, 5, 15);
        updateRequest.gender = Patient.Gender.FEMALE;
        updateRequest.address = "456 New Address, Prague";  // Changed address
        updateRequest.emergencyContactName = "Bob Johnson";
        updateRequest.emergencyContactPhone = "+420444555666";

        given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/api/patients/" + patient1Id)
                .then()
                .statusCode(200)
                .body("lastName", equalTo("Johnson-Smith"))
                .body("phoneNumber", equalTo("+420999888777"))
                .body("address", equalTo("456 New Address, Prague"));

        System.out.println("✅ Patient updated successfully");

        // Step 4: Search for the patient
        System.out.println("Step 4: Searching for patient...");
        given()
                .queryParam("q", "alice")
                .when()
                .get("/api/patients/search")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].lastName", equalTo("Johnson-Smith"));

        System.out.println("✅ Patient found in search");

        // Step 5: Verify patient count increased
        System.out.println("Step 5: Checking patient count...");
        given()
                .when()
                .get("/api/patients/count")
                .then()
                .statusCode(200)
                .body("count", greaterThan(0));

        System.out.println("✅ Patient count verified");

        // Step 6: Deactivate patient
        System.out.println("Step 6: Deactivating patient...");
        given()
                .when()
                .delete("/api/patients/" + patient1Id)
                .then()
                .statusCode(204);

        System.out.println("✅ Patient deactivated");

        // Step 7: Verify patient is no longer accessible
        System.out.println("Step 7: Verifying patient is inactive...");
        given()
                .when()
                .get("/api/patients/" + patient1Id)
                .then()
                .statusCode(404);

        System.out.println("✅ Patient correctly returns 404 after deactivation");
        System.out.println("🎉 WORKFLOW 1 COMPLETED SUCCESSFULLY!\n");
    }

    /**
     * WORKFLOW 2: Duplicate Email Handling
     * Register patient 1 → Try to register patient 2 with same email → Should fail
     */
    @Test
    @Order(2)
    void workflow2_DuplicateEmailHandling() {
        System.out.println("\n📋 WORKFLOW 2: Duplicate Email Handling");
        System.out.println("-".repeat(60));

        // Step 1: Register first patient
        System.out.println("Step 1: Registering first patient...");
        PatientDTO.RegistrationRequest request1 = new PatientDTO.RegistrationRequest();
        request1.firstName = "Charlie";
        request1.lastName = "Brown";
        request1.email = "charlie.brown@email.com";
        request1.phoneNumber = "+420111111111";
        request1.dateOfBirth = LocalDate.of(1985, 3, 10);
        request1.gender = Patient.Gender.MALE;

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(request1)
                .when()
                .post("/api/patients/register")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        patient2Id = id.longValue();
        System.out.println("✅ First patient registered with ID: " + patient2Id);

        // Step 2: Try to register second patient with duplicate email
        System.out.println("Step 2: Attempting to register with duplicate email...");
        PatientDTO.RegistrationRequest request2 = new PatientDTO.RegistrationRequest();
        request2.firstName = "Different";
        request2.lastName = "Person";
        request2.email = "charlie.brown@email.com";  // Same email!
        request2.phoneNumber = "+420222222222";
        request2.dateOfBirth = LocalDate.of(1990, 1, 1);
        request2.gender = Patient.Gender.FEMALE;

        given()
                .contentType(ContentType.JSON)
                .body(request2)
                .when()
                .post("/api/patients/register")
                .then()
                .statusCode(409)  // Conflict
                .body("error", equalTo("Conflict"))
                .body("message", containsString("charlie.brown@email.com"));

        System.out.println("✅ Duplicate email correctly rejected with 409");

        // Step 3: Verify only one patient exists with that email
        System.out.println("Step 3: Verifying database integrity...");
        given()
                .queryParam("q", "charlie")
                .when()
                .get("/api/patients/search")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].firstName", equalTo("Charlie"));

        System.out.println("✅ Database integrity maintained");
        System.out.println("🎉 WORKFLOW 2 COMPLETED SUCCESSFULLY!\n");
    }

    /**
     * WORKFLOW 3: Multiple Patient Management
     * Register multiple patients → List all → Search → Verify counts
     */
    @Test
    @Order(3)
    void workflow3_MultiplePatientManagement() {
        System.out.println("\n📋 WORKFLOW 3: Multiple Patient Management");
        System.out.println("-".repeat(60));

        // Step 1: Get initial count
        System.out.println("Step 1: Getting initial patient count...");
        Integer initialCount = given()
                .when()
                .get("/api/patients/count")
                .then()
                .statusCode(200)
                .extract()
                .path("count");

        System.out.println("✅ Initial count: " + initialCount);

        // Step 2: Register multiple patients
        System.out.println("Step 2: Registering multiple patients...");

        String[] patients = {
                "David:Miller:david.miller@email.com",
                "Emma:Wilson:emma.wilson@email.com",
                "Frank:Moore:frank.moore@email.com"
        };

        for (String patientData : patients) {
            String[] parts = patientData.split(":");
            PatientDTO.RegistrationRequest request = new PatientDTO.RegistrationRequest();
            request.firstName = parts[0];
            request.lastName = parts[1];
            request.email = parts[2];
            request.phoneNumber = "+420" + (int)(Math.random() * 1000000000);
            request.dateOfBirth = LocalDate.of(1990, 1, 1);
            request.gender = Patient.Gender.OTHER;

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/patients/register")
                    .then()
                    .statusCode(201);

            System.out.println("✅ Registered: " + parts[0] + " " + parts[1]);
        }

        // Step 3: Verify count increased
        System.out.println("Step 3: Verifying patient count increased...");
        given()
                .when()
                .get("/api/patients/count")
                .then()
                .statusCode(200)
                .body("count", greaterThan(initialCount));

        System.out.println("✅ Patient count increased correctly");

        // Step 4: Get all active patients
        System.out.println("Step 4: Retrieving all active patients...");
        given()
                .when()
                .get("/api/patients")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));

        System.out.println("✅ Retrieved all active patients");

        // Step 5: Search for specific patient
        System.out.println("Step 5: Searching for 'Emma'...");
        given()
                .queryParam("q", "emma")
                .when()
                .get("/api/patients/search")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].firstName", equalTo("Emma"));

        System.out.println("✅ Search successful");
        System.out.println("🎉 WORKFLOW 3 COMPLETED SUCCESSFULLY!\n");
    }

    /**
     * WORKFLOW 4: Update Validation
     * Register patient → Try invalid update → Try duplicate email update → Valid update
     */
    @Test
    @Order(4)
    void workflow4_UpdateValidation() {
        System.out.println("\n📋 WORKFLOW 4: Update Validation");
        System.out.println("-".repeat(60));

        // Step 1: Register patient to update
        System.out.println("Step 1: Registering patient...");
        PatientDTO.RegistrationRequest registerRequest = new PatientDTO.RegistrationRequest();
        registerRequest.firstName = "Grace";
        registerRequest.lastName = "Taylor";
        registerRequest.email = "grace.taylor@email.com";
        registerRequest.phoneNumber = "+420333333333";
        registerRequest.dateOfBirth = LocalDate.of(1992, 7, 20);
        registerRequest.gender = Patient.Gender.FEMALE;

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .when()
                .post("/api/patients/register")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        patient3Id = id.longValue();
        System.out.println("✅ Patient registered with ID: " + patient3Id);

        // Step 2: Try to update with email that belongs to another patient
        System.out.println("Step 2: Attempting update with duplicate email...");
        PatientDTO.UpdateRequest duplicateEmailRequest = new PatientDTO.UpdateRequest();
        duplicateEmailRequest.firstName = "Grace";
        duplicateEmailRequest.lastName = "Taylor";
        duplicateEmailRequest.email = "charlie.brown@email.com";  // Belongs to patient2
        duplicateEmailRequest.phoneNumber = "+420333333333";
        duplicateEmailRequest.dateOfBirth = LocalDate.of(1992, 7, 20);
        duplicateEmailRequest.gender = Patient.Gender.FEMALE;

        given()
                .contentType(ContentType.JSON)
                .body(duplicateEmailRequest)
                .when()
                .put("/api/patients/" + patient3Id)
                .then()
                .statusCode(409)  // Conflict
                .body("error", equalTo("Conflict"));

        System.out.println("✅ Duplicate email update correctly rejected");

        // Step 3: Valid update with same email (should work)
        System.out.println("Step 3: Updating with same email (should succeed)...");
        PatientDTO.UpdateRequest sameEmailRequest = new PatientDTO.UpdateRequest();
        sameEmailRequest.firstName = "Grace";
        sameEmailRequest.lastName = "Taylor-Anderson";  // Changed
        sameEmailRequest.email = "grace.taylor@email.com";  // Same email
        sameEmailRequest.phoneNumber = "+420777777777";  // Changed
        sameEmailRequest.dateOfBirth = LocalDate.of(1992, 7, 20);
        sameEmailRequest.gender = Patient.Gender.FEMALE;

        given()
                .contentType(ContentType.JSON)
                .body(sameEmailRequest)
                .when()
                .put("/api/patients/" + patient3Id)
                .then()
                .statusCode(200)
                .body("lastName", equalTo("Taylor-Anderson"))
                .body("phoneNumber", equalTo("+420777777777"));

        System.out.println("✅ Update with same email succeeded");
        System.out.println("🎉 WORKFLOW 4 COMPLETED SUCCESSFULLY!\n");
    }

    /**
     * WORKFLOW 5: Search Functionality
     * Test various search scenarios
     */
    @Test
    @Order(5)
    void workflow5_SearchFunctionality() {
        System.out.println("\n📋 WORKFLOW 5: Search Functionality");
        System.out.println("-".repeat(60));

        // Step 1: Search by first name
        System.out.println("Step 1: Searching by first name 'David'...");
        given()
                .queryParam("q", "david")
                .when()
                .get("/api/patients/search")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));

        System.out.println("✅ First name search successful");

        // Step 2: Search by last name
        System.out.println("Step 2: Searching by last name 'Miller'...");
        given()
                .queryParam("q", "miller")
                .when()
                .get("/api/patients/search")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));

        System.out.println("✅ Last name search successful");

        // Step 3: Search with no results
        System.out.println("Step 3: Searching for non-existent patient...");
        given()
                .queryParam("q", "NonExistentPatient12345")
                .when()
                .get("/api/patients/search")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));

        System.out.println("✅ Empty search results handled correctly");

        // Step 4: Search without query parameter (should fail)
        System.out.println("Step 4: Testing search without query parameter...");
        given()
                .when()
                .get("/api/patients/search")
                .then()
                .statusCode(400);

        System.out.println("✅ Missing query parameter correctly rejected");
        System.out.println("🎉 WORKFLOW 5 COMPLETED SUCCESSFULLY!\n");
    }

    @AfterAll
    void teardownWorkflowTests() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ ALL INTEGRATION WORKFLOWS COMPLETED SUCCESSFULLY!");
        System.out.println("=".repeat(60) + "\n");
    }
}



