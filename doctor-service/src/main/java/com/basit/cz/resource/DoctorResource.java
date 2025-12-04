package com.basit.cz.resource;

import com.basit.cz.dto.CreateDoctorRequest;
import com.basit.cz.dto.DoctorDTO;
import com.basit.cz.dto.UpdateDoctorRequest;
import com.basit.cz.service.DoctorService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * Doctor REST Resource
 *
 * Provides REST API endpoints for doctor management.
 * Base path: /api/doctors
 *
 * Endpoints:
 * - POST   /register           - Register new doctor
 * - GET    /{id}              - Get doctor by ID
 * - PUT    /{id}              - Update doctor
 * - DELETE /{id}              - Deactivate doctor
 * - GET    /                  - Get all active doctors
 * - GET    /search            - Search by name
 * - GET    /specialization/{specialization} - Find by specialization
 * - GET    /top-rated         - Get top-rated doctors
 * - GET    /available/{day}   - Find available on day
 * - GET    /specializations   - List all specializations
 * - GET    /statistics        - Get doctor statistics
 * - POST   /{id}/activate     - Activate doctor
 * - POST   /{id}/deactivate   - Deactivate doctor
 */
@Path("/api/doctors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Doctor", description = "Doctor management endpoints")
public class DoctorResource {

    @Inject
    DoctorService doctorService;

    /**
     * Register a new doctor
     *
     * @param request Doctor registration data
     * @return Created doctor (201)
     */
    @POST
    @Path("/register")
    @Operation(summary = "Register new doctor", description = "Create a new doctor account")
    @APIResponse(responseCode = "201", description = "Doctor created successfully")
    @APIResponse(responseCode = "400", description = "Invalid input or email already exists")
    public Response registerDoctor(@Valid CreateDoctorRequest request) {
        DoctorDTO doctor = doctorService.registerDoctor(request);
        return Response.status(Response.Status.CREATED).entity(doctor).build();
    }

    /**
     * Get doctor by ID
     *
     * @param id Doctor ID
     * @return Doctor details (200)
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "Get doctor by ID", description = "Retrieve doctor details")
    @APIResponse(responseCode = "200", description = "Doctor found")
    @APIResponse(responseCode = "404", description = "Doctor not found")
    public Response getDoctorById(@PathParam("id") Long id) {
        DoctorDTO doctor = doctorService.getDoctorById(id);
        return Response.ok(doctor).build();
    }

    /**
     * Update doctor information
     *
     * @param id Doctor ID
     * @param request Update data
     * @return Updated doctor (200)
     */
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update doctor", description = "Update doctor information")
    @APIResponse(responseCode = "200", description = "Doctor updated successfully")
    @APIResponse(responseCode = "404", description = "Doctor not found")
    @APIResponse(responseCode = "400", description = "Invalid input")
    public Response updateDoctor(@PathParam("id") Long id,
                                 @Valid UpdateDoctorRequest request) {
        DoctorDTO doctor = doctorService.updateDoctor(id, request);
        return Response.ok(doctor).build();
    }

    /**
     * Delete (deactivate) doctor
     *
     * @param id Doctor ID
     * @return No content (204)
     */
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Deactivate doctor", description = "Soft delete doctor account")
    @APIResponse(responseCode = "204", description = "Doctor deactivated successfully")
    @APIResponse(responseCode = "404", description = "Doctor not found")
    public Response deleteDoctor(@PathParam("id") Long id) {
        doctorService.deactivateDoctor(id);
        return Response.noContent().build();
    }

    /**
     * Get all active doctors
     *
     * @return List of active doctors (200)
     */
    @GET
    @Operation(summary = "Get all active doctors", description = "List all active doctor accounts")
    @APIResponse(responseCode = "200", description = "Doctors retrieved successfully")
    public Response getAllActiveDoctors() {
        List<DoctorDTO> doctors = doctorService.getAllActiveDoctors();
        return Response.ok(doctors).build();
    }

