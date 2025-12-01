package com.basit.cz.entity;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *                    DOCTOR SERVICE - ENTITY LAYER TESTS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * PURPOSE: Verify all 4 entities work correctly
 *
 * ENTITIES TESTED:
 * 1. Doctor - Main doctor entity
 * 2. DoctorAvailability - Doctor's working hours
 * 3. DoctorReview - Patient reviews and ratings
 * 4. DoctorSchedule - Time-off and vacations
 *
 * WHAT IS TESTED:
 * - Entity creation and persistence
 * - Relationships (One-to-Many, Many-to-One)
 * - Validation rules
 * - Business logic methods
 * - Lifecycle callbacks (@PrePersist, @PreUpdate)
 * - Cascade operations (delete)
 *
 * HOW TO RUN:
 * - All tests: mvn test
 * - This file only: mvn test -Dtest=DoctorEntityTest
 * - Single test: mvn test -Dtest=DoctorEntityTest#testCreateDoctor
 *
 * EXPECTED RESULT: All 10 tests pass ✅
 * ═══════════════════════════════════════════════════════════════════════════
 */
@QuarkusTest
@DisplayName("Entity Layer Tests - Doctor Service")
public class DoctorEntityTest {

    @Inject
    EntityManager entityManager;

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * SETUP: Clean database before each test
     * ═══════════════════════════════════════════════════════════════════════
     */
    @BeforeEach
    @Transactional
    public void cleanup() {
        // Delete in correct order (child entities first)
        entityManager.createQuery("DELETE FROM DoctorSchedule").executeUpdate();
        entityManager.createQuery("DELETE FROM DoctorReview").executeUpdate();
        entityManager.createQuery("DELETE FROM DoctorAvailability").executeUpdate();
        entityManager.createQuery("DELETE FROM Doctor").executeUpdate();
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * TEST 1: Create and Persist Doctor Entity
     * ═══════════════════════════════════════════════════════════════════════
     *
     * WHAT IT TESTS:
     * - Doctor entity can be created
     * - Doctor can be persisted to database
     * - Auto-generated ID is assigned
     * - Timestamps (createdAt, updatedAt) are set automatically
     * - Default values are correct (averageRating=0.0, totalReviews=0, isActive=true)
     * - getFullName() method works
     * - Entity can be retrieved from database
     *
     * STEPS:
     * 1. Create new Doctor object
     * 2. Set all required fields
     * 3. Persist to database
     * 4. Verify ID and timestamps
     * 5. Verify default values
     * 6. Test getFullName() method
     * 7. Retrieve from database and verify
     *
     * EXPECTED RESULT:
     * - Doctor saved with auto-generated ID
     * - createdAt and updatedAt are set
     * - averageRating = 0.0
     * - totalReviews = 0
     * - isActive = true
     * - getFullName() returns "John Smith"
     * - Can retrieve doctor from database
     */
    @Test
    @Transactional
    @DisplayName("TEST 1: Create and persist Doctor entity")
    public void testCreateDoctor() {
        // ════════════════════════════════════════════════════════════════
        // GIVEN: Create a new doctor with all required information
        // ════════════════════════════════════════════════════════════════
        Doctor doctor = new Doctor();
        doctor.firstName = "John";
        doctor.lastName = "Smith";
        doctor.email = "john.smith@hospital.com";
        doctor.phoneNumber = "+420111222333";
        doctor.specialization = "Cardiology";
        doctor.yearsOfExperience = 15;
        doctor.licenseNumber = "MED-12345";
        doctor.qualifications = "MD, FACC, Board Certified Cardiologist";
        doctor.bio = "Experienced cardiologist specializing in heart surgery";
        doctor.consultationFee = 100.0;

        // ════════════════════════════════════════════════════════════════
        // WHEN: Persist the doctor to database
        // ════════════════════════════════════════════════════════════════
        doctor.persist();
        entityManager.flush(); // Force database write
        entityManager.clear(); // Clear cache

        // ════════════════════════════════════════════════════════════════
        // THEN: Verify all fields are saved correctly
        // ════════════════════════════════════════════════════════════════

        // 1. Verify ID was auto-generated
        assertNotNull(doctor.id, "Doctor ID should be auto-generated");
        assertTrue(doctor.id > 0, "Doctor ID should be positive");

        // 2. Verify timestamps were set automatically
        assertNotNull(doctor.createdAt, "createdAt should be set automatically");
        assertNotNull(doctor.updatedAt, "updatedAt should be set automatically");

        // 3. Verify default values
        assertEquals(0.0, doctor.averageRating, "Default averageRating should be 0.0");
        assertEquals(0, doctor.totalReviews, "Default totalReviews should be 0");
        assertTrue(doctor.isActive, "Doctor should be active by default");

        // 4. Verify getFullName() method
        assertEquals("John Smith", doctor.getFullName(), "getFullName() should return 'John Smith'");

        // 5. Verify doctor can be retrieved from database
        Doctor found = Doctor.findById(doctor.id);
        assertNotNull(found, "Doctor should be retrievable from database");
        assertEquals("John", found.firstName, "First name should match");
        assertEquals("Smith", found.lastName, "Last name should match");
        assertEquals("john.smith@hospital.com", found.email, "Email should match");
        assertEquals("Cardiology", found.specialization, "Specialization should match");
        assertEquals(15, found.yearsOfExperience, "Years of experience should match");
        assertEquals(100.0, found.consultationFee, "Consultation fee should match");

        System.out.println("✅ TEST 1 PASSED: Doctor entity created and persisted successfully");
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * TEST 2: Doctor with Availability Slots
     * ═══════════════════════════════════════════════════════════════════════
     *
     * WHAT IT TESTS:
     * - One-to-Many relationship between Doctor and DoctorAvailability
     * - Multiple availability slots can be added
     * - Availability slot calculations work correctly
     * - isAvailableAt() method works
     * - getTotalSlots() calculates correctly
     * - getDurationInHours() calculates correctly
     * - Bidirectional relationship works
     *
     * STEPS:
     * 1. Create and persist doctor
     * 2. Create Monday availability (9:00-17:00, 30-min slots)
     * 3. Create Tuesday availability (10:00-16:00, 30-min slots)
     * 4. Persist both slots
     * 5. Verify slot calculations
     * 6. Verify time checking
     * 7. Verify relationship from both sides
     *
     * EXPECTED RESULT:
     * - Monday: 16 slots (8 hours * 2 slots/hour)
     * - Tuesday: 12 slots (6 hours * 2 slots/hour)
     * - Monday duration: 8.0 hours
     * - isAvailableAt(10:00) = true on Monday
     * - isAvailableAt(18:00) = false on Monday
     * - Doctor has 2 availability slots
     */
    @Test
    @Transactional
    @DisplayName("TEST 2: Doctor with availability slots and calculations")
    public void testDoctorWithAvailability() {
        // ════════════════════════════════════════════════════════════════
        // GIVEN: A doctor with availability slots
        // ════════════════════════════════════════════════════════════════
        Doctor doctor = createTestDoctor("Jane", "Doe", "jane.doe@hospital.com");
        doctor.persist();
        entityManager.flush();

        // ════════════════════════════════════════════════════════════════
        // WHEN: Add Monday availability (9:00 AM - 5:00 PM)
        // ════════════════════════════════════════════════════════════════
        DoctorAvailability monday = new DoctorAvailability();
        monday.doctor = doctor;
        monday.dayOfWeek = DayOfWeek.MONDAY;
        monday.startTime = LocalTime.of(9, 0);  // 9:00 AM
        monday.endTime = LocalTime.of(17, 0);   // 5:00 PM
        monday.slotDurationMinutes = 30;        // 30-minute slots
        monday.persist();

        // ════════════════════════════════════════════════════════════════
        // WHEN: Add Tuesday availability (10:00 AM - 4:00 PM)
        // ════════════════════════════════════════════════════════════════
        DoctorAvailability tuesday = new DoctorAvailability();
        tuesday.doctor = doctor;
        tuesday.dayOfWeek = DayOfWeek.TUESDAY;
        tuesday.startTime = LocalTime.of(10, 0); // 10:00 AM
        tuesday.endTime = LocalTime.of(16, 0);   // 4:00 PM
        tuesday.slotDurationMinutes = 30;        // 30-minute slots
        tuesday.persist();

        entityManager.flush();
        entityManager.clear();

        // ════════════════════════════════════════════════════════════════
        // THEN: Verify availability calculations
        // ════════════════════════════════════════════════════════════════

        // 1. Verify IDs were assigned
        assertNotNull(monday.id, "Monday slot should have ID");
        assertNotNull(tuesday.id, "Tuesday slot should have ID");

        // 2. Verify total slots calculation
        // Monday: 9:00-17:00 = 8 hours = 480 minutes / 30 = 16 slots
        assertEquals(16, monday.getTotalSlots(),
                "Monday should have 16 slots (8 hours * 2 slots/hour)");

        // Tuesday: 10:00-16:00 = 6 hours = 360 minutes / 30 = 12 slots
        assertEquals(12, tuesday.getTotalSlots(),
                "Tuesday should have 12 slots (6 hours * 2 slots/hour)");

        // 3. Verify duration in hours
        assertEquals(8.0, monday.getDurationInHours(),
                "Monday duration should be 8.0 hours");
        assertEquals(6.0, tuesday.getDurationInHours(),
                "Tuesday duration should be 6.0 hours");

        // 4. Verify time availability checking
        assertTrue(monday.isAvailableAt(LocalTime.of(10, 0)),
                "10:00 AM should be available on Monday");
        assertTrue(monday.isAvailableAt(LocalTime.of(16, 30)),
                "4:30 PM should be available on Monday");
        assertFalse(monday.isAvailableAt(LocalTime.of(18, 0)),
                "6:00 PM should NOT be available on Monday");
        assertFalse(monday.isAvailableAt(LocalTime.of(8, 0)),
                "8:00 AM should NOT be available on Monday");

        // 5. Verify bidirectional relationship
        // Use JPQL with JOIN FETCH to load availabilitySlots
        Doctor foundDoctor = entityManager.createQuery(
                        "SELECT d FROM Doctor d LEFT JOIN FETCH d.availabilitySlots WHERE d.id = :id", Doctor.class)
                .setParameter("id", doctor.id)
                .getSingleResult();

        assertNotNull(foundDoctor, "Doctor should be retrievable");
        assertEquals(2, foundDoctor.availabilitySlots.size(),
                "Doctor should have 2 availability slots");

        System.out.println("✅ TEST 2 PASSED: Availability slots created with correct calculations");
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * TEST 3: Doctor with Reviews and Rating Update
     * ═══════════════════════════════════════════════════════════════════════
     *
     * WHAT IT TESTS:
     * - One-to-Many relationship between Doctor and DoctorReview
     * - Multiple reviews can be added
     * - updateRating() calculates average correctly
     * - isPositive(), isNegative(), isNeutral() methods work
     * - getRatingCategory() returns correct category
     * - totalReviews counter is updated
     *
     * STEPS:
     * 1. Create and persist doctor
     * 2. Add review with 5 stars (excellent)
     * 3. Add review with 4 stars (very good)
     * 4. Add review with 3 stars (okay)
     * 5. Call updateRating() to calculate average
     * 6. Verify average rating = 4.0
     * 7. Verify totalReviews = 3
     * 8. Verify rating categorization methods
     *
     * EXPECTED RESULT:
     * - Average rating = 4.0 (from 5, 4, 3)
     * - Total reviews = 3
     * - 5-star is positive
     * - 4-star is positive
     * - 3-star is neutral
     */
    @Test
    @Transactional
    @DisplayName("TEST 3: Doctor with reviews and rating calculation")
    public void testDoctorWithReviewsAndRating() {
        // ════════════════════════════════════════════════════════════════
        // GIVEN: A doctor
        // ════════════════════════════════════════════════════════════════
        Doctor doctor = createTestDoctor("Bob", "Johnson", "bob.johnson@hospital.com");
        doctor.persist();
        entityManager.flush();

        // ════════════════════════════════════════════════════════════════
        // WHEN: Add multiple reviews (5 stars, 4 stars, 3 stars)
        // ════════════════════════════════════════════════════════════════

        // Review 1: 5 stars (Excellent)
        DoctorReview review1 = new DoctorReview();
        review1.doctor = doctor;
        review1.patientId = 1L;
        review1.patientName = "Patient One";
        review1.rating = 5;
        review1.comment = "Excellent doctor! Very professional and caring.";
        review1.appointmentDate = LocalDateTime.now().minusDays(10);
        review1.persist();

        // Review 2: 4 stars (Very Good)
        DoctorReview review2 = new DoctorReview();
        review2.doctor = doctor;
        review2.patientId = 2L;
        review2.patientName = "Patient Two";
        review2.rating = 4;
        review2.comment = "Very good doctor, would recommend.";
        review2.appointmentDate = LocalDateTime.now().minusDays(5);
        review2.persist();

        // Review 3: 3 stars (Okay)
        DoctorReview review3 = new DoctorReview();
        review3.doctor = doctor;
        review3.patientId = 3L;
        review3.patientName = "Patient Three";
        review3.rating = 3;
        review3.comment = "Average experience, nothing special.";
        review3.appointmentDate = LocalDateTime.now().minusDays(2);
        review3.persist();

        // Add reviews to doctor's list
        doctor.reviews.add(review1);
        doctor.reviews.add(review2);
        doctor.reviews.add(review3);

        // ════════════════════════════════════════════════════════════════
        // WHEN: Update doctor's average rating
        // ════════════════════════════════════════════════════════════════
        doctor.updateRating();
        entityManager.flush();
        entityManager.clear();

        // ════════════════════════════════════════════════════════════════
        // THEN: Verify rating calculation and categorization
        // ════════════════════════════════════════════════════════════════

        // 1. Verify total reviews count
        assertEquals(3, doctor.totalReviews,
                "Doctor should have 3 total reviews");

        // 2. Verify average rating calculation: (5 + 4 + 3) / 3 = 4.0
        assertEquals(4.0, doctor.averageRating, 0.1,
                "Average rating should be 4.0");

        // 3. Verify review categorization
        assertTrue(review1.isPositive(),
                "5-star review should be positive");
        assertTrue(review2.isPositive(),
                "4-star review should be positive");
        assertFalse(review1.isNegative(),
                "5-star review should NOT be negative");
        assertTrue(review3.isNeutral(),
                "3-star review should be neutral");

        // 4. Verify rating category strings
        assertEquals("POSITIVE", review1.getRatingCategory(),
                "5-star should return 'POSITIVE'");
        assertEquals("POSITIVE", review2.getRatingCategory(),
                "4-star should return 'POSITIVE'");
        assertEquals("NEUTRAL", review3.getRatingCategory(),
                "3-star should return 'NEUTRAL'");

        // 5. Verify relationship from database
        // Use JPQL with JOIN FETCH to load reviews
        Doctor foundDoctor = entityManager.createQuery(
                        "SELECT d FROM Doctor d LEFT JOIN FETCH d.reviews WHERE d.id = :id", Doctor.class)
                .setParameter("id", doctor.id)
                .getSingleResult();

        assertNotNull(foundDoctor, "Doctor should be retrievable");
        assertEquals(3, foundDoctor.reviews.size(),
                "Doctor should have 3 reviews");
        assertEquals(4.0, foundDoctor.averageRating, 0.1,
                "Average rating should persist in database");

        System.out.println("✅ TEST 3 PASSED: Reviews created and rating calculated correctly");
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * TEST 4: Doctor with Schedule/Time-off
     * ═══════════════════════════════════════════════════════════════════════
     *
     * WHAT IT TESTS:
     * - One-to-Many relationship between Doctor and DoctorSchedule
     * - Schedule creation and persistence
     * - Approval workflow (PENDING → APPROVED)
     * - getDurationInDays() calculation
     * - coversDate() date checking
     * - Status checking methods (isPending, isApproved, isActive)
     *
     * STEPS:
     * 1. Create and persist doctor
     * 2. Create vacation schedule (10 days from now, 11 days duration)
     * 3. Verify initial status is PENDING
     * 4. Approve the schedule
     * 5. Verify approval details
     * 6. Test date coverage
     * 7. Verify relationship
     *
     * EXPECTED RESULT:
     * - Schedule created with PENDING status
     * - Duration = 11 days
     * - After approval: status = APPROVED
     * - Covers dates in range
     * - Doesn't cover dates outside range
     */
    @Test
    @Transactional
    @DisplayName("TEST 4: Doctor with schedule and approval workflow")
    public void testDoctorWithSchedule() {
        // ════════════════════════════════════════════════════════════════
        // GIVEN: A doctor
        // ════════════════════════════════════════════════════════════════
        Doctor doctor = createTestDoctor("Alice", "Williams", "alice.williams@hospital.com");
        doctor.persist();
        entityManager.flush();

        // ════════════════════════════════════════════════════════════════
        // WHEN: Create a vacation schedule
        // ════════════════════════════════════════════════════════════════
        DoctorSchedule vacation = new DoctorSchedule();
        vacation.doctor = doctor;
        vacation.scheduleType = DoctorSchedule.ScheduleType.VACATION;
        vacation.startDate = LocalDate.now().plusDays(10);  // Starts in 10 days
        vacation.endDate = LocalDate.now().plusDays(20);    // Ends in 20 days (11 days total)
        vacation.reason = "Summer vacation to Spain";
        vacation.status = DoctorSchedule.ScheduleStatus.PENDING;
        vacation.notes = "Will be back on the 21st";
        vacation.persist();

        // Add to doctor's collection
        doctor.schedules.add(vacation);

        entityManager.flush();
        entityManager.clear();

        // ════════════════════════════════════════════════════════════════
        // THEN: Verify schedule details
        // ════════════════════════════════════════════════════════════════

        // 1. Verify ID was assigned
        assertNotNull(vacation.id, "Schedule should have ID");

        // 2. Verify duration calculation (20 - 10 + 1 = 11 days)
        assertEquals(11, vacation.getDurationInDays(),
                "Schedule should be 11 days long");

        // 3. Verify initial status is PENDING
        assertTrue(vacation.isPending(),
                "New schedule should be PENDING");
        assertFalse(vacation.isApproved(),
                "New schedule should NOT be APPROVED yet");
        assertFalse(vacation.isActive(),
                "New schedule should NOT be active (not approved yet)");

        // ════════════════════════════════════════════════════════════════
        // WHEN: Approve the schedule
        // ════════════════════════════════════════════════════════════════
        vacation.approve("admin@hospital.com");
        entityManager.flush();

        // ════════════════════════════════════════════════════════════════
        // THEN: Verify approval
        // ════════════════════════════════════════════════════════════════

        // 1. Verify status changed to APPROVED
        assertTrue(vacation.isApproved(),
                "Schedule should be APPROVED");
        assertFalse(vacation.isPending(),
                "Schedule should NOT be PENDING anymore");

        // 2. Verify approval details
        assertNotNull(vacation.approvalDate,
                "Approval date should be set");
        assertEquals("admin@hospital.com", vacation.approvedBy,
                "Approver should be recorded");

        // 3. Verify date coverage
        LocalDate middleDate = LocalDate.now().plusDays(15); // Middle of vacation
        LocalDate beforeDate = LocalDate.now().plusDays(5);  // Before vacation
        LocalDate afterDate = LocalDate.now().plusDays(25);  // After vacation

        assertTrue(vacation.coversDate(middleDate),
                "Schedule should cover dates in the middle");
        assertFalse(vacation.coversDate(beforeDate),
                "Schedule should NOT cover dates before start");
        assertFalse(vacation.coversDate(afterDate),
                "Schedule should NOT cover dates after end");

        // 4. Verify relationship
        // Use JPQL with JOIN FETCH to load schedules
        Doctor foundDoctor = entityManager.createQuery(
                        "SELECT d FROM Doctor d LEFT JOIN FETCH d.schedules WHERE d.id = :id", Doctor.class)
                .setParameter("id", doctor.id)
                .getSingleResult();

        assertNotNull(foundDoctor, "Doctor should be retrievable");
        assertEquals(1, foundDoctor.schedules.size(),
                "Doctor should have 1 schedule");

        System.out.println("✅ TEST 4 PASSED: Schedule created and approval workflow works");
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * TEST 5: Doctor Deactivation and Reactivation
     * ═══════════════════════════════════════════════════════════════════════
     *
     * WHAT IT TESTS:
     * - deactivate() method sets isActive to false
     * - activate() method sets isActive to true
     * - Status persists to database
     * - Can toggle status multiple times
     *
     * STEPS:
     * 1. Create active doctor (default isActive = true)
     * 2. Deactivate doctor
     * 3. Verify isActive = false
     * 4. Verify in database
     * 5. Reactivate doctor
     * 6. Verify isActive = true
     *
     * EXPECTED RESULT:
     * - New doctor is active by default
     * - deactivate() sets isActive to false
     * - activate() sets isActive to true
     * - Status persists correctly
     */
    @Test
    @Transactional
    @DisplayName("TEST 5: Doctor deactivation and reactivation")
    public void testDoctorDeactivation() {
        // ════════════════════════════════════════════════════════════════
        // GIVEN: An active doctor
        // ════════════════════════════════════════════════════════════════
        Doctor doctor = createTestDoctor("Charlie", "Brown", "charlie.brown@hospital.com");
        doctor.persist();
        entityManager.flush();

        // Verify doctor is active by default
        assertTrue(doctor.isActive,
                "New doctor should be active by default");

        // ════════════════════════════════════════════════════════════════
        // WHEN: Deactivate the doctor
        // ════════════════════════════════════════════════════════════════
        doctor.deactivate();
        entityManager.flush();
        entityManager.clear();

        // ════════════════════════════════════════════════════════════════
        // THEN: Verify doctor is deactivated
        // ════════════════════════════════════════════════════════════════
        assertFalse(doctor.isActive,
                "Doctor should be deactivated");

        // Verify in database
        Doctor foundDeactivated = Doctor.findById(doctor.id);
        assertNotNull(foundDeactivated, "Doctor should still exist in database");
        assertFalse(foundDeactivated.isActive,
                "Doctor should be deactivated in database");

        // ════════════════════════════════════════════════════════════════
        // WHEN: Reactivate the doctor
        // ════════════════════════════════════════════════════════════════
        foundDeactivated.activate();
        entityManager.flush();
        entityManager.clear();

        // ════════════════════════════════════════════════════════════════
        // THEN: Verify doctor is reactivated
        // ════════════════════════════════════════════════════════════════
        Doctor foundReactivated = Doctor.findById(doctor.id);
        assertNotNull(foundReactivated, "Doctor should still exist");
        assertTrue(foundReactivated.isActive,
                "Doctor should be reactivated in database");

        System.out.println("✅ TEST 5 PASSED: Deactivation and reactivation work correctly");
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * TEST 6: Specialization Check
     * ═══════════════════════════════════════════════════════════════════════
     *
     * WHAT IT TESTS:
     * - hasSpecialization() method works correctly
     * - Case-insensitive matching
     * - Returns false for non-matching specializations
     *
     * STEPS:
     * 1. Create doctor with "Neurology" specialization
     * 2. Test exact match
     * 3. Test case-insensitive match
     * 4. Test non-matching specialization
     *
     * EXPECTED RESULT:
     * - hasSpecialization("Neurology") = true
     * - hasSpecialization("neurology") = true (case-insensitive)
     * - hasSpecialization("Cardiology") = false
     */
    @Test
    @Transactional
    @DisplayName("TEST 6: Specialization checking (case-insensitive)")
    public void testSpecializationCheck() {
        // ════════════════════════════════════════════════════════════════
        // GIVEN: Doctor with Neurology specialization
        // ════════════════════════════════════════════════════════════════
        Doctor doctor = createTestDoctor("David", "Miller", "david.miller@hospital.com");
        doctor.specialization = "Neurology";
        doctor.persist();
        entityManager.flush();

        // ════════════════════════════════════════════════════════════════
        // THEN: Verify specialization checking
        // ════════════════════════════════════════════════════════════════

        // 1. Exact match
        assertTrue(doctor.hasSpecialization("Neurology"),
                "Should match exact specialization");

        // 2. Case-insensitive match
        assertTrue(doctor.hasSpecialization("neurology"),
                "Should match lowercase (case-insensitive)");
        assertTrue(doctor.hasSpecialization("NEUROLOGY"),
                "Should match uppercase (case-insensitive)");
        assertTrue(doctor.hasSpecialization("NeUrOlOgY"),
                "Should match mixed case (case-insensitive)");

        // 3. Non-matching specialization
        assertFalse(doctor.hasSpecialization("Cardiology"),
                "Should NOT match different specialization");
        assertFalse(doctor.hasSpecialization("Pediatrics"),
                "Should NOT match different specialization");

        System.out.println("✅ TEST 6 PASSED: Specialization checking works correctly");
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * TEST 7: Availability Slot Time Validation
     * ═══════════════════════════════════════════════════════════════════════
     *
     * WHAT IT TESTS:
     * - Validation that endTime must be after startTime
     * - Throws IllegalArgumentException for invalid times
     * - Validation happens in @PrePersist
     *
     * STEPS:
     * 1. Create doctor
     * 2. Create availability with end time BEFORE start time
     * 3. Try to persist
     * 4. Verify exception is thrown
     *
     * EXPECTED RESULT:
     * - IllegalArgumentException thrown
     * - Message: "End time must be after start time"
     */
    @Test
    @Transactional
    @DisplayName("TEST 7: Availability time validation (end > start)")
    public void testAvailabilityValidation() {
        // ════════════════════════════════════════════════════════════════
        // GIVEN: Doctor and invalid availability slot
        // ════════════════════════════════════════════════════════════════
        Doctor doctor = createTestDoctor("Emma", "Davis", "emma.davis@hospital.com");
        doctor.persist();
        entityManager.flush();

        DoctorAvailability invalidSlot = new DoctorAvailability();
        invalidSlot.doctor = doctor;
        invalidSlot.dayOfWeek = DayOfWeek.WEDNESDAY;
        invalidSlot.startTime = LocalTime.of(17, 0); // 5:00 PM
        invalidSlot.endTime = LocalTime.of(9, 0);    // 9:00 AM (BEFORE start!)
        invalidSlot.slotDurationMinutes = 30;

        // ════════════════════════════════════════════════════════════════
        // WHEN/THEN: Try to persist invalid slot
        // ════════════════════════════════════════════════════════════════
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invalidSlot.persist(),
                "Should throw IllegalArgumentException for invalid times"
        );

        // Verify error message
        assertEquals("End time must be after start time", exception.getMessage(),
                "Error message should explain the validation error");

        System.out.println("✅ TEST 7 PASSED: Availability time validation works");
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * TEST 8: Schedule Date Validation
     * ═══════════════════════════════════════════════════════════════════════
     *
     * WHAT IT TESTS:
     * - Validation that endDate must be >= startDate
     * - Throws IllegalArgumentException for invalid dates
     * - Validation happens in @PrePersist
     *
     * STEPS:
     * 1. Create doctor
     * 2. Create schedule with end date BEFORE start date
     * 3. Try to persist
     * 4. Verify exception is thrown
     *
     * EXPECTED RESULT:
     * - IllegalArgumentException thrown
     * - Message: "End date cannot be before start date"
     */
    @Test
    @Transactional
    @DisplayName("TEST 8: Schedule date validation (end >= start)")
    public void testScheduleValidation() {
        // ════════════════════════════════════════════════════════════════
        // GIVEN: Doctor and invalid schedule
        // ════════════════════════════════════════════════════════════════
        Doctor doctor = createTestDoctor("Frank", "Wilson", "frank.wilson@hospital.com");
        doctor.persist();
        entityManager.flush();

        DoctorSchedule invalidSchedule = new DoctorSchedule();
        invalidSchedule.doctor = doctor;
        invalidSchedule.scheduleType = DoctorSchedule.ScheduleType.SICK_LEAVE;
        invalidSchedule.startDate = LocalDate.now().plusDays(10); // Starts day 10
        invalidSchedule.endDate = LocalDate.now().plusDays(5);    // Ends day 5 (BEFORE start!)
        invalidSchedule.status = DoctorSchedule.ScheduleStatus.PENDING;
        invalidSchedule.reason = "Medical leave";

        // ════════════════════════════════════════════════════════════════
        // WHEN/THEN: Try to persist invalid schedule
        // ════════════════════════════════════════════════════════════════
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invalidSchedule.persist(),
                "Should throw IllegalArgumentException for invalid dates"
        );

        // Verify error message
        assertEquals("End date cannot be before start date", exception.getMessage(),
                "Error message should explain the validation error");

        System.out.println("✅ TEST 8 PASSED: Schedule date validation works");
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * TEST 9: Review Rating Categories
     * ═══════════════════════════════════════════════════════════════════════
     *
     * WHAT IT TESTS:
     * - All rating values (1-5) can be saved
     * - Review categorization methods work for all ratings
     * - Verification system works
     *
     * STEPS:
     * 1. Create doctor
     * 2. Add reviews with ratings 1, 2, 3, 4, 5
     * 3. Verify each review categorization
     * 4. Test verification
     *
     * EXPECTED RESULT:
     * - Rating 1-2: NEGATIVE
     * - Rating 3: NEUTRAL
     * - Rating 4-5: POSITIVE
     */
    @Test
    @Transactional
    @DisplayName("TEST 9: Review rating categories (1-5 stars)")
    public void testReviewRatingCategories() {
        // ════════════════════════════════════════════════════════════════
        // GIVEN: Doctor
        // ════════════════════════════════════════════════════════════════
        Doctor doctor = createTestDoctor("Grace", "Taylor", "grace.taylor@hospital.com");
        doctor.persist();
        entityManager.flush();

        // ════════════════════════════════════════════════════════════════
        // WHEN: Create reviews with all rating values (1-5)
        // ════════════════════════════════════════════════════════════════
        DoctorReview[] reviews = new DoctorReview[5];

        for (int rating = 1; rating <= 5; rating++) {
            DoctorReview review = new DoctorReview();
            review.doctor = doctor;
            review.patientId = (long) rating;
            review.patientName = "Patient " + rating;
            review.rating = rating;
            review.comment = "Review with " + rating + " stars";
            review.persist();
            reviews[rating - 1] = review;
        }

        entityManager.flush();

        // ════════════════════════════════════════════════════════════════
        // THEN: Verify categorization for each rating
        // ════════════════════════════════════════════════════════════════

        // 1 star: NEGATIVE
        assertTrue(reviews[0].isNegative(), "1 star should be negative");
        assertFalse(reviews[0].isPositive(), "1 star should NOT be positive");
        assertEquals("NEGATIVE", reviews[0].getRatingCategory());

        // 2 stars: NEGATIVE
        assertTrue(reviews[1].isNegative(), "2 stars should be negative");
        assertEquals("NEGATIVE", reviews[1].getRatingCategory());

        // 3 stars: NEUTRAL
        assertTrue(reviews[2].isNeutral(), "3 stars should be neutral");
        assertFalse(reviews[2].isNegative(), "3 stars should NOT be negative");
        assertFalse(reviews[2].isPositive(), "3 stars should NOT be positive");
        assertEquals("NEUTRAL", reviews[2].getRatingCategory());

        // 4 stars: POSITIVE
        assertTrue(reviews[3].isPositive(), "4 stars should be positive");
        assertFalse(reviews[3].isNegative(), "4 stars should NOT be negative");
        assertEquals("POSITIVE", reviews[3].getRatingCategory());

        // 5 stars: POSITIVE
        assertTrue(reviews[4].isPositive(), "5 stars should be positive");
        assertEquals("POSITIVE", reviews[4].getRatingCategory());

        // ════════════════════════════════════════════════════════════════
        // Test verification
        // ════════════════════════════════════════════════════════════════
        assertFalse(reviews[0].isVerified, "Review should not be verified by default");
        reviews[0].verify();
        assertTrue(reviews[0].isVerified, "Review should be verified after verify()");

        System.out.println("✅ TEST 9 PASSED: All rating categories work correctly");
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * TEST 10: Cascade Delete Operations
     * ═══════════════════════════════════════════════════════════════════════
     *
     * WHAT IT TESTS:
     * - Deleting doctor cascades to all related entities
     * - All availability slots are deleted
     * - All reviews are deleted
     * - All schedules are deleted
     * - Orphan removal works correctly
     *
     * STEPS:
     * 1. Create doctor
     * 2. Add availability slot
     * 3. Add review
     * 4. Add schedule
     * 5. Delete doctor
     * 6. Verify all related entities are deleted
     *
     * EXPECTED RESULT:
     * - Doctor deleted
     * - Availability deleted
     * - Review deleted
     * - Schedule deleted
     */
    @Test
    @Transactional
    @DisplayName("TEST 10: CASCADE DELETE removes all related entities")
    public void testCascadeDelete() {
        // ════════════════════════════════════════════════════════════════
        // GIVEN: Doctor with all relationships
        // ════════════════════════════════════════════════════════════════
        Doctor doctor = createTestDoctor("Henry", "Anderson", "henry.anderson@hospital.com");
        doctor.persist();

        // Add availability slot
        DoctorAvailability availability = new DoctorAvailability();
        availability.doctor = doctor;
        availability.dayOfWeek = DayOfWeek.FRIDAY;
        availability.startTime = LocalTime.of(9, 0);
        availability.endTime = LocalTime.of(17, 0);
        availability.persist();

        // Add review
        DoctorReview review = new DoctorReview();
        review.doctor = doctor;
        review.patientId = 1L;
        review.rating = 5;
        review.comment = "Great doctor!";
        review.persist();

        // Add schedule
        DoctorSchedule schedule = new DoctorSchedule();
        schedule.doctor = doctor;
        schedule.scheduleType = DoctorSchedule.ScheduleType.CONFERENCE;
        schedule.startDate = LocalDate.now();
        schedule.endDate = LocalDate.now().plusDays(3);
        schedule.status = DoctorSchedule.ScheduleStatus.APPROVED;
        schedule.persist();

        entityManager.flush();
        entityManager.clear();

        // Save IDs for verification
        Long doctorId = doctor.id;
        Long availabilityId = availability.id;
        Long reviewId = review.id;
        Long scheduleId = schedule.id;

        // Verify all entities exist before delete
        assertNotNull(Doctor.findById(doctorId), "Doctor should exist");
        assertNotNull(DoctorAvailability.findById(availabilityId), "Availability should exist");
        assertNotNull(DoctorReview.findById(reviewId), "Review should exist");
        assertNotNull(DoctorSchedule.findById(scheduleId), "Schedule should exist");

        // ════════════════════════════════════════════════════════════════
        // WHEN: Delete the doctor
        // ════════════════════════════════════════════════════════════════
        Doctor.deleteById(doctorId);
        entityManager.flush();
        entityManager.clear();

        // ════════════════════════════════════════════════════════════════
        // THEN: Verify all related entities are deleted (CASCADE)
        // ════════════════════════════════════════════════════════════════
        assertNull(Doctor.findById(doctorId),
                "Doctor should be deleted");
        assertNull(DoctorAvailability.findById(availabilityId),
                "Availability should be deleted (CASCADE)");
        assertNull(DoctorReview.findById(reviewId),
                "Review should be deleted (CASCADE)");
        assertNull(DoctorSchedule.findById(scheduleId),
                "Schedule should be deleted (CASCADE)");

        System.out.println("✅ TEST 10 PASSED: CASCADE DELETE works correctly");
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * HELPER METHOD: Create Test Doctor
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Creates a doctor with default values for testing.
     * Saves typing the same fields in every test.
     */
    private Doctor createTestDoctor(String firstName, String lastName, String email) {
        Doctor doctor = new Doctor();
        doctor.firstName = firstName;
        doctor.lastName = lastName;
        doctor.email = email;
        doctor.phoneNumber = "+420999888777";
        doctor.specialization = "General Practice";
        doctor.yearsOfExperience = 10;
        doctor.consultationFee = 80.0;
        return doctor;
    }
}




