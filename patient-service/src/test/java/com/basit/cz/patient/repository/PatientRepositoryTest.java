package com.basit.cz.patient.repository;

import com.basit.cz.entity.Patient;
import com.basit.cz.repository.PatientRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PatientRepository custom query methods.
 *
 * These tests verify that all custom queries work correctly.
 * Each test creates test data, executes the query, and verifies the result.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PatientRepositoryTest {

    @Inject
    PatientRepository patientRepository;

    /**
     * Clean database before each test to ensure isolation.
     */
    @BeforeEach
    @Transactional
    void setUp() {
        // Delete all patients to start fresh
        patientRepository.deleteAll();
        System.out.println("🧹 Database cleaned");
    }

    @Test
    @Order(1)
    @Transactional
    void testFindByEmail_WhenPatientExists_ShouldReturnPatient() {
        System.out.println("\n📧 TEST 1: Find by email (exists)");

        // Given - Create a patient with specific email
        Patient patient = createPatient(
                "John",
                "Doe",
                "john.doe@email.com",
                "+420111222333"
        );
        patientRepository.persist(patient);

        // When - Search by email
        Optional<Patient> result = patientRepository.findByEmail("john.doe@email.com");

        // Then - Should find the patient
        assertTrue(result.isPresent(), "Patient should be found");
        assertEquals("John", result.get().firstName);
        assertEquals("Doe", result.get().lastName);

        System.out.println("✅ Found patient: " + result.get());
    }

    @Test
    @Order(2)
    @Transactional
    void testFindByEmail_WhenPatientDoesNotExist_ShouldReturnEmpty() {
        System.out.println("\n📧 TEST 2: Find by email (not exists)");

        // Given - Empty database (cleaned in setUp)

        // When - Search for non-existent email
        Optional<Patient> result = patientRepository.findByEmail("nonexistent@email.com");

        // Then - Should return empty
        assertFalse(result.isPresent(), "Should not find any patient");

        System.out.println("✅ Correctly returned empty Optional");
    }

    @Test
    @Order(3)
    @Transactional
    void testSearchByName_ShouldFindMatchingPatients() {
        System.out.println("\n🔍 TEST 3: Search by name");

        // Given - Create multiple patients with different names
        patientRepository.persist(createPatient("John", "Doe", "john@email.com", "+420111"));
        patientRepository.persist(createPatient("Jane", "Smith", "jane@email.com", "+420222"));
        patientRepository.persist(createPatient("Johnny", "Walker", "johnny@email.com", "+420333"));
        patientRepository.persist(createPatient("Bob", "Johnson", "bob@email.com", "+420444"));

        // When - Search for "john" (should find John, Johnny, AND Johnson - 3 total)
        List<Patient> results = patientRepository.searchByName("john");

        // Then - Should find 3 patients (John Doe, Johnny Walker, Bob Johnson)
        assertEquals(3, results.size(), "Should find 3 patients with 'john' in name (including Johnson)");
        assertTrue(
                results.stream().anyMatch(p -> p.firstName.equals("John")),
                "Should find John Doe"
        );
        assertTrue(
                results.stream().anyMatch(p -> p.firstName.equals("Johnny")),
                "Should find Johnny Walker"
        );
        assertTrue(
                results.stream().anyMatch(p -> p.lastName.equals("Johnson")),
                "Should find Bob Johnson"
        );

        System.out.println("✅ Found " + results.size() + " patients matching 'john'");
        results.forEach(p -> System.out.println("   - " + p.firstName + " " + p.lastName));
    }

    @Test
    @Order(4)
    @Transactional
    void testSearchByName_CaseInsensitive_ShouldWork() {
        System.out.println("\n🔍 TEST 4: Search by name (case insensitive)");

        // Given
        patientRepository.persist(createPatient("Alice", "Wonder", "alice@email.com", "+420555"));

        // When - Search with different cases
        List<Patient> upperCase = patientRepository.searchByName("ALICE");
        List<Patient> lowerCase = patientRepository.searchByName("alice");
        List<Patient> mixedCase = patientRepository.searchByName("AlIcE");

        // Then - All should find the patient
        assertEquals(1, upperCase.size(), "Upper case should find patient");
        assertEquals(1, lowerCase.size(), "Lower case should find patient");
        assertEquals(1, mixedCase.size(), "Mixed case should find patient");

        System.out.println("✅ Case-insensitive search works correctly");
    }

    @Test
    @Order(5)
    @Transactional
    void testSearchByName_SearchByLastName_ShouldWork() {
        System.out.println("\n🔍 TEST 5: Search by last name");

        // Given
        patientRepository.persist(createPatient("Bob", "Smith", "bob.smith@email.com", "+420111"));
        patientRepository.persist(createPatient("Alice", "Smith", "alice.smith@email.com", "+420222"));
        patientRepository.persist(createPatient("John", "Doe", "john.doe@email.com", "+420333"));

        // When - Search by last name "smith"
        List<Patient> results = patientRepository.searchByName("smith");

        // Then - Should find both Smiths
        assertEquals(2, results.size(), "Should find 2 patients with last name Smith");
        assertTrue(
                results.stream().allMatch(p -> p.lastName.equals("Smith")),
                "All results should have last name Smith"
        );

        System.out.println("✅ Found " + results.size() + " patients with last name 'Smith'");
    }

    @Test
    @Order(6)
    @Transactional
    void testFindAllActive_ShouldReturnOnlyActivePatients() {
        System.out.println("\n✅ TEST 6: Find all active patients");

        // Given - Create active and inactive patients
        Patient active1 = createPatient("Active", "One", "active1@email.com", "+420111");
        active1.isActive = true;
        patientRepository.persist(active1);

        Patient active2 = createPatient("Active", "Two", "active2@email.com", "+420222");
        active2.isActive = true;
        patientRepository.persist(active2);

        Patient inactive = createPatient("Inactive", "Patient", "inactive@email.com", "+420333");
        inactive.isActive = false;
        patientRepository.persist(inactive);

        // When - Get all active patients
        List<Patient> activePatients = patientRepository.findAllActive();

        // Then - Should only return active patients
        assertEquals(2, activePatients.size(), "Should find 2 active patients");
        assertTrue(
                activePatients.stream().allMatch(p -> p.isActive),
                "All returned patients should be active"
        );

        System.out.println("✅ Found " + activePatients.size() + " active patients");
        activePatients.forEach(p -> System.out.println("   - " + p.firstName + " " + p.lastName));
    }

    @Test
    @Order(7)
    @Transactional
    void testFindActiveById_WhenPatientIsActive_ShouldReturnPatient() {
        System.out.println("\n🔎 TEST 7: Find active patient by ID");

        // Given - Create active patient
        Patient patient = createPatient("Active", "Patient", "active@email.com", "+420111");
        patient.isActive = true;
        patientRepository.persist(patient);
        Long patientId = patient.id;

        // When - Find by ID (only active)
        Optional<Patient> result = patientRepository.findActiveById(patientId);

        // Then - Should find the patient
        assertTrue(result.isPresent(), "Should find active patient");
        assertEquals("Active", result.get().firstName);

        System.out.println("✅ Found active patient: " + result.get());
    }

    @Test
    @Order(8)
    @Transactional
    void testFindActiveById_WhenPatientIsInactive_ShouldReturnEmpty() {
        System.out.println("\n❌ TEST 8: Find active patient by ID (patient is inactive)");

        // Given - Create inactive patient
        Patient patient = createPatient("Inactive", "Patient", "inactive@email.com", "+420111");
        patient.isActive = false;
        patientRepository.persist(patient);
        Long patientId = patient.id;

        // When - Try to find by ID (only active)
        Optional<Patient> result = patientRepository.findActiveById(patientId);

        // Then - Should NOT find the patient (because inactive)
        assertFalse(result.isPresent(), "Should not find inactive patient");

        System.out.println("✅ Correctly did not return inactive patient");
    }

    @Test
    @Order(9)
    @Transactional
    void testCountActiveAndInactive_ShouldReturnCorrectCounts() {
        System.out.println("\n📊 TEST 9: Count active and inactive patients");

        // Given - Create 3 active and 2 inactive patients
        for (int i = 1; i <= 3; i++) {
            Patient active = createPatient("Active", "Patient" + i, "active" + i + "@email.com", "+42011" + i);
            active.isActive = true;
            patientRepository.persist(active);
        }

        for (int i = 1; i <= 2; i++) {
            Patient inactive = createPatient("Inactive", "Patient" + i, "inactive" + i + "@email.com", "+42022" + i);
            inactive.isActive = false;
            patientRepository.persist(inactive);
        }

        // When - Count active and inactive
        long activeCount = patientRepository.countActive();
        long inactiveCount = patientRepository.countInactive();
        long totalCount = patientRepository.count();

        // Then - Verify counts
        assertEquals(3, activeCount, "Should have 3 active patients");
        assertEquals(2, inactiveCount, "Should have 2 inactive patients");
        assertEquals(5, totalCount, "Should have 5 total patients");

        System.out.println("✅ Counts verified:");
        System.out.println("   - Active: " + activeCount);
        System.out.println("   - Inactive: " + inactiveCount);
        System.out.println("   - Total: " + totalCount);
    }

    @Test
    @Order(10)
    @Transactional
    void testEmailExists_ShouldReturnTrueIfExists() {
        System.out.println("\n📧 TEST 10: Check if email exists");

        // Given
        patientRepository.persist(createPatient("John", "Doe", "exists@email.com", "+420111"));

        // When & Then
        assertTrue(patientRepository.emailExists("exists@email.com"), "Email should exist");
        assertFalse(patientRepository.emailExists("notexists@email.com"), "Email should not exist");

        System.out.println("✅ Email existence check works correctly");
    }

    @Test
    @Order(11)
    @Transactional
    void testFindByGender_ShouldReturnPatientsOfSpecificGender() {
        System.out.println("\n👤 TEST 11: Find patients by gender");

        // Given - Create patients with different genders
        Patient male1 = createPatient("John", "Doe", "john@email.com", "+420111");
        male1.gender = Patient.Gender.MALE;
        patientRepository.persist(male1);

        Patient male2 = createPatient("Bob", "Smith", "bob@email.com", "+420222");
        male2.gender = Patient.Gender.MALE;
        patientRepository.persist(male2);

        Patient female = createPatient("Jane", "Doe", "jane@email.com", "+420333");
        female.gender = Patient.Gender.FEMALE;
        patientRepository.persist(female);

        // When
        List<Patient> males = patientRepository.findByGender(Patient.Gender.MALE);
        List<Patient> females = patientRepository.findByGender(Patient.Gender.FEMALE);

        // Then
        assertEquals(2, males.size(), "Should find 2 male patients");
        assertEquals(1, females.size(), "Should find 1 female patient");

        System.out.println("✅ Found by gender:");
        System.out.println("   - Males: " + males.size());
        System.out.println("   - Females: " + females.size());
    }

    /**
     * Helper method to create a test patient.
     */
    private Patient createPatient(String firstName, String lastName, String email, String phone) {
        Patient patient = new Patient();
        patient.firstName = firstName;
        patient.lastName = lastName;
        patient.email = email;
        patient.phoneNumber = phone;
        patient.dateOfBirth = LocalDate.of(1990, 1, 1);
        patient.gender = Patient.Gender.OTHER;
        patient.isActive = true;
        return patient;
    }
}