    /**
     * Search doctors by name
     *
     * @param query Search term
     * @return Matching doctors (200)
     */
    @GET
    @Path("/search")
    @Operation(summary = "Search doctors by name", description = "Search doctors by first or last name")
    @APIResponse(responseCode = "200", description = "Search completed")
    public Response searchByName(@QueryParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Search query is required")
                    .build();
        }
        List<DoctorDTO> doctors = doctorService.searchByName(query);
        return Response.ok(doctors).build();
    }

    /**
     * Find doctors by specialization
     *
     * @param specialization Medical specialization
     * @return Doctors with matching specialization (200)
     */
    @GET
    @Path("/specialization/{specialization}")
    @Operation(summary = "Find by specialization", description = "Get doctors by medical specialization")
    @APIResponse(responseCode = "200", description = "Doctors retrieved")
    public Response findBySpecialization(@PathParam("specialization") String specialization) {
        List<DoctorDTO> doctors = doctorService.findBySpecialization(specialization);
        return Response.ok(doctors).build();
    }

    /**
     * Get top-rated doctors
     *
     * @return Top-rated doctors (rating >= 4.0) (200)
     */
    @GET
    @Path("/top-rated")
    @Operation(summary = "Get top-rated doctors", description = "Get doctors with rating >= 4.0")
    @APIResponse(responseCode = "200", description = "Top-rated doctors retrieved")
    public Response findTopRated() {
        List<DoctorDTO> doctors = doctorService.findTopRated();
        return Response.ok(doctors).build();
    }

    /**
     * Find doctors by minimum rating
     *
     * @param rating Minimum rating
     * @return Doctors with rating >= specified value (200)
     */
    @GET
    @Path("/rating/{rating}")
    @Operation(summary = "Find by minimum rating", description = "Get doctors with minimum rating")
    @APIResponse(responseCode = "200", description = "Doctors retrieved")
    public Response findByMinimumRating(@PathParam("rating") double rating) {
        List<DoctorDTO> doctors = doctorService.findByMinimumRating(rating);
        return Response.ok(doctors).build();
    }

    /**
     * Find doctors by minimum experience
     *
     * @param years Minimum years of experience
     * @return Doctors with experience >= specified years (200)
     */
    @GET
    @Path("/experience/{years}")
    @Operation(summary = "Find by experience", description = "Get doctors with minimum years of experience")
    @APIResponse(responseCode = "200", description = "Doctors retrieved")
    public Response findByMinimumExperience(@PathParam("years") int years) {
        List<DoctorDTO> doctors = doctorService.findByMinimumExperience(years);
        return Response.ok(doctors).build();
    }

    /**
     * Find doctors available on specific day
     *
     * @param day Day of week (e.g., MONDAY)
     * @return Available doctors (200)
     */
    @GET
    @Path("/available/{day}")
    @Operation(summary = "Find available on day", description = "Get doctors available on specific day of week")
    @APIResponse(responseCode = "200", description = "Available doctors retrieved")
    @APIResponse(responseCode = "400", description = "Invalid day of week")
    public Response findAvailableOnDay(@PathParam("day") String day) {
        try {
            List<DoctorDTO> doctors = doctorService.findAvailableOnDay(day);
            return Response.ok(doctors).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid day of week: " + day)
                    .build();
        }
    }

    /**
     * Find doctors by consultation fee range
     *
     * @param minFee Minimum fee
     * @param maxFee Maximum fee
     * @return Doctors within fee range (200)
     */
    @GET
    @Path("/fee-range")
    @Operation(summary = "Find by fee range", description = "Get doctors within consultation fee range")
    @APIResponse(responseCode = "200", description = "Doctors retrieved")
    @APIResponse(responseCode = "400", description = "Invalid fee range")
    public Response findByConsultationFeeRange(@QueryParam("min") double minFee,
                                               @QueryParam("max") double maxFee) {
        if (minFee < 0 || maxFee < 0 || minFee > maxFee) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid fee range")
                    .build();
        }
        List<DoctorDTO> doctors = doctorService.findByConsultationFeeRange(minFee, maxFee);
        return Response.ok(doctors).build();
    }

    /**
     * Get all specializations
     *
     * @return List of specializations (200)
     */
    @GET
    @Path("/specializations")
    @Operation(summary = "Get all specializations", description = "List all medical specializations")
    @APIResponse(responseCode = "200", description = "Specializations retrieved")
    public Response getAllSpecializations() {
        List<String> specializations = doctorService.getAllSpecializations();
        return Response.ok(specializations).build();
    }

    /**
     * Get doctor statistics
     *
     * @return Statistics (total, avg rating, avg experience) (200)
     */
    @GET
    @Path("/statistics")
    @Operation(summary = "Get doctor statistics", description = "Get aggregate statistics for all doctors")
    @APIResponse(responseCode = "200", description = "Statistics retrieved")
    public Response getStatistics() {
        DoctorService.DoctorStatistics stats = doctorService.getStatistics();
        return Response.ok(stats).build();
    }

    /**
     * Activate doctor account
     *
     * @param id Doctor ID
     * @return No content (204)
     */
    @POST
    @Path("/{id}/activate")
    @Operation(summary = "Activate doctor", description = "Activate a deactivated doctor account")
    @APIResponse(responseCode = "204", description = "Doctor activated successfully")
    @APIResponse(responseCode = "404", description = "Doctor not found")
    public Response activateDoctor(@PathParam("id") Long id) {
        doctorService.activateDoctor(id);
        return Response.noContent().build();
    }

    /**
     * Deactivate doctor account
     *
     * @param id Doctor ID
     * @return No content (204)
     */
    @POST
    @Path("/{id}/deactivate")
    @Operation(summary = "Deactivate doctor", description = "Deactivate a doctor account")
    @APIResponse(responseCode = "204", description = "Doctor deactivated successfully")
    @APIResponse(responseCode = "404", description = "Doctor not found")
    public Response deactivateDoctor(@PathParam("id") Long id) {
        doctorService.deactivateDoctor(id);
        return Response.noContent().build();
    }
}









