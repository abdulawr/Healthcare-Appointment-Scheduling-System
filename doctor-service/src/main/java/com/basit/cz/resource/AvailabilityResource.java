package com.basit.cz.resource;

import com.basit.cz.dto.AvailabilityDTO;
import com.basit.cz.dto.CreateAvailabilityRequest;
import com.basit.cz.service.DoctorService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * REST Resource for Doctor Availability Management
 *
 * Handles all availability-related operations:
 * - Get doctor's availability schedule
 * - Add new availability slots
 * - Update existing slots
 * - Remove availability slots
 */
@Path("/api/doctors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Doctor Availability", description = "Doctor availability management endpoints")
public class AvailabilityResource {

    @Inject
    DoctorService doctorService;

    /**
     * Get all availability slots for a specific doctor
     */
    @GET
    @Path("/{id}/availability")
    @Operation(
            summary = "Get doctor availability",
            description = "Retrieve all availability slots for a specific doctor"
    )
    @APIResponse(
            responseCode = "200",
            description = "Availability slots retrieved successfully",
            content = @Content(schema = @Schema(implementation = AvailabilityDTO.class))
    )
    @APIResponse(responseCode = "404", description = "Doctor not found")
    public Response getDoctorAvailability(@PathParam("id") Long doctorId) {
        List<AvailabilityDTO> availability = doctorService.getDoctorAvailability(doctorId);
        return Response.ok(availability).build();
    }

    /**
     * Add new availability slot for a doctor
     */
    @POST
    @Path("/{id}/availability")
    @Operation(
            summary = "Add availability slot",
            description = "Add a new availability slot for a doctor"
    )
    @APIResponse(
            responseCode = "201",
            description = "Availability slot created successfully",
            content = @Content(schema = @Schema(implementation = AvailabilityDTO.class))
    )
    @APIResponse(responseCode = "400", description = "Invalid input or time conflict")
    @APIResponse(responseCode = "404", description = "Doctor not found")
    public Response addDoctorAvailability(
            @PathParam("id") Long doctorId,
            @Valid CreateAvailabilityRequest request) {

        AvailabilityDTO availability = doctorService.addDoctorAvailability(doctorId, request);
        return Response.status(Response.Status.CREATED).entity(availability).build();
    }

    /**
     * Update existing availability slot
     */
    @PUT
    @Path("/availability/{availabilityId}")
    @Operation(
            summary = "Update availability slot",
            description = "Update an existing availability slot"
    )
    @APIResponse(
            responseCode = "200",
            description = "Availability slot updated successfully",
            content = @Content(schema = @Schema(implementation = AvailabilityDTO.class))
    )
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "404", description = "Availability slot not found")
    public Response updateAvailability(
            @PathParam("availabilityId") Long availabilityId,
            @Valid CreateAvailabilityRequest request) {

        AvailabilityDTO availability = doctorService.updateAvailability(availabilityId, request);
        return Response.ok(availability).build();
    }

    /**
     * Delete availability slot
     */
    @DELETE
    @Path("/availability/{availabilityId}")
    @Operation(
            summary = "Delete availability slot",
            description = "Remove an availability slot"
    )
    @APIResponse(responseCode = "204", description = "Availability slot deleted successfully")
    @APIResponse(responseCode = "404", description = "Availability slot not found")
    public Response deleteAvailability(@PathParam("availabilityId") Long availabilityId) {
        doctorService.deleteAvailability(availabilityId);
        return Response.noContent().build();
    }
}












