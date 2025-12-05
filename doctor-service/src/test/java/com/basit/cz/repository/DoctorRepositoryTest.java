package com.basit.cz.repository;

import com.basit.cz.entity.Doctor;
import com.basit.cz.entity.DoctorAvailability;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DoctorRepository
 */
@QuarkusTest
public class DoctorRepositoryTest {

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    DoctorAvailabilityRepository availabilityRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        // Clean up database before each test
        availabilityRepository.deleteAll();
        doctorRepository.deleteAll();
    }

    // ===============================================
    // BASIC CRUD TESTS
    // ===============================================

    @Test
    @Transactional
    public void testPersistDoctor() {
        Doctor doctor = createTestDoctor("John", "Smith", "john.smith@test.com");
        doctorRepository.persist(doctor);

        assertNotNull(doctor.id);
        assertEquals("John", doctor.firstName);
        assertEquals("Smith", doctor.lastName);
    }

    @Test
    @Transactional
    public void testFindDoctorById() {
        Doctor doctor = createTestDoctor("Jane", "Doe", "jane.doe@test.com");
        doctorRepository.persist(doctor);

        Doctor found = doctorRepository.findById(doctor.id);
        assertNotNull(found);
        assertEquals("Jane", found.firstName);
        assertEquals("Doe", found.lastName);
    }

    @Test
    @Transactional
    public void testFindActiveDoctors() {
        // Create active doctor
        Doctor active = createTestDoctor("Active", "Doctor", "active@test.com");
        active.isActive = true;
        doctorRepository.persist(active);

        // Create inactive doctor
        Doctor inactive = createTestDoctor("Inactive", "Doctor", "inactive@test.com");
        inactive.isActive = false;
        doctorRepository.persist(inactive);

        List<Doctor> activeDoctors = doctorRepository.findActiveDoctors();

        assertTrue(activeDoctors.size() >= 1);
        assertTrue(activeDoctors.stream().allMatch(d -> d.isActive));
    }

    // ===============================================
    // EMAIL TESTS
    // ===============================================

    @Test
    @Transactional
    public void testFindByEmail() {
        Doctor doctor = createTestDoctor("Email", "Test", "email.test@hospital.com");
        doctorRepository.persist(doctor);

        Doctor found = doctorRepository.findByEmail("email.test@hospital.com");
        assertNotNull(found);
        assertEquals("email.test@hospital.com", found.email);
    }

    @Test
    @Transactional
    public void testExistsByEmail() {
        Doctor doctor = createTestDoctor("Exists", "Test", "exists@test.com");
        doctorRepository.persist(doctor);

        assertTrue(doctorRepository.existsByEmail("exists@test.com"));
        assertFalse(doctorRepository.existsByEmail("notexists@test.com"));
    }

    @Test
    @Transactional
    public void testExistsByEmailCaseInsensitive() {
        Doctor doctor = createTestDoctor("Case", "Test", "CasE@Test.com");
        doctorRepository.persist(doctor);

        assertTrue(doctorRepository.existsByEmail("case@test.com"));
        assertTrue(doctorRepository.existsByEmail("CASE@TEST.COM"));
    }

    // ===============================================
    // SPECIALIZATION TESTS
    // ===============================================

    @Test
    @Transactional
    public void testFindBySpecialization() {
        Doctor cardio1 = createTestDoctor("Cardio", "One", "cardio1@test.com");
        cardio1.specialization = "Cardiology";
        doctorRepository.persist(cardio1);

        Doctor cardio2 = createTestDoctor("Cardio", "Two", "cardio2@test.com");
        cardio2.specialization = "Cardiology";
        doctorRepository.persist(cardio2);

        Doctor neuro = createTestDoctor("Neuro", "One", "neuro@test.com");
        neuro.specialization = "Neurology";
        doctorRepository.persist(neuro);

        List<Doctor> cardiologists = doctorRepository.findBySpecialization("Cardiology");

        assertEquals(2, cardiologists.size());
        assertTrue(cardiologists.stream().allMatch(d -> "Cardiology".equals(d.specialization)));
    }

    @Test
    @Transactional
    public void testGetAllSpecializations() {
        Doctor cardio = createTestDoctor("Cardio", "Doc", "cardio@test.com");
        cardio.specialization = "Cardiology";
        doctorRepository.persist(cardio);

        Doctor neuro = createTestDoctor("Neuro", "Doc", "neuro@test.com");
        neuro.specialization = "Neurology";
        doctorRepository.persist(neuro);

        List<String> specializations = doctorRepository.getAllSpecializations();

        assertTrue(specializations.size() >= 2);
        assertTrue(specializations.contains("Cardiology"));
        assertTrue(specializations.contains("Neurology"));
    }

    @Test
    @Transactional
    public void testCountBySpecialization() {
        Doctor cardio1 = createTestDoctor("C1", "Doc", "c1@test.com");
        cardio1.specialization = "Cardiology";
        doctorRepository.persist(cardio1);

        Doctor cardio2 = createTestDoctor("C2", "Doc", "c2@test.com");
        cardio2.specialization = "Cardiology";
        doctorRepository.persist(cardio2);

        long count = doctorRepository.countBySpecialization("Cardiology");
        assertEquals(2, count);
    }

    // ===============================================
    // SEARCH TESTS
    // ===============================================

    @Test
    @Transactional
    public void testSearchByName() {
        Doctor john = createTestDoctor("John", "Smith", "john.smith@test.com");
        doctorRepository.persist(john);

        Doctor jane = createTestDoctor("Jane", "Johnson", "jane.johnson@test.com");
        doctorRepository.persist(jane);

        // Search by first name
        List<Doctor> johnResults = doctorRepository.searchByName("John");
        assertTrue(johnResults.size() >= 1);
        assertTrue(johnResults.stream().anyMatch(d -> "John".equals(d.firstName)));

        // Search by last name
        List<Doctor> smithResults = doctorRepository.searchByName("Smith");
        assertTrue(smithResults.size() >= 1);
        assertTrue(smithResults.stream().anyMatch(d -> "Smith".equals(d.lastName)));
    }

    @Test
    @Transactional
    public void testSearchByNameCaseInsensitive() {
        Doctor doctor = createTestDoctor("Doctor", "Name", "doctor.name@test.com");
        doctorRepository.persist(doctor);

        List<Doctor> lowerCase = doctorRepository.searchByName("doctor");
        List<Doctor> upperCase = doctorRepository.searchByName("DOCTOR");

        assertTrue(lowerCase.size() >= 1);
        assertTrue(upperCase.size() >= 1);
    }

    // ===============================================
    // RATING TESTS
    // ===============================================

    @Test
    @Transactional
    public void testFindTopRated() {
        Doctor highRated = createTestDoctor("High", "Rated", "high@test.com");
        highRated.averageRating = 4.5;
        doctorRepository.persist(highRated);

        Doctor lowRated = createTestDoctor("Low", "Rated", "low@test.com");
        lowRated.averageRating = 3.5;
        doctorRepository.persist(lowRated);

        List<Doctor> topRated = doctorRepository.findTopRated();

        assertTrue(topRated.size() >= 1);
        assertTrue(topRated.stream().allMatch(d -> d.averageRating >= 4.0));
    }

    @Test
    @Transactional
    public void testFindByMinimumRating() {
        Doctor rating45 = createTestDoctor("R45", "Doc", "r45@test.com");
        rating45.averageRating = 4.5;
        doctorRepository.persist(rating45);

        Doctor rating40 = createTestDoctor("R40", "Doc", "r40@test.com");
        rating40.averageRating = 4.0;
        doctorRepository.persist(rating40);

        Doctor rating35 = createTestDoctor("R35", "Doc", "r35@test.com");
        rating35.averageRating = 3.5;
        doctorRepository.persist(rating35);

        List<Doctor> minRating40 = doctorRepository.findByMinimumRating(4.0);

        assertEquals(2, minRating40.size());
        assertTrue(minRating40.stream().allMatch(d -> d.averageRating >= 4.0));
    }

    @Test
    @Transactional
    public void testFindBySpecializationAndRating() {
        Doctor cardioHigh = createTestDoctor("CH", "Doc", "ch@test.com");
        cardioHigh.specialization = "Cardiology";
        cardioHigh.averageRating = 4.5;
        doctorRepository.persist(cardioHigh);

        Doctor cardioLow = createTestDoctor("CL", "Doc", "cl@test.com");
        cardioLow.specialization = "Cardiology";
        cardioLow.averageRating = 3.5;
        doctorRepository.persist(cardioLow);

        List<Doctor> results = doctorRepository.findBySpecializationAndRating("Cardiology", 4.0);

        assertEquals(1, results.size());
        assertEquals("Cardiology", results.get(0).specialization);
        assertTrue(results.get(0).averageRating >= 4.0);
    }

    // ===============================================
    // EXPERIENCE TESTS
    // ===============================================

    @Test
    @Transactional
    public void testFindByMinimumExperience() {
        Doctor exp20 = createTestDoctor("E20", "Doc", "e20@test.com");
        exp20.yearsOfExperience = 20;
        doctorRepository.persist(exp20);

        Doctor exp10 = createTestDoctor("E10", "Doc", "e10@test.com");
        exp10.yearsOfExperience = 10;
        doctorRepository.persist(exp10);

        Doctor exp5 = createTestDoctor("E5", "Doc", "e5@test.com");
        exp5.yearsOfExperience = 5;
        doctorRepository.persist(exp5);

        List<Doctor> minExp10 = doctorRepository.findByMinimumExperience(10);

        assertEquals(2, minExp10.size());
        assertTrue(minExp10.stream().allMatch(d -> d.yearsOfExperience >= 10));
    }

    // ===============================================
    // FEE TESTS
    // ===============================================

    @Test
    @Transactional
    public void testFindByConsultationFeeRange() {
        Doctor fee100 = createTestDoctor("F100", "Doc", "f100@test.com");
        fee100.consultationFee = 100.0;
        doctorRepository.persist(fee100);

        Doctor fee150 = createTestDoctor("F150", "Doc", "f150@test.com");
        fee150.consultationFee = 150.0;
        doctorRepository.persist(fee150);

        Doctor fee200 = createTestDoctor("F200", "Doc", "f200@test.com");
        fee200.consultationFee = 200.0;
        doctorRepository.persist(fee200);

        List<Doctor> range = doctorRepository.findByConsultationFeeRange(100.0, 180.0);

        assertEquals(2, range.size());
        assertTrue(range.stream().allMatch(d ->
                d.consultationFee >= 100.0 && d.consultationFee <= 180.0
        ));
    }

    // ===============================================
    // AVAILABILITY TESTS
    // ===============================================

    @Test
    @Transactional
    public void testFindAvailableOnDay() {
        // Create doctor with Monday availability
        Doctor doctor = createTestDoctor("Available", "Monday", "monday@test.com");
        doctorRepository.persist(doctor);

        DoctorAvailability availability = new DoctorAvailability();
        availability.doctor = doctor;
        availability.dayOfWeek = "MONDAY";
        availability.startTime = LocalTime.of(9, 0);
        availability.endTime = LocalTime.of(17, 0);
        availability.isActive = true;
        availability.createdAt = LocalDateTime.now();
        availabilityRepository.persist(availability);

        List<Doctor> mondayDoctors = doctorRepository.findAvailableOnDay("MONDAY");

        assertTrue(mondayDoctors.size() >= 1);
        assertTrue(mondayDoctors.stream().anyMatch(d -> d.id.equals(doctor.id)));
    }

    // ===============================================
    // AGGREGATION TESTS
    // ===============================================

    @Test
    @Transactional
    public void testCountActiveDoctors() {
        Doctor active1 = createTestDoctor("A1", "Doc", "a1@test.com");
        active1.isActive = true;
        doctorRepository.persist(active1);

        Doctor active2 = createTestDoctor("A2", "Doc", "a2@test.com");
        active2.isActive = true;
        doctorRepository.persist(active2);

        Doctor inactive = createTestDoctor("I1", "Doc", "i1@test.com");
        inactive.isActive = false;
        doctorRepository.persist(inactive);

        long count = doctorRepository.countActiveDoctors();
        assertTrue(count >= 2);
    }

    @Test
    @Transactional
    public void testCalculateAverageRating() {
        Doctor d1 = createTestDoctor("D1", "Doc", "d1@test.com");
        d1.averageRating = 4.0;
        doctorRepository.persist(d1);

        Doctor d2 = createTestDoctor("D2", "Doc", "d2@test.com");
        d2.averageRating = 5.0;
        doctorRepository.persist(d2);

        double avgRating = doctorRepository.calculateAverageRating();
        assertTrue(avgRating >= 4.0 && avgRating <= 5.0);
    }

    @Test
    @Transactional
    public void testCalculateAverageExperience() {
        Doctor d1 = createTestDoctor("D1", "Doc", "d1@test.com");
        d1.yearsOfExperience = 10;
        doctorRepository.persist(d1);

        Doctor d2 = createTestDoctor("D2", "Doc", "d2@test.com");
        d2.yearsOfExperience = 20;
        doctorRepository.persist(d2);

        double avgExp = doctorRepository.calculateAverageExperience();
        assertTrue(avgExp >= 10.0 && avgExp <= 20.0);
    }

    @Test
    @Transactional
    public void testGetDoctorStatistics() {
        Doctor d1 = createTestDoctor("Stat1", "Doc", "stat1@test.com");
        d1.averageRating = 4.5;
        d1.yearsOfExperience = 15;
        doctorRepository.persist(d1);

        Doctor d2 = createTestDoctor("Stat2", "Doc", "stat2@test.com");
        d2.averageRating = 4.0;
        d2.yearsOfExperience = 10;
        doctorRepository.persist(d2);

        Object[] stats = doctorRepository.getDoctorStatistics();

        assertNotNull(stats);
        assertEquals(3, stats.length);

        Long totalDoctors = ((Number) stats[0]).longValue();
        assertTrue(totalDoctors >= 2);
    }

    // ===============================================
    // HELPER METHODS
    // ===============================================

    private Doctor createTestDoctor(String firstName, String lastName, String email) {
        Doctor doctor = new Doctor();
        doctor.firstName = firstName;
        doctor.lastName = lastName;
        doctor.email = email;
        doctor.phoneNumber = "+1234567890";
        doctor.specialization = "General";
        doctor.yearsOfExperience = 5;
        doctor.licenseNumber = "LIC-" + java.util.UUID.randomUUID().toString();
        doctor.consultationFee = 100.0;
        doctor.bio = "Test doctor bio";
        doctor.qualifications = "MD";
        doctor.averageRating = 4.0;
        doctor.totalReviews = 10;
        doctor.isActive = true;
        doctor.createdAt = LocalDateTime.now();
        doctor.updatedAt = LocalDateTime.now();
        return doctor;
    }
}