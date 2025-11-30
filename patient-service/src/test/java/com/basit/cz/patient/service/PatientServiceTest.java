package com.basit.cz.patient.service;

import com.basit.cz.dto.PatientDTO;
import com.basit.cz.entity.Patient;
import com.basit.cz.exception.DuplicateEmailException;
import com.basit.cz.exception.PatientNotFoundException;
import com.basit.cz.repository.PatientRepository;
import com.basit.cz.service.PatientService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Service tests for PatientService.
 *
 * Uses @InjectMock to mock the repository layer.
 * This allows us to test the service logic in isolation.
 */
@QuarkusTest
class PatientServiceTest {

    @Inject
    PatientService patientService;

    @InjectMock
    PatientRepository patientRepository;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        Mockito.reset(patientRepository);
    }

    @Test
    void testRegisterPatient_WithValidData_ShouldCreatePatient() {
        System.out.println("\n✅ TEST 1: Register patient with valid data");

        // Given
        PatientDTO.RegistrationRequest request = createRegistrationRequest(
                "John", "Doe", "john.doe@email.com", "+420111222333"
        );

        // Mock: Email doesn't exist
        when(patientRepository.emailExists("john.doe@email.com")).thenReturn(false);

        // Mock: Persist will set the ID
        doAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            patient.id = 1L;
            return null;
        }).when(patientRepository).persist(any(Patient.class));

        // When
        PatientDTO.Response response = patientService.registerPatient(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.id);
        assertEquals("John", response.firstName);
        assertEquals("john.doe@email.com", response.email);
        assertTrue(response.isActive);

        // Verify repository was called correctly
        verify(patientRepository, times(1)).emailExists("john.doe@email.com");
        verify(patientRepository, times(1)).persist(any(Patient.class));

        System.out.println("✅ Patient registered: " + response);
    }

    @Test
    void testRegisterPatient_WithDuplicateEmail_ShouldThrowException() {
        System.out.println("\n❌ TEST 2: Register patient with duplicate email");

        // Given
        PatientDTO.RegistrationRequest request = createRegistrationRequest(
                "John", "Doe", "existing@email.com", "+420111222333"
        );

        // Mock: Email already exists
        when(patientRepository.emailExists("existing@email.com")).thenReturn(true);

        // When & Then
        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> patientService.registerPatient(request)
        );

        assertTrue(exception.getMessage().contains("existing@email.com"));

        // Verify persist was never called
        verify(patientRepository, never()).persist(any(Patient.class));

        System.out.println("✅ Correctly threw exception: " + exception.getMessage());
    }

    @Test
    void testGetPatient_WhenPatientExists_ShouldReturnPatient() {
        System.out.println("\n🔍 TEST 3: Get patient (exists)");

        // Given
        Patient patient = createPatient(1L, "Jane", "Smith", "jane@email.com");
        when(patientRepository.findActiveById(1L)).thenReturn(Optional.of(patient));

        // When
        PatientDTO.Response response = patientService.getPatient(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.id);
        assertEquals("Jane", response.firstName);
        assertEquals("jane@email.com", response.email);

        verify(patientRepository, times(1)).findActiveById(1L);

        System.out.println("✅ Found patient: " + response);
    }

    @Test
    void testGetPatient_WhenPatientNotFound_ShouldThrowException() {
        System.out.println("\n❌ TEST 4: Get patient (not found)");

        // Given
        when(patientRepository.findActiveById(999L)).thenReturn(Optional.empty());

        // When & Then
        PatientNotFoundException exception = assertThrows(
                PatientNotFoundException.class,
                () -> patientService.getPatient(999L)
        );

        assertTrue(exception.getMessage().contains("999"));

        System.out.println("✅ Correctly threw exception: " + exception.getMessage());
    }

    @Test
    void testUpdatePatient_WithValidData_ShouldUpdatePatient() {
        System.out.println("\n📝 TEST 5: Update patient with valid data");

        // Given
        Long patientId = 1L;
        Patient existingPatient = createPatient(patientId, "John", "Doe", "john@email.com");

        PatientDTO.UpdateRequest request = createUpdateRequest(
                "John", "Doe", "john.new@email.com", "+420999888777"
        );

        // Mock: Patient exists
        when(patientRepository.findActiveById(patientId)).thenReturn(Optional.of(existingPatient));

        // Mock: New email doesn't exist
        when(patientRepository.emailExists("john.new@email.com")).thenReturn(false);

        // When
        PatientDTO.Response response = patientService.updatePatient(patientId, request);

        // Then
        assertNotNull(response);
        assertEquals("john.new@email.com", response.email);
        assertEquals("+420999888777", response.phoneNumber);

        verify(patientRepository, times(1)).findActiveById(patientId);
        verify(patientRepository, times(1)).emailExists("john.new@email.com");

        System.out.println("✅ Patient updated: " + response);
    }

    @Test
    void testUpdatePatient_WithDuplicateEmail_ShouldThrowException() {
        System.out.println("\n❌ TEST 6: Update patient with duplicate email");

        // Given
        Long patientId = 1L;
        Patient existingPatient = createPatient(patientId, "John", "Doe", "john@email.com");

        PatientDTO.UpdateRequest request = createUpdateRequest(
                "John", "Doe", "taken@email.com", "+420999888777"
        );

        // Mock: Patient exists
        when(patientRepository.findActiveById(patientId)).thenReturn(Optional.of(existingPatient));

        // Mock: New email already exists
        when(patientRepository.emailExists("taken@email.com")).thenReturn(true);

        // When & Then
        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> patientService.updatePatient(patientId, request)
        );

        assertTrue(exception.getMessage().contains("taken@email.com"));

        System.out.println("✅ Correctly threw exception: " + exception.getMessage());
    }

    @Test
    void testUpdatePatient_SameEmail_ShouldNotCheckDuplicate() {
        System.out.println("\n📝 TEST 7: Update patient with same email (no duplicate check)");

        // Given
        Long patientId = 1L;
        Patient existingPatient = createPatient(patientId, "John", "Doe", "john@email.com");

        PatientDTO.UpdateRequest request = createUpdateRequest(
                "John", "Doe Updated", "john@email.com", "+420999888777"  // Same email
        );

        // Mock: Patient exists
        when(patientRepository.findActiveById(patientId)).thenReturn(Optional.of(existingPatient));

        // When
        PatientDTO.Response response = patientService.updatePatient(patientId, request);

        // Then
        assertNotNull(response);
        assertEquals("john@email.com", response.email);

        // Verify emailExists was NOT called (because email didn't change)
        verify(patientRepository, never()).emailExists(anyString());

        System.out.println("✅ Patient updated without checking email: " + response);
    }

    @Test
    void testDeactivatePatient_WhenPatientExists_ShouldDeactivate() {
        System.out.println("\n🗑️ TEST 8: Deactivate patient");

        // Given
        Long patientId = 1L;
        Patient patient = createPatient(patientId, "John", "Doe", "john@email.com");
        patient.isActive = true;

        when(patientRepository.findById(patientId)).thenReturn(patient);

        // When
        patientService.deactivatePatient(patientId);

        // Then
        assertFalse(patient.isActive, "Patient should be deactivated");
        verify(patientRepository, times(1)).persist(patient);

        System.out.println("✅ Patient deactivated successfully");
    }

    @Test
    void testDeactivatePatient_WhenPatientNotFound_ShouldThrowException() {
        System.out.println("\n❌ TEST 9: Deactivate patient (not found)");

        // Given
        when(patientRepository.findById(999L)).thenReturn(null);

        // When & Then
        PatientNotFoundException exception = assertThrows(
                PatientNotFoundException.class,
                () -> patientService.deactivatePatient(999L)
        );

        assertTrue(exception.getMessage().contains("999"));

        System.out.println("✅ Correctly threw exception: " + exception.getMessage());
    }

    @Test
    void testSearchPatients_ShouldReturnMatchingActivePatients() {
        System.out.println("\n🔍 TEST 10: Search patients");

        // Given
        Patient active1 = createPatient(1L, "John", "Doe", "john@email.com");
        active1.isActive = true;

        Patient active2 = createPatient(2L, "Johnny", "Walker", "johnny@email.com");
        active2.isActive = true;

        Patient inactive = createPatient(3L, "John", "Inactive", "inactive@email.com");
        inactive.isActive = false;

        List<Patient> searchResults = List.of(active1, active2, inactive);
        when(patientRepository.searchByName("john")).thenReturn(searchResults);

        // When
        List<PatientDTO.Response> results = patientService.searchPatients("john");

        // Then
        assertEquals(2, results.size(), "Should only return active patients");
        assertTrue(results.stream().allMatch(p -> p.isActive), "All results should be active");

        System.out.println("✅ Found " + results.size() + " active patients");
    }

    @Test
    void testGetAllActivePatients_ShouldReturnOnlyActivePatients() {
        System.out.println("\n📋 TEST 11: Get all active patients");

        // Given
        Patient patient1 = createPatient(1L, "John", "Doe", "john@email.com");
        Patient patient2 = createPatient(2L, "Jane", "Smith", "jane@email.com");

        when(patientRepository.findAllActive()).thenReturn(List.of(patient1, patient2));

        // When
        List<PatientDTO.Response> results = patientService.getAllActivePatients();

        // Then
        assertEquals(2, results.size());
        verify(patientRepository, times(1)).findAllActive();

        System.out.println("✅ Found " + results.size() + " active patients");
    }

    @Test
    void testGetActivePatientCount_ShouldReturnCorrectCount() {
        System.out.println("\n📊 TEST 12: Get active patient count");

        // Given
        when(patientRepository.countActive()).thenReturn(5L);

        // When
        long count = patientService.getActivePatientCount();

        // Then
        assertEquals(5L, count);
        verify(patientRepository, times(1)).countActive();

        System.out.println("✅ Active patient count: " + count);
    }

    // Helper methods

    private PatientDTO.RegistrationRequest createRegistrationRequest(
            String firstName, String lastName, String email, String phone) {
        PatientDTO.RegistrationRequest request = new PatientDTO.RegistrationRequest();
        request.firstName = firstName;
        request.lastName = lastName;
        request.email = email;
        request.phoneNumber = phone;
        request.dateOfBirth = LocalDate.of(1990, 1, 1);
        request.gender = Patient.Gender.MALE;
        return request;
    }

    private PatientDTO.UpdateRequest createUpdateRequest(
            String firstName, String lastName, String email, String phone) {
        PatientDTO.UpdateRequest request = new PatientDTO.UpdateRequest();
        request.firstName = firstName;
        request.lastName = lastName;
        request.email = email;
        request.phoneNumber = phone;
        request.dateOfBirth = LocalDate.of(1990, 1, 1);
        request.gender = Patient.Gender.MALE;
        return request;
    }

    private Patient createPatient(Long id, String firstName, String lastName, String email) {
        Patient patient = new Patient();
        patient.id = id;
        patient.firstName = firstName;
        patient.lastName = lastName;
        patient.email = email;
        patient.phoneNumber = "+420111222333";
        patient.dateOfBirth = LocalDate.of(1990, 1, 1);
        patient.gender = Patient.Gender.MALE;
        patient.isActive = true;
        return patient;
    }
}
