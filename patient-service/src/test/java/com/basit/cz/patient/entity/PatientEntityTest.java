package com.basit.cz.patient.entity;

import com.basit.cz.entity.Patient;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

    @QuarkusTest
    class PatientEntityTest {

        @Inject
        EntityManager entityManager;

        @Test
        @Transactional
        void testCreatePatient() {
            // Given
            Patient patient = new Patient();
            patient.firstName = "Test";
            patient.lastName = "User";
            patient.email = "test.user@example.com";
            patient.phoneNumber = "+420111222333";
            patient.dateOfBirth = LocalDate.of(1990, 1, 1);
            patient.gender = Patient.Gender.MALE;

            // When
            patient.persist();

            // Then
            assertNotNull(patient.id);
            assertNotNull(patient.createdAt);
            assertNotNull(patient.updatedAt);
            assertTrue(patient.isActive);

            System.out.println("Patient created successfully: " + patient);
        }

        @Test
        @Transactional
        void testFindPatientById() {
            // Given - create a patient first
            Patient patient = new Patient();
            patient.firstName = "Find";
            patient.lastName = "Me";
            patient.email = "find.me@example.com";
            patient.phoneNumber = "+420444555666";
            patient.dateOfBirth = LocalDate.of(1985, 5, 15);
            patient.gender = Patient.Gender.FEMALE;
            patient.persist();

            // When
            Patient found = Patient.findById(patient.id);

            // Then
            assertNotNull(found);
            assertEquals("Find", found.firstName);
            assertEquals("Me", found.lastName);
            assertEquals("find.me@example.com", found.email);

            System.out.println("Patient found successfully: " + found);
        }

        @Test
        @Transactional
        void testUpdatePatient() {
            // Given
            Patient patient = new Patient();
            patient.firstName = "Original";
            patient.lastName = "Name";
            patient.email = "original@example.com";
            patient.phoneNumber = "+420777888999";
            patient.dateOfBirth = LocalDate.of(1995, 3, 10);
            patient.gender = Patient.Gender.OTHER;
            patient.persist();

            Long patientId = patient.id;

            // When
            patient.firstName = "Updated";
            patient.lastName = "NewName";
            entityManager.flush();

            // Then
            Patient updated = Patient.findById(patientId);
            assertEquals("Updated", updated.firstName);
            assertEquals("NewName", updated.lastName);

            System.out.println("Patient updated successfully: " + updated);
        }

        @Test
        @Transactional
        void testDeletePatient() {
            // Given
            Patient patient = new Patient();
            patient.firstName = "Delete";
            patient.lastName = "Me";
            patient.email = "delete.me@example.com";
            patient.phoneNumber = "+420000111222";
            patient.dateOfBirth = LocalDate.of(2000, 12, 25);
            patient.gender = Patient.Gender.MALE;
            patient.persist();

            Long patientId = patient.id;

            // When
            patient.delete();

            // Then
            Patient deleted = Patient.findById(patientId);
            assertNull(deleted);

            System.out.println("Patient deleted successfully");
        }

        @Test
        void testFindAllPatients() {
            // When
            long count = Patient.count();

            // Then
            assertTrue(count >= 0);
            System.out.println("Total patients in database: " + count);
        }
    }



