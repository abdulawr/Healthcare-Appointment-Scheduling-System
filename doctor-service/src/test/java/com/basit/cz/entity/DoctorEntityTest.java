package com.basit.cz.entity;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Doctor entity
 */
@QuarkusTest
public class DoctorEntityTest {

    @Test
    public void testDoctorCreation() {
        Doctor doctor = new Doctor();
        doctor.firstName = "John";
        doctor.lastName = "Smith";
        doctor.email = "john.smith@hospital.com";
        doctor.specialization = "Cardiology";
        doctor.yearsOfExperience = 15;

        assertNotNull(doctor);
        assertEquals("John", doctor.firstName);
        assertEquals("Smith", doctor.lastName);
        assertEquals("john.smith@hospital.com", doctor.email);
        assertEquals("Cardiology", doctor.specialization);
        assertEquals(15, doctor.yearsOfExperience);
    }

    @Test
    public void testGetFullName() {
        Doctor doctor = new Doctor();
        doctor.firstName = "John";
        doctor.lastName = "Smith";

        assertEquals("John Smith", doctor.getFullName());
    }

    @Test
    public void testGetFullNameWithNullValues() {
        Doctor doctor = new Doctor();

        // Both null
        assertNull(doctor.getFullName());

        // Only firstName
        doctor.firstName = "John";
        assertEquals("John", doctor.getFullName());

        // Only lastName
        doctor.firstName = null;
        doctor.lastName = "Smith";
        assertEquals("Smith", doctor.getFullName());

        // Both set
        doctor.firstName = "John";
        assertEquals("John Smith", doctor.getFullName());
    }

    @Test
    public void testDoctorWithDefaultValues() {
        Doctor doctor = new Doctor();
        doctor.prePersist(); // Trigger defaults

        assertEquals(0.0, doctor.averageRating);
        assertEquals(0, doctor.totalReviews);
        assertTrue(doctor.isActive);
    }

    @Test
    public void testDoctorRatingAndReviews() {
        Doctor doctor = new Doctor();
        doctor.firstName = "Jane";
        doctor.lastName = "Doe";
        doctor.averageRating = 4.5;
        doctor.totalReviews = 120;

        assertEquals(4.5, doctor.averageRating, 0.01);
        assertEquals(120, doctor.totalReviews);
    }

    @Test
    public void testDoctorActiveStatus() {
        Doctor doctor = new Doctor();
        doctor.firstName = "Active";
        doctor.lastName = "Doctor";
        doctor.isActive = true;

        assertTrue(doctor.isActive);

        doctor.isActive = false;
        assertFalse(doctor.isActive);
    }

    @Test
    public void testDoctorTimestamps() {
        Doctor doctor = new Doctor();
        LocalDateTime now = LocalDateTime.now();

        doctor.createdAt = now;
        doctor.updatedAt = now;

        assertEquals(now, doctor.createdAt);
        assertEquals(now, doctor.updatedAt);
    }

    @Test
    public void testDoctorConsultationFee() {
        Doctor doctor = new Doctor();
        doctor.firstName = "Fee";
        doctor.lastName = "Doctor";
        doctor.consultationFee = 150.00;

        assertEquals(150.00, doctor.consultationFee, 0.01);
    }

    @Test
    public void testDoctorWithAllFields() {
        Doctor doctor = new Doctor();
        doctor.firstName = "Complete";
        doctor.lastName = "Doctor";
        doctor.email = "complete@hospital.com";
        doctor.phoneNumber = "+1234567890";
        doctor.specialization = "Neurology";
        doctor.yearsOfExperience = 20;
        doctor.licenseNumber = "MED-12345";
        doctor.consultationFee = 200.00;
        doctor.bio = "Experienced neurologist";
        doctor.qualifications = "MD, PhD, FAAN";
        doctor.averageRating = 4.8;
        doctor.totalReviews = 250;
        doctor.isActive = true;
        doctor.createdAt = LocalDateTime.now();
        doctor.updatedAt = LocalDateTime.now();

        assertNotNull(doctor);
        assertEquals("Complete Doctor", doctor.getFullName());
        assertEquals("complete@hospital.com", doctor.email);
        assertEquals("+1234567890", doctor.phoneNumber);
        assertEquals("Neurology", doctor.specialization);
        assertEquals(20, doctor.yearsOfExperience);
        assertEquals("MED-12345", doctor.licenseNumber);
        assertEquals(200.00, doctor.consultationFee, 0.01);
        assertEquals("Experienced neurologist", doctor.bio);
        assertEquals("MD, PhD, FAAN", doctor.qualifications);
        assertEquals(4.8, doctor.averageRating, 0.01);
        assertEquals(250, doctor.totalReviews);
        assertTrue(doctor.isActive);
        assertNotNull(doctor.createdAt);
        assertNotNull(doctor.updatedAt);
    }

    // ===============================================
    // DOCTOR AVAILABILITY TESTS
    // ===============================================

    @Test
    public void testDoctorAvailabilityCreation() {
        Doctor doctor = new Doctor();
        doctor.firstName = "Available";
        doctor.lastName = "Doctor";

        DoctorAvailability availability = new DoctorAvailability();
        availability.doctor = doctor;
        availability.dayOfWeek = "MONDAY";
        availability.startTime = LocalTime.of(9, 0);
        availability.endTime = LocalTime.of(17, 0);
        availability.isActive = true;

        assertNotNull(availability);
        assertEquals(doctor, availability.doctor);
        assertEquals("MONDAY", availability.dayOfWeek);
        assertEquals(LocalTime.of(9, 0), availability.startTime);
        assertEquals(LocalTime.of(17, 0), availability.endTime);
        assertTrue(availability.isActive);
    }

