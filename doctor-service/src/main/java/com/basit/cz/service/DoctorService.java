package com.basit.cz.service;


import com.basit.cz.dto.CreateDoctorRequest;
import com.basit.cz.dto.DoctorDTO;
import com.basit.cz.dto.DoctorMapper;
import com.basit.cz.dto.UpdateDoctorRequest;
import com.basit.cz.entity.Doctor;
import com.basit.cz.repository.DoctorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Doctor Service
 *
 * Provides business logic for doctor management.
 * Handles CRUD operations, validation, and business rules.
 */
@ApplicationScoped
public class DoctorService {

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    DoctorMapper doctorMapper;

    /**
     * Register a new doctor
     *
     * @param request Doctor registration data
     * @return Created doctor DTO
     * @throws IllegalArgumentException if email already exists
     */
    @Transactional
    public DoctorDTO registerDoctor(CreateDoctorRequest request) {
        // Check if email already exists
        if (doctorRepository.findByEmail(request.email).isPresent()) {
            throw new IllegalArgumentException("Email already registered: " + request.email);
        }

        // Check if license number already exists (if provided)
        if (request.licenseNumber != null &&
                doctorRepository.findByLicenseNumber(request.licenseNumber).isPresent()) {
            throw new IllegalArgumentException("License number already registered: " + request.licenseNumber);
        }

        // Create and persist doctor
        Doctor doctor = doctorMapper.toEntity(request);
        doctor.persist();

        return doctorMapper.toDTO(doctor);
    }

    /**
     * Get doctor by ID
     *
     * @param id Doctor ID
     * @return Doctor DTO
     * @throws NotFoundException if doctor not found
     */
    public DoctorDTO getDoctorById(Long id) {
        Doctor doctor = Doctor.findById(id);
        if (doctor == null) {
            throw new NotFoundException("Doctor not found with id: " + id);
        }
        return doctorMapper.toDTO(doctor);
    }

    /**
     * Get all active doctors
     *
     * @return List of active doctor DTOs
     */
    public List<DoctorDTO> getAllActiveDoctors() {
        return doctorRepository.findAllActive()
                .stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update doctor information
     *
     * @param id Doctor ID
     * @param request Update data
     * @return Updated doctor DTO
     * @throws NotFoundException if doctor not found
     */
    @Transactional
    public DoctorDTO updateDoctor(Long id, UpdateDoctorRequest request) {
        Doctor doctor = Doctor.findById(id);
        if (doctor == null) {
            throw new NotFoundException("Doctor not found with id: " + id);
        }

        // Check email uniqueness if changing email
        if (request.email != null && !request.email.equals(doctor.email)) {
            if (doctorRepository.findByEmail(request.email).isPresent()) {
                throw new IllegalArgumentException("Email already registered: " + request.email);
            }
        }

        // Check license uniqueness if changing license
        if (request.licenseNumber != null && !request.licenseNumber.equals(doctor.licenseNumber)) {
            if (doctorRepository.findByLicenseNumber(request.licenseNumber).isPresent()) {
                throw new IllegalArgumentException("License number already registered: " + request.licenseNumber);
            }
        }

        // Update doctor
        doctorMapper.updateEntity(doctor, request);

        return doctorMapper.toDTO(doctor);
    }

    /**
     * Deactivate doctor account
     *
     * @param id Doctor ID
     * @throws NotFoundException if doctor not found
     */
    @Transactional
    public void deactivateDoctor(Long id) {
        Doctor doctor = Doctor.findById(id);
        if (doctor == null) {
            throw new NotFoundException("Doctor not found with id: " + id);
        }
        doctor.deactivate();
    }

    /**
     * Activate doctor account
     *
     * @param id Doctor ID
     * @throws NotFoundException if doctor not found
     */
    @Transactional
    public void activateDoctor(Long id) {
        Doctor doctor = Doctor.findById(id);
        if (doctor == null) {
            throw new NotFoundException("Doctor not found with id: " + id);
        }
        doctor.activate();
    }

    /**
     * Find doctors by specialization
     *
     * @param specialization Medical specialization
     * @return List of doctors
     */
    public List<DoctorDTO> findBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization)
                .stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search doctors by name
     *
     * @param searchTerm Search term
     * @return List of matching doctors
     */
    public List<DoctorDTO> searchByName(String searchTerm) {
        return doctorRepository.searchByName(searchTerm)
                .stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find top-rated doctors
     *
     * @return List of top-rated doctors (rating >= 4.0)
     */
    public List<DoctorDTO> findTopRated() {
        return doctorRepository.findTopRated()
                .stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find doctors by minimum rating
     *
     * @param minRating Minimum rating
     * @return List of doctors
     */
    public List<DoctorDTO> findByMinimumRating(double minRating) {
        return doctorRepository.findByMinimumRating(minRating)
                .stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find doctors by specialization and minimum rating
     *
     * @param specialization Medical specialization
     * @param minRating Minimum rating
     * @return List of doctors
     */
    public List<DoctorDTO> findBySpecializationAndRating(String specialization, double minRating) {
        return doctorRepository.findBySpecializationAndRating(specialization, minRating)
                .stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find doctors with minimum experience
     *
     * @param minYears Minimum years of experience
     * @return List of doctors
     */
    public List<DoctorDTO> findByMinimumExperience(int minYears) {
        return doctorRepository.findByMinimumExperience(minYears)
                .stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find doctors available on specific day
     *
     * @param dayOfWeek Day of week (e.g., "MONDAY")
     * @return List of available doctors
     */
    public List<DoctorDTO> findAvailableOnDay(String dayOfWeek) {
        return doctorRepository.findAvailableOnDay(dayOfWeek)
                .stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find doctors by consultation fee range
     *
     * @param minFee Minimum fee
     * @param maxFee Maximum fee
     * @return List of doctors
     */
    public List<DoctorDTO> findByConsultationFeeRange(double minFee, double maxFee) {
        return doctorRepository.findByConsultationFeeRange(minFee, maxFee)
                .stream()
                .map(doctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all specializations
     *
     * @return List of specializations
     */
    public List<String> getAllSpecializations() {
        return doctorRepository.getAllSpecializations();
    }

    /**
     * Count doctors by specialization
     *
     * @param specialization Medical specialization
     * @return Count of doctors
     */
    public long countBySpecialization(String specialization) {
        return doctorRepository.countBySpecialization(specialization);
    }

    /**
     * Get doctor statistics
     *
     * @return Statistics (total, avg rating, avg experience)
     */
    public DoctorStatistics getStatistics() {
        Object[] stats = doctorRepository.getDoctorStatistics();

        Long totalDoctors = ((Number) stats[0]).longValue();
        Double avgRating = (Double) stats[1];
        Double avgExperience = (Double) stats[2];

        return new DoctorStatistics(totalDoctors, avgRating, avgExperience);
    }

    /**
     * Doctor Statistics DTO
     */
    public static class DoctorStatistics {
        public Long totalDoctors;
        public Double averageRating;
        public Double averageExperience;

        public DoctorStatistics(Long totalDoctors, Double averageRating, Double averageExperience) {
            this.totalDoctors = totalDoctors;
            this.averageRating = averageRating;
            this.averageExperience = averageExperience;
        }
    }
}









