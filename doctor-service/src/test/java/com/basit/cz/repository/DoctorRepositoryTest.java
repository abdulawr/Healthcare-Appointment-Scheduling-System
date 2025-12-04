package com.basit.cz.repository;

import com.basit.cz.entity.Doctor;
import com.basit.cz.entity.DoctorAvailability;
import com.basit.cz.entity.DoctorReview;
import com.basit.cz.entity.DoctorSchedule;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *                  DOCTOR REPOSITORY TESTS - STEP 2
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Tests all custom queries in DoctorRepository
 *
 * CUSTOM QUERIES TESTED (16):
 * 1. findAllActive()
 * 2. findByEmail()
 * 3. findBySpecialization()
 * 4. searchByName()
 * 5. findByMinimumRating()
 * 6. findBySpecializationAndRating()
 * 7. findTopRated()
 * 8. findByMinimumExperience()
 * 9. findDoctorsWithReviews()
 * 10. findAvailableOnDay()
 * 11. countBySpecialization()
 * 12. getAllSpecializations()
 * 13. findByLicenseNumber()
 * 14. findDoctorsWithAvailability()
 * 15. findByConsultationFeeRange()
 * 16. getDoctorStatistics()
 */
@QuarkusTest
@DisplayName("Repository Tests - DoctorRepository")
public class DoctorRepositoryTest {

    @Inject
    DoctorRepository doctorRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        // Clean all data - DELETE CHILDREN FIRST to avoid FK constraint violations
        DoctorReview.deleteAll();
        DoctorAvailability.deleteAll();
        DoctorSchedule.deleteAll();
        Doctor.deleteAll();