    @Test
    public void testMultipleAvailabilitySlots() {
        Doctor doctor = new Doctor();
        doctor.firstName = "Multi";
        doctor.lastName = "Slot";

        // Monday slot
        DoctorAvailability monday = new DoctorAvailability();
        monday.doctor = doctor;
        monday.dayOfWeek = "MONDAY";
        monday.startTime = LocalTime.of(9, 0);
        monday.endTime = LocalTime.of(12, 0);
        monday.isActive = true;

        // Tuesday slot
        DoctorAvailability tuesday = new DoctorAvailability();
        tuesday.doctor = doctor;
        tuesday.dayOfWeek = "TUESDAY";
        tuesday.startTime = LocalTime.of(14, 0);
        tuesday.endTime = LocalTime.of(18, 0);
        tuesday.isActive = true;

        assertNotNull(monday);
        assertNotNull(tuesday);
        assertEquals("MONDAY", monday.dayOfWeek);
        assertEquals("TUESDAY", tuesday.dayOfWeek);
        assertEquals(doctor, monday.doctor);
        assertEquals(doctor, tuesday.doctor);
    }

    @Test
    public void testAvailabilityTimeValidation() {
        DoctorAvailability availability = new DoctorAvailability();
        availability.dayOfWeek = "WEDNESDAY";
        availability.startTime = LocalTime.of(9, 0);
        availability.endTime = LocalTime.of(17, 0);

        // Verify start time is before end time
        assertTrue(availability.startTime.isBefore(availability.endTime));

        // Calculate duration
        long durationHours = java.time.Duration.between(
                availability.startTime,
                availability.endTime
        ).toHours();
        assertEquals(8, durationHours);
    }

    @Test
    public void testAvailabilityActiveStatus() {
        DoctorAvailability availability = new DoctorAvailability();
        availability.dayOfWeek = "THURSDAY";
        availability.startTime = LocalTime.of(10, 0);
        availability.endTime = LocalTime.of(16, 0);
        availability.isActive = true;

        assertTrue(availability.isActive);

        // Deactivate
        availability.isActive = false;
        assertFalse(availability.isActive);
    }

    @Test
    public void testAvailabilityCreatedAt() {
        DoctorAvailability availability = new DoctorAvailability();
        availability.dayOfWeek = "FRIDAY";
        availability.startTime = LocalTime.of(9, 0);
        availability.endTime = LocalTime.of(13, 0);
        availability.createdAt = LocalDateTime.now();

        assertNotNull(availability.createdAt);
    }

    @Test
    public void testInvalidTimeSlot() {
        DoctorAvailability invalidSlot = new DoctorAvailability();
        invalidSlot.dayOfWeek = "MONDAY";
        invalidSlot.startTime = LocalTime.of(17, 0);
        invalidSlot.endTime = LocalTime.of(9, 0);

        // Start time is after end time (invalid)
        assertTrue(invalidSlot.startTime.isAfter(invalidSlot.endTime));
    }

    @Test
    public void testDayOfWeekValidation() {
        DoctorAvailability availability = new DoctorAvailability();

        // Valid days
        String[] validDays = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};

        for (String day : validDays) {
            availability.dayOfWeek = day;
            assertNotNull(availability.dayOfWeek);
            assertEquals(day, availability.dayOfWeek);
        }
    }

    @Test
    public void testAvailabilityDurationCalculation() {
        DoctorAvailability slot1 = new DoctorAvailability();
        slot1.startTime = LocalTime.of(9, 0);
        slot1.endTime = LocalTime.of(12, 0);

        long duration1 = java.time.Duration.between(slot1.startTime, slot1.endTime).toHours();
        assertEquals(3, duration1);

        DoctorAvailability slot2 = new DoctorAvailability();
        slot2.startTime = LocalTime.of(14, 0);
        slot2.endTime = LocalTime.of(18, 30);

        long duration2Minutes = java.time.Duration.between(slot2.startTime, slot2.endTime).toMinutes();
        assertEquals(270, duration2Minutes); // 4 hours 30 minutes = 270 minutes
    }

    @Test
    public void testTimeSlotOverlap() {
        DoctorAvailability slot1 = new DoctorAvailability();
        slot1.startTime = LocalTime.of(9, 0);
        slot1.endTime = LocalTime.of(12, 0);

        DoctorAvailability slot2 = new DoctorAvailability();
        slot2.startTime = LocalTime.of(10, 0);
        slot2.endTime = LocalTime.of(13, 0);

        // Check if slots overlap
        boolean overlaps = slot1.startTime.isBefore(slot2.endTime) &&
                slot1.endTime.isAfter(slot2.startTime);
        assertTrue(overlaps);
    }

    @Test
    public void testNoTimeSlotOverlap() {
        DoctorAvailability morning = new DoctorAvailability();
        morning.startTime = LocalTime.of(9, 0);
        morning.endTime = LocalTime.of(12, 0);

        DoctorAvailability afternoon = new DoctorAvailability();
        afternoon.startTime = LocalTime.of(14, 0);
        afternoon.endTime = LocalTime.of(17, 0);

        // Check if slots do NOT overlap
        boolean overlaps = morning.startTime.isBefore(afternoon.endTime) &&
                morning.endTime.isAfter(afternoon.startTime);
        assertFalse(overlaps);
    }
}