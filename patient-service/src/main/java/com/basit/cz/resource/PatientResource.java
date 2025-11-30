package com.basit.cz.resource;

import com.basit.cz.dto.CommunicationPreferenceDTO;
import com.basit.cz.dto.InsuranceDTO;
import com.basit.cz.dto.MedicalRecordDTO;
import com.basit.cz.dto.PatientDTO;
import com.basit.cz.entity.CommunicationPreference;
import com.basit.cz.entity.Insurance;
import com.basit.cz.entity.MedicalRecord;
import com.basit.cz.entity.Patient;
import com.basit.cz.service.PatientService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API endpoints for Patient operations.
 *
 * Base path: /api/patients
 *
 * All endpoints are documented with OpenAPI annotations
 * for automatic Swagger UI generation.
 */
@Path("/api/patients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient", description = "Patient management operations")
public class PatientResource {

    private static final Logger LOG = Logger.getLogger(PatientResource.class);

    @Inject
    PatientService patientService;

    /**
     * ENDPOINT 1: Register a new patient
     * POST /api/patients/register
     */
    @POST
    @Path("/register")
    @Operation(
            summary = "Register a new patient",
            description = "Creates a new patient record with the provided information"
    )
    @APIResponse(
            responseCode = "201",
            description = "Patient created successfully",
            content = @Content(schema = @Schema(implementation = PatientDTO.Response.class))
    )
    @APIResponse(
            responseCode = "409",
            description = "Email already exists"
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid request data"
    )
    public Response registerPatient(@Valid PatientDTO.RegistrationRequest request) {
        LOG.infof("POST /api/patients/register - %s", request);

        PatientDTO.Response response = patientService.registerPatient(request);

        return Response
                .status(Response.Status.CREATED)
                .entity(response)
                .build();
    }

