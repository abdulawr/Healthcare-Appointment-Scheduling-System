package com.basit.cz.service;


import com.basit.cz.dto.*;
import com.basit.cz.entity.*;
import com.basit.cz.exception.NotFoundException;
import com.basit.cz.repository.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Doctor management operations
 */
@ApplicationScoped
public class DoctorService {

    @Inject
    DoctorRepository doctorRepository;

    @Inject
    DoctorAvailabilityRepository availabilityRepository;

    @Inject
    DoctorReviewRepository reviewRepository;

    @Inject
    DoctorScheduleRepository scheduleRepository;

    // ===============================================
    // CRUD OPERATIONS
    // ===============================================

    /**
     * Register a new doctor
     */
    @Transactional
    public DoctorDTO registerDoctor(CreateDoctorRequest request) {
        // Check if email already exists
        if (doctorRepository.existsByEmail(request.email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create doctor entity
        Doctor doctor = new Doctor();
        doctor.firstName = request.firstName;
        doctor.lastName = request.lastName;
        doctor.email = request.email;
        doctor.phoneNumber = request.phoneNumber;
        doctor.specialization = request.specialization;
        doctor.yearsOfExperience = request.yearsOfExperience;
        doctor.licenseNumber = request.licenseNumber;
        doctor.consultationFee = request.consultationFee;
        doctor.bio = request.bio;
        doctor.qualifications = request.qualifications;
        doctor.isActive = true;
        doctor.averageRating = 0.0;
        doctor.totalReviews = 0;
        doctor.createdAt = LocalDateTime.now();
        doctor.updatedAt = LocalDateTime.now();

        // Persist
        doctorRepository.persist(doctor);

        // Convert to DTO
        return DoctorMapper.toDTO(doctor);
    }

    /**
     * Get doctor by ID
     */
    public DoctorDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + id));
        return DoctorMapper.toDTO(doctor);
    }

    /**
     * Get all active doctors
     */
    public List<DoctorDTO> getAllActiveDoctors() {
        return doctorRepository.findActiveDoctors().stream()
                .map(DoctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update doctor information
     */
    @Transactional
    public DoctorDTO updateDoctor(Long id, UpdateDoctorRequest request) {
        Doctor doctor = doctorRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + id));

        // Update fields if provided
        if (request.phoneNumber != null) {
            doctor.phoneNumber = request.phoneNumber;
        }
        if (request.consultationFee != null) {
            doctor.consultationFee = request.consultationFee;
        }
        if (request.bio != null) {
            doctor.bio = request.bio;
        }
        if (request.qualifications != null) {
            doctor.qualifications = request.qualifications;
        }

        doctor.updatedAt = LocalDateTime.now();
        doctorRepository.persist(doctor);

        return DoctorMapper.toDTO(doctor);
    }

    /**
     * Deactivate doctor (soft delete)
     */
    @Transactional
    public void deactivateDoctor(Long id) {
        Doctor doctor = doctorRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + id));

        doctor.isActive = false;
        doctor.updatedAt = LocalDateTime.now();
        doctorRepository.persist(doctor);
    }

    /**
     * Activate doctor
     */
    @Transactional
    public void activateDoctor(Long id) {
        Doctor doctor = doctorRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + id));

        doctor.isActive = true;
        doctor.updatedAt = LocalDateTime.now();
        doctorRepository.persist(doctor);
    }

    // ===============================================
    // SEARCH AND FILTER OPERATIONS
    // ===============================================

    /**
     * Find doctors by specialization
     */
    public List<DoctorDTO> findBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization).stream()
                .map(DoctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search doctors by name
     */
    public List<DoctorDTO> searchByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be empty");
        }

        return doctorRepository.searchByName(searchTerm).stream()
                .map(DoctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find top-rated doctors (rating >= 4.0)
     */
    public List<DoctorDTO> findTopRated() {
        return doctorRepository.findTopRated().stream()
                .map(DoctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find doctors by minimum rating
     */
    public List<DoctorDTO> findByMinimumRating(double minRating) {
        return doctorRepository.findByMinimumRating(minRating).stream()
                .map(DoctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find doctors by specialization and minimum rating
     */
    public List<DoctorDTO> findBySpecializationAndRating(String specialization, double minRating) {
        return doctorRepository.findBySpecializationAndRating(specialization, minRating).stream()
                .map(DoctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find doctors by minimum experience
     */
    public List<DoctorDTO> findByMinimumExperience(int minYears) {
        return doctorRepository.findByMinimumExperience(minYears).stream()
                .map(DoctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find doctors available on specific day
     */
    public List<DoctorDTO> findAvailableOnDay(String dayOfWeek) {
        if (!isValidDayOfWeek(dayOfWeek)) {
            throw new IllegalArgumentException(
                    "Invalid day of week. Use: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY"
            );
        }

        return doctorRepository.findAvailableOnDay(dayOfWeek).stream()
                .map(DoctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find doctors by consultation fee range
     */
    public List<DoctorDTO> findByConsultationFeeRange(double minFee, double maxFee) {
        if (minFee > maxFee) {
            throw new IllegalArgumentException("Invalid fee range: min must be less than or equal to max");
        }

        return doctorRepository.findByConsultationFeeRange(minFee, maxFee).stream()
                .map(DoctorMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ===============================================
    // UTILITY OPERATIONS
    // ===============================================

    /**
     * Get all specializations
     */
    public List<String> getAllSpecializations() {
        return doctorRepository.getAllSpecializations();
    }

    /**
     * Count doctors by specialization
     */
    public long countBySpecialization(String specialization) {
        return doctorRepository.countBySpecialization(specialization);
    }

    /**
     * Get doctor statistics
     */
    public DoctorStatistics getStatistics() {
        Object[] stats = doctorRepository.getDoctorStatistics();

        Long totalDoctors = ((Number) stats[0]).longValue();
        Double avgRating = stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0;
        Double avgExperience = stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0;

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

    // ===============================================
    // AVAILABILITY MANAGEMENT METHODS
    // ===============================================

    /**
     * Get all availability slots for a doctor
     */
    public List<AvailabilityDTO> getDoctorAvailability(Long doctorId) {
        Doctor doctor = doctorRepository.findByIdOptional(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + doctorId));

        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctorId(doctorId);

        return availabilities.stream()
                .map(a -> new AvailabilityDTO(
                        a.id,
                        doctor.id,
                        doctor.getFullName(),
                        a.dayOfWeek,
                        a.startTime,
                        a.endTime,
                        a.isActive
                ))
                .collect(Collectors.toList());
    }

    /**
     * Add new availability slot for doctor
     */
    @Transactional
    public AvailabilityDTO addDoctorAvailability(
            Long doctorId, CreateAvailabilityRequest request) {

        // Validate doctor exists
        Doctor doctor = doctorRepository.findByIdOptional(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + doctorId));

        // Validate day of week
        if (!isValidDayOfWeek(request.dayOfWeek)) {
            throw new IllegalArgumentException(
                    "Invalid day of week. Use: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY"
            );
        }

        // Validate time range
        if (request.startTime.isAfter(request.endTime) || request.startTime.equals(request.endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        // Check for conflicts
        List<DoctorAvailability> existingSlots =
                availabilityRepository.findByDoctorIdAndDayOfWeek(doctorId, request.dayOfWeek);

        for (DoctorAvailability slot : existingSlots) {
            if (slot.isActive && hasTimeConflict(
                    request.startTime, request.endTime, slot.startTime, slot.endTime)) {
                throw new IllegalArgumentException("Time slot conflicts with existing availability");
            }
        }

        // Create new availability
        DoctorAvailability availability = new DoctorAvailability();
        availability.doctor = doctor;
        availability.dayOfWeek = request.dayOfWeek.toUpperCase();
        availability.startTime = request.startTime;
        availability.endTime = request.endTime;
        availability.isActive = request.isActive != null ? request.isActive : true;
        availability.createdAt = LocalDateTime.now();

        availabilityRepository.persist(availability);

        return new AvailabilityDTO(
                availability.id,
                doctor.id,
                doctor.getFullName(),
                availability.dayOfWeek,
                availability.startTime,
                availability.endTime,
                availability.isActive
        );
    }

    /**
     * Update existing availability slot
     */
    @Transactional
    public AvailabilityDTO updateAvailability(
            Long availabilityId, CreateAvailabilityRequest request) {

        DoctorAvailability availability = availabilityRepository.findByIdOptional(availabilityId)
                .orElseThrow(() -> new NotFoundException("Availability not found with id: " + availabilityId));

        // Validate time range if provided
        if (request.startTime != null && request.endTime != null) {
            if (request.startTime.isAfter(request.endTime) || request.startTime.equals(request.endTime)) {
                throw new IllegalArgumentException("Start time must be before end time");
            }
            availability.startTime = request.startTime;
            availability.endTime = request.endTime;
        }

        // Update day if provided
        if (request.dayOfWeek != null) {
            if (!isValidDayOfWeek(request.dayOfWeek)) {
                throw new IllegalArgumentException("Invalid day of week");
            }
            availability.dayOfWeek = request.dayOfWeek.toUpperCase();
        }

        // Update active status if provided
        if (request.isActive != null) {
            availability.isActive = request.isActive;
        }

        availabilityRepository.persist(availability);

        return new AvailabilityDTO(
                availability.id,
                availability.doctor.id,
                availability.doctor.getFullName(),
                availability.dayOfWeek,
                availability.startTime,
                availability.endTime,
                availability.isActive
        );
    }

    /**
     * Delete availability slot
     */
    @Transactional
    public void deleteAvailability(Long availabilityId) {
        DoctorAvailability availability = availabilityRepository.findByIdOptional(availabilityId)
                .orElseThrow(() -> new NotFoundException("Availability not found with id: " + availabilityId));

        availabilityRepository.delete(availability);
    }

    // ===============================================
    // SPECIALIZATION METHODS
    // ===============================================

    /**
     * Get all specializations with details
     */
    public List<SpecializationDTO> getAllSpecializationsWithDetails() {
        List<String> specializations = doctorRepository.getAllSpecializations();

        return specializations.stream()
                .map(spec -> {
                    List<Doctor> doctors = doctorRepository.findBySpecialization(spec);
                    long count = doctors.size();
                    double avgFee = doctors.stream()
                            .filter(d -> d.consultationFee != null)
                            .mapToDouble(d -> d.consultationFee)
                            .average()
                            .orElse(0.0);
                    double avgRating = doctors.stream()
                            .mapToDouble(d -> d.averageRating)
                            .average()
                            .orElse(0.0);

                    return new SpecializationDTO(spec, count, avgFee, avgRating);
                })
                .collect(Collectors.toList());
    }

    // ===============================================
    // HELPER METHODS
    // ===============================================

    /**
     * Validate day of week
     */
    private boolean isValidDayOfWeek(String day) {
        try {
            DayOfWeek.valueOf(day.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if two time slots conflict
     */
    private boolean hasTimeConflict(LocalTime start1, LocalTime end1,
                                    LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
}





