        // Create test doctors
        createTestDoctors();
    }

    @Test
    @Transactional
    @DisplayName("TEST 1: Find all active doctors")
    public void testFindAllActive() {
        // Given: 4 active doctors, 1 inactive

        // When
        List<Doctor> activeDoctors = doctorRepository.findAllActive();

        // Then
        assertEquals(4, activeDoctors.size(), "Should find 4 active doctors");
        activeDoctors.forEach(doctor ->
                assertTrue(doctor.isActive, "All doctors should be active")
        );
    }

    @Test
    @Transactional
    @DisplayName("TEST 2: Find doctor by email")
    public void testFindByEmail() {
        // When
        Optional<Doctor> found = doctorRepository.findByEmail("john.cardio@hospital.com");

        // Then
        assertTrue(found.isPresent(), "Doctor should be found");
        assertEquals("John", found.get().firstName);
        assertEquals("Cardiology", found.get().specialization);
    }

    @Test
    @Transactional
    @DisplayName("TEST 3: Find by specialization")
    public void testFindBySpecialization() {
        // When
        List<Doctor> cardiologists = doctorRepository.findBySpecialization("Cardiology");

        // Then
        assertEquals(2, cardiologists.size(), "Should find 2 cardiologists");
        cardiologists.forEach(doctor ->
                assertTrue(doctor.hasSpecialization("Cardiology"))
        );
    }

    @Test
    @Transactional
    @DisplayName("TEST 4: Search by name")
    public void testSearchByName() {
        // Test first name search
        List<Doctor> johnDoctors = doctorRepository.searchByName("john");
        assertEquals(1, johnDoctors.size());
        assertEquals("John", johnDoctors.get(0).firstName);

        // Test last name search
        List<Doctor> smithDoctors = doctorRepository.searchByName("smith");
        assertEquals(2, smithDoctors.size());

        // Test partial match
        List<Doctor> partialMatch = doctorRepository.searchByName("jo");
        assertTrue(partialMatch.size() >= 1);
    }

    @Test
    @Transactional
    @DisplayName("TEST 5: Find by minimum rating")
    public void testFindByMinimumRating() {
        // When
        List<Doctor> topRated = doctorRepository.findByMinimumRating(4.5);

        // Then
        assertTrue(topRated.size() >= 1, "Should find at least 1 top-rated doctor");
        topRated.forEach(doctor ->
                assertTrue(doctor.averageRating >= 4.5,
                        "All doctors should have rating >= 4.5")
        );

        // Verify ordering (DESC)
        for (int i = 0; i < topRated.size() - 1; i++) {
            assertTrue(topRated.get(i).averageRating >= topRated.get(i + 1).averageRating,
                    "Should be ordered by rating DESC");
        }
    }

    @Test
    @Transactional
    @DisplayName("TEST 6: Find by specialization and rating")
    public void testFindBySpecializationAndRating() {
        // When
        List<Doctor> topCardiologists = doctorRepository
                .findBySpecializationAndRating("Cardiology", 4.0);

        // Then
        assertTrue(topCardiologists.size() >= 1);
        topCardiologists.forEach(doctor -> {
            assertTrue(doctor.hasSpecialization("Cardiology"));
            assertTrue(doctor.averageRating >= 4.0);
        });
    }

    @Test
    @Transactional
    @DisplayName("TEST 7: Find top-rated doctors (4.0+)")
    public void testFindTopRated() {
        // When
        List<Doctor> topRated = doctorRepository.findTopRated();

        // Then
        assertTrue(topRated.size() >= 1);
        topRated.forEach(doctor ->
                assertTrue(doctor.averageRating >= 4.0)
        );
    }

    @Test
    @Transactional
    @DisplayName("TEST 8: Find by minimum experience")
    public void testFindByMinimumExperience() {
        // When
        List<Doctor> experienced = doctorRepository.findByMinimumExperience(15);

        // Then
        assertTrue(experienced.size() >= 1);
        experienced.forEach(doctor ->
                assertTrue(doctor.yearsOfExperience >= 15)
        );

        // Verify ordering (DESC)
        for (int i = 0; i < experienced.size() - 1; i++) {
            assertTrue(experienced.get(i).yearsOfExperience >=
                    experienced.get(i + 1).yearsOfExperience);
        }
    }

    @Test
    @Transactional
    @DisplayName("TEST 9: Find doctors with reviews")
    public void testFindDoctorsWithReviews() {
        // When
        List<Doctor> withReviews = doctorRepository.findDoctorsWithReviews();

        // Then
        assertTrue(withReviews.size() >= 1);
        withReviews.forEach(doctor ->
                assertTrue(doctor.totalReviews > 0)
        );
    }

    @Test
    @Transactional
    @DisplayName("TEST 10: Find available on specific day")
    public void testFindAvailableOnDay() {
        // When
        List<Doctor> mondayDoctors = doctorRepository.findAvailableOnDay("MONDAY");

        // Then
        assertTrue(mondayDoctors.size() >= 1,
                "Should find doctors available on Monday");
    }

    @Test
    @Transactional
    @DisplayName("TEST 11: Count by specialization")
    public void testCountBySpecialization() {
        // When
        long cardiologyCount = doctorRepository.countBySpecialization("Cardiology");

        // Then
        assertEquals(2, cardiologyCount, "Should have 2 cardiologists");
    }

    @Test
    @Transactional
    @DisplayName("TEST 12: Get all specializations")
    public void testGetAllSpecializations() {
        // When
        List<String> specializations = doctorRepository.getAllSpecializations();

        // Then
        assertTrue(specializations.size() >= 3);
        assertTrue(specializations.contains("Cardiology"));
        assertTrue(specializations.contains("Neurology"));
        assertTrue(specializations.contains("Pediatrics"));
    }

    @Test
    @Transactional
    @DisplayName("TEST 13: Find by license number")
    public void testFindByLicenseNumber() {
        // When
        Optional<Doctor> found = doctorRepository.findByLicenseNumber("LIC-001");

        // Then
        assertTrue(found.isPresent());
        assertEquals("John", found.get().firstName);
    }

    @Test
    @Transactional
    @DisplayName("TEST 14: Find doctors with availability")
    public void testFindDoctorsWithAvailability() {
        // When
        List<Doctor> withAvailability = doctorRepository.findDoctorsWithAvailability();

        // Then
        assertTrue(withAvailability.size() >= 1);
    }

    @Test
    @Transactional
    @DisplayName("TEST 15: Find by consultation fee range")
    public void testFindByConsultationFeeRange() {
        // When
        List<Doctor> midRange = doctorRepository.findByConsultationFeeRange(80.0, 120.0);

        // Then
        assertTrue(midRange.size() >= 1);
        midRange.forEach(doctor -> {
            assertTrue(doctor.consultationFee >= 80.0);
            assertTrue(doctor.consultationFee <= 120.0);
        });

        // Verify ordering by fee ASC
        for (int i = 0; i < midRange.size() - 1; i++) {
            assertTrue(midRange.get(i).consultationFee <=
                    midRange.get(i + 1).consultationFee);
        }
    }

    @Test
    @Transactional
    @DisplayName("TEST 16: Get doctor statistics")
    public void testGetDoctorStatistics() {
        // When
        Object[] stats = doctorRepository.getDoctorStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(3, stats.length);

        Long totalDoctors = ((Number) stats[0]).longValue();
        Double avgRating = (Double) stats[1];
        Double avgExperience = (Double) stats[2];

        assertEquals(4L, totalDoctors, "Should have 4 active doctors");
        assertTrue(avgRating > 0.0, "Average rating should be positive");
        assertTrue(avgExperience > 0.0, "Average experience should be positive");
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER METHOD: Create Test Data
    // ═══════════════════════════════════════════════════════════
    private void createTestDoctors() {
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
        avail1.dayOfWeek = DayOfWeek.MONDAY;
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
        d4.lastName = "Smith";
        d4.email = "alice.smith@hospital.com";
        d4.phoneNumber = "+420444444444";
        d4.specialization = "Pediatrics";
        d4.yearsOfExperience = 8;
        d4.licenseNumber = "LIC-004";
        d4.consultationFee = 75.0;
        d4.averageRating = 3.9;
        d4.totalReviews = 25;
        d4.isActive = true;
        d4.persist();

        // Doctor 5: Inactive doctor (should not appear in most queries)
        Doctor d5 = new Doctor();
        d5.firstName = "Inactive";
        d5.lastName = "Doctor";
        d5.email = "inactive@hospital.com";
        d5.phoneNumber = "+420555555555";
        d5.specialization = "Surgery";
        d5.yearsOfExperience = 12;
        d5.licenseNumber = "LIC-005";
        d5.consultationFee = 90.0;
        d5.averageRating = 4.0;
        d5.totalReviews = 20;
        d5.isActive = false; // INACTIVE
        d5.persist();
    }
}