    /**
     * ENDPOINT 2: Get patient by ID
     * GET /api/patients/{id}
     */
    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get patient by ID",
            description = "Retrieves a patient's information by their ID"
    )
    @APIResponse(
            responseCode = "200",
            description = "Patient found",
            content = @Content(schema = @Schema(implementation = PatientDTO.Response.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Patient not found"
    )
    public Response getPatient(
            @Parameter(description = "Patient ID", required = true)
            @PathParam("id") Long id) {
        LOG.infof("GET /api/patients/%d", id);

        PatientDTO.Response response = patientService.getPatient(id);

        return Response.ok(response).build();
    }

    /**
     * ENDPOINT 3: Update patient
     * PUT /api/patients/{id}
     */
    @PUT
    @Path("/{id}")
    @Operation(
            summary = "Update patient information",
            description = "Updates an existing patient's information"
    )
    @APIResponse(
            responseCode = "200",
            description = "Patient updated successfully",
            content = @Content(schema = @Schema(implementation = PatientDTO.Response.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Patient not found"
    )
    @APIResponse(
            responseCode = "409",
            description = "New email already exists"
    )
    public Response updatePatient(
            @Parameter(description = "Patient ID", required = true)
            @PathParam("id") Long id,
            @Valid PatientDTO.UpdateRequest request) {
        LOG.infof("PUT /api/patients/%d - %s", id, request);

        PatientDTO.Response response = patientService.updatePatient(id, request);

        return Response.ok(response).build();
    }

    /**
     * ENDPOINT 4: Deactivate patient (soft delete)
     * DELETE /api/patients/{id}
     */
    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Deactivate patient",
            description = "Soft deletes a patient (marks as inactive)"
    )
    @APIResponse(
            responseCode = "204",
            description = "Patient deactivated successfully"
    )
    @APIResponse(
            responseCode = "404",
            description = "Patient not found"
    )
    public Response deactivatePatient(
            @Parameter(description = "Patient ID", required = true)
            @PathParam("id") Long id) {
        LOG.infof("DELETE /api/patients/%d", id);

        patientService.deactivatePatient(id);

        return Response.noContent().build();
    }

    /**
     * ENDPOINT 5: Search patients by name
     * GET /api/patients/search?q={searchTerm}
     */
    @GET
    @Path("/search")
    @Operation(
            summary = "Search patients by name",
            description = "Searches for patients by first or last name (case-insensitive)"
    )
    @APIResponse(
            responseCode = "200",
            description = "Search results (may be empty)",
            content = @Content(schema = @Schema(implementation = PatientDTO.Response.class))
    )
    public Response searchPatients(
            @Parameter(description = "Search term for patient name", required = true)
            @QueryParam("q") String searchTerm) {
        LOG.infof("GET /api/patients/search?q=%s", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Search term 'q' is required\"}")
                    .build();
        }

        List<PatientDTO.Response> results = patientService.searchPatients(searchTerm);

        return Response.ok(results).build();
    }

    /**
     * ENDPOINT 6: Get all active patients
     * GET /api/patients
     */
    @GET
    @Operation(
            summary = "Get all active patients",
            description = "Retrieves a list of all active patients"
    )
    @APIResponse(
            responseCode = "200",
            description = "List of active patients",
            content = @Content(schema = @Schema(implementation = PatientDTO.Response.class))
    )
    public Response getAllActivePatients() {
        LOG.info("GET /api/patients");

        List<PatientDTO.Response> patients = patientService.getAllActivePatients();

        return Response.ok(patients).build();
    }

    /**
     * ENDPOINT 7: Get active patient count
     * GET /api/patients/count
     */
    @GET
    @Path("/count")
    @Operation(
            summary = "Get active patient count",
            description = "Returns the total number of active patients"
    )
    @APIResponse(
            responseCode = "200",
            description = "Count of active patients"
    )
    public Response getActivePatientCount() {
        LOG.info("GET /api/patients/count");

        long count = patientService.getActivePatientCount();

        return Response.ok("{\"count\": " + count + "}").build();
    }

    /**
     * ENDPOINT 8: Health check
     * GET /api/patients/health
     */
    @GET
    @Path("/health")
    @Operation(
            summary = "Health check",
            description = "Check if the patient service is running"
    )
    @APIResponse(
            responseCode = "200",
            description = "Service is healthy"
    )
    public Response healthCheck() {
        return Response.ok("{\"status\": \"UP\", \"service\": \"patient-service\"}").build();
    }

    /**
     * ENDPOINT 9: Get patient insurance
     * GET /api/patients/{id}/insurance
     */
    @GET
    @Path("/{id}/insurance")
    @Operation(
            summary = "Get patient insurance information",
            description = "Retrieves insurance details for a specific patient"
    )
    @APIResponse(
            responseCode = "200",
            description = "Insurance found",
            content = @Content(schema = @Schema(implementation = InsuranceDTO.Response.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Patient or insurance not found"
    )
    public Response getPatientInsurance(
            @Parameter(description = "Patient ID", required = true)
            @PathParam("id") Long patientId) {
        LOG.infof("GET /api/patients/%d/insurance", patientId);

        Patient patient = Patient.findById(patientId);
        if (patient == null || !patient.isActive) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Patient not found\"}")
                    .build();
        }

        Insurance insurance = Insurance.find("patient.id", patientId).firstResult();
        if (insurance == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Insurance not found for patient\"}")
                    .build();
        }

        return Response.ok(InsuranceDTO.Response.fromEntity(insurance)).build();
    }

    /**
     * ENDPOINT 10: Update patient insurance
     * PUT /api/patients/{id}/insurance
     */
    @PUT
    @Path("/{id}/insurance")
    @Transactional
    @Operation(
            summary = "Update patient insurance",
            description = "Creates or updates insurance information for a patient"
    )
    @APIResponse(
            responseCode = "200",
            description = "Insurance updated",
            content = @Content(schema = @Schema(implementation = InsuranceDTO.Response.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Patient not found"
    )
    public Response updatePatientInsurance(
            @Parameter(description = "Patient ID", required = true)
            @PathParam("id") Long patientId,
            @Valid InsuranceDTO.Request request) {
        LOG.infof("PUT /api/patients/%d/insurance", patientId);

        Patient patient = Patient.findById(patientId);
        if (patient == null || !patient.isActive) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Patient not found\"}")
                    .build();
        }

        Insurance insurance = Insurance.find("patient.id", patientId).firstResult();
        if (insurance == null) {
            insurance = new Insurance();
            insurance.patient = patient;
        }

        insurance.providerName = request.providerName;
        insurance.policyNumber = request.policyNumber;
        insurance.groupNumber = request.groupNumber;
        insurance.policyHolderName = request.policyHolderName;
        insurance.policyHolderRelationship = request.policyHolderRelationship;
        insurance.coverageStartDate = request.coverageStartDate;
        insurance.coverageEndDate = request.coverageEndDate;
        insurance.copayAmount = request.copayAmount;
        insurance.deductibleAmount = request.deductibleAmount;

        insurance.persist();

        return Response.ok(InsuranceDTO.Response.fromEntity(insurance)).build();
    }

    /**
     * ENDPOINT 11: Get patient medical history
     * GET /api/patients/{id}/medical-history
     */
    @GET
    @Path("/{id}/medical-history")
    @Operation(
            summary = "Get patient medical history",
            description = "Retrieves all medical records for a specific patient"
    )
    @APIResponse(
            responseCode = "200",
            description = "Medical records found",
            content = @Content(schema = @Schema(implementation = MedicalRecordDTO.Response.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Patient not found"
    )
    public Response getPatientMedicalHistory(
            @Parameter(description = "Patient ID", required = true)
            @PathParam("id") Long patientId) {
        LOG.infof("GET /api/patients/%d/medical-history", patientId);

        Patient patient = Patient.findById(patientId);
        if (patient == null || !patient.isActive) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Patient not found\"}")
                    .build();
        }

        List<MedicalRecord> records = MedicalRecord.list("patient.id", patientId);
        List<MedicalRecordDTO.Response> response = records.stream()
                .map(MedicalRecordDTO.Response::fromEntity)
                .collect(Collectors.toList());

        return Response.ok(response).build();
    }

    /**
     * ENDPOINT 12: Add medical record
     * POST /api/patients/{id}/medical-history
     */
    @POST
    @Path("/{id}/medical-history")
    @Transactional
    @Operation(
            summary = "Add medical record",
            description = "Adds a new medical record for a patient"
    )
    @APIResponse(
            responseCode = "201",
            description = "Medical record created",
            content = @Content(schema = @Schema(implementation = MedicalRecordDTO.Response.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Patient not found"
    )
    public Response addMedicalRecord(
            @Parameter(description = "Patient ID", required = true)
            @PathParam("id") Long patientId,
            @Valid MedicalRecordDTO.Request request) {
        LOG.infof("POST /api/patients/%d/medical-history", patientId);

        Patient patient = Patient.findById(patientId);
        if (patient == null || !patient.isActive) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Patient not found\"}")
                    .build();
        }

        MedicalRecord record = new MedicalRecord();
        record.patient = patient;
        record.recordType = request.recordType;
        record.recordDate = request.recordDate;
        record.description = request.description;
        record.diagnosis = request.diagnosis;
        record.prescription = request.prescription;
        record.doctorName = request.doctorName;
        record.hospitalName = request.hospitalName;
        record.notes = request.notes;

        record.persist();

        return Response.status(Response.Status.CREATED)
                .entity(MedicalRecordDTO.Response.fromEntity(record))
                .build();
    }

    /**
     * ENDPOINT 13: Get communication preferences
     * GET /api/patients/{id}/preferences
     */
    @GET
    @Path("/{id}/preferences")
    @Operation(
            summary = "Get communication preferences",
            description = "Retrieves communication preferences for a patient"
    )
    @APIResponse(
            responseCode = "200",
            description = "Preferences found",
            content = @Content(schema = @Schema(implementation = CommunicationPreferenceDTO.Response.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Patient not found"
    )
    public Response getPatientPreferences(
            @Parameter(description = "Patient ID", required = true)
            @PathParam("id") Long patientId) {
        LOG.infof("GET /api/patients/%d/preferences", patientId);

        Patient patient = Patient.findById(patientId);
        if (patient == null || !patient.isActive) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Patient not found\"}")
                    .build();
        }

        CommunicationPreference pref = CommunicationPreference.find("patient.id", patientId).firstResult();
        if (pref == null) {
            // Return default preferences if none exist
            pref = new CommunicationPreference();
            pref.patient = patient;
        }

        return Response.ok(CommunicationPreferenceDTO.Response.fromEntity(pref)).build();
    }

    /**
     * ENDPOINT 14: Update communication preferences
     * PUT /api/patients/{id}/preferences
     */
    @PUT
    @Path("/{id}/preferences")
    @Transactional
    @Operation(
            summary = "Update communication preferences",
            description = "Creates or updates communication preferences for a patient"
    )
    @APIResponse(
            responseCode = "200",
            description = "Preferences updated",
            content = @Content(schema = @Schema(implementation = CommunicationPreferenceDTO.Response.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Patient not found"
    )
    public Response updatePatientPreferences(
            @Parameter(description = "Patient ID", required = true)
            @PathParam("id") Long patientId,
            @Valid CommunicationPreferenceDTO.Request request) {
        LOG.infof("PUT /api/patients/%d/preferences", patientId);

        Patient patient = Patient.findById(patientId);
        if (patient == null || !patient.isActive) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Patient not found\"}")
                    .build();
        }

        CommunicationPreference pref = CommunicationPreference.find("patient.id", patientId).firstResult();
        if (pref == null) {
            pref = new CommunicationPreference();
            pref.patient = patient;
        }

        if (request.emailNotifications != null) pref.emailNotifications = request.emailNotifications;
        if (request.smsNotifications != null) pref.smsNotifications = request.smsNotifications;
        if (request.pushNotifications != null) pref.pushNotifications = request.pushNotifications;
        if (request.appointmentReminders != null) pref.appointmentReminders = request.appointmentReminders;
        if (request.marketingCommunications != null) pref.marketingCommunications = request.marketingCommunications;
        if (request.preferredContactMethod != null) pref.preferredContactMethod = request.preferredContactMethod;
        if (request.preferredLanguage != null) pref.preferredLanguage = request.preferredLanguage;
        if (request.reminderHoursBefore != null) pref.reminderHoursBefore = request.reminderHoursBefore;

        pref.persist();

        return Response.ok(CommunicationPreferenceDTO.Response.fromEntity(pref)).build();
    }
}


