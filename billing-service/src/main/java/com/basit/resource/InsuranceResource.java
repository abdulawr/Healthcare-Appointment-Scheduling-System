package com.basit.resource;


import com.basit.dto.request.InsuranceClaimRequest;
import com.basit.dto.response.InsuranceClaimResponse;
import com.basit.constant.ClaimStatus;
import com.basit.service.InsuranceService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;

@Path("/api/billing/insurance")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Insurance Management", description = "Insurance claim operations")
public class InsuranceResource {

    @Inject
    InsuranceService insuranceService;

    @POST
    @Path("/claim")
    @Operation(summary = "Submit insurance claim", description = "Submit a new insurance claim for an invoice")
    @APIResponse(responseCode = "201", description = "Claim submitted successfully")
    @APIResponse(responseCode = "400", description = "Invalid request")
    @APIResponse(responseCode = "404", description = "Invoice not found")
    public Response submitClaim(@Valid InsuranceClaimRequest request) {
        InsuranceClaimResponse response = insuranceService.submitClaim(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("/verify")
    @Operation(summary = "Verify insurance coverage", description = "Verify insurance coverage for a claim")
    @APIResponse(responseCode = "200", description = "Coverage verified")
    @APIResponse(responseCode = "400", description = "Invalid request")
    public Response verifyCoverage(@Valid InsuranceClaimRequest request) {
        InsuranceClaimResponse response = insuranceService.verifyCoverage(request);
        return Response.ok(response).build();
    }

    @POST
    @Path("/claim/{id}/approve")
    @Operation(summary = "Approve insurance claim", description = "Approve an insurance claim with approved amount")
    @APIResponse(responseCode = "200", description = "Claim approved")
    @APIResponse(responseCode = "400", description = "Invalid request")
    @APIResponse(responseCode = "404", description = "Claim not found")
    public Response approveClaim(
            @Parameter(description = "Claim ID", required = true)
            @PathParam("id") Long claimId,
            @Parameter(description = "Approved amount", required = true)
            @QueryParam("approvedAmount") BigDecimal approvedAmount) {
        InsuranceClaimResponse response = insuranceService.approveClaim(claimId, approvedAmount);
        return Response.ok(response).build();
    }

    @POST
    @Path("/claim/{id}/reject")
    @Operation(summary = "Reject insurance claim", description = "Reject an insurance claim with reason")
    @APIResponse(responseCode = "200", description = "Claim rejected")
    @APIResponse(responseCode = "400", description = "Invalid request")
    @APIResponse(responseCode = "404", description = "Claim not found")
    public Response rejectClaim(
            @Parameter(description = "Claim ID", required = true)
            @PathParam("id") Long claimId,
            @Parameter(description = "Rejection reason", required = true)
            @QueryParam("reason") String reason) {
        InsuranceClaimResponse response = insuranceService.rejectClaim(claimId, reason);
        return Response.ok(response).build();
    }

    @GET
    @Path("/claim/{id}")
    @Operation(summary = "Get claim details", description = "Retrieve claim by ID")
    @APIResponse(responseCode = "200", description = "Claim found")
    @APIResponse(responseCode = "404", description = "Claim not found")
    public Response getClaim(
            @Parameter(description = "Claim ID", required = true)
            @PathParam("id") Long id) {
        InsuranceClaimResponse response = insuranceService.getClaim(id);
        return Response.ok(response).build();
    }

    @GET
    @Path("/claim/number/{claimNumber}")
    @Operation(summary = "Get claim by number", description = "Retrieve claim by claim number")
    @APIResponse(responseCode = "200", description = "Claim found")
    @APIResponse(responseCode = "404", description = "Claim not found")
    public Response getClaimByNumber(
            @Parameter(description = "Claim Number", required = true)
            @PathParam("claimNumber") String claimNumber) {
        InsuranceClaimResponse response = insuranceService.getClaimByNumber(claimNumber);
        return Response.ok(response).build();
    }

    @GET
    @Path("/claims/invoice/{invoiceId}")
    @Operation(summary = "Get claims by invoice", description = "Get all claims for a specific invoice")
    @APIResponse(responseCode = "200", description = "Claims retrieved successfully")
    public Response getClaimsByInvoice(
            @Parameter(description = "Invoice ID", required = true)
            @PathParam("invoiceId") Long invoiceId) {
        List<InsuranceClaimResponse> responses = insuranceService.getClaimsByInvoice(invoiceId);
        return Response.ok(responses).build();
    }

    @GET
    @Path("/claims/patient/{patientId}")
    @Operation(summary = "Get claims by patient", description = "Get all claims for a specific patient")
    @APIResponse(responseCode = "200", description = "Claims retrieved successfully")
    public Response getClaimsByPatient(
            @Parameter(description = "Patient ID", required = true)
            @PathParam("patientId") Long patientId) {
        List<InsuranceClaimResponse> responses = insuranceService.getClaimsByPatient(patientId);
        return Response.ok(responses).build();
    }

    @GET
    @Path("/claims/provider/{provider}")
    @Operation(summary = "Get claims by provider", description = "Get all claims for a specific insurance provider")
    @APIResponse(responseCode = "200", description = "Claims retrieved successfully")
    public Response getClaimsByProvider(
            @Parameter(description = "Insurance Provider", required = true)
            @PathParam("provider") String provider) {
        List<InsuranceClaimResponse> responses = insuranceService.getClaimsByProvider(provider);
        return Response.ok(responses).build();
    }

    @GET
    @Path("/claims/status/{status}")
    @Operation(summary = "Get claims by status", description = "Get all claims with a specific status")
    @APIResponse(responseCode = "200", description = "Claims retrieved successfully")
    public Response getClaimsByStatus(
            @Parameter(description = "Claim Status", required = true)
            @PathParam("status") ClaimStatus status) {
        List<InsuranceClaimResponse> responses = insuranceService.getClaimsByStatus(status);
        return Response.ok(responses).build();
    }
}
