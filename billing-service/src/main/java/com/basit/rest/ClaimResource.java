package com.basit.rest;

import com.basit.dto.request.ApproveClaimRequest;
import com.basit.dto.request.SubmitClaimRequest;
import com.basit.dto.response.ClaimResponse;
import com.basit.entity.InsuranceClaim;
import com.basit.mapper.ClaimMapper;
import com.basit.service.InsuranceClaimService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/billing/claims")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClaimResource {

    @Inject
    InsuranceClaimService claimService;

    @Inject
    ClaimMapper claimMapper;

    @POST
    public Response createClaim(@Valid SubmitClaimRequest request) {
        InsuranceClaim claim = claimMapper.toEntity(request);
        InsuranceClaim created = claimService.createClaim(claim);
        ClaimResponse response = claimMapper.toResponse(created);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{id}")
    public Response getClaimById(@PathParam("id") Long id) {
        InsuranceClaim claim = claimService.getClaimById(id);
        ClaimResponse response = claimMapper.toResponse(claim);
        return Response.ok(response).build();
    }

    @GET
    @Path("/number/{claimNumber}")
    public Response getClaimByNumber(@PathParam("claimNumber") String claimNumber) {
        InsuranceClaim claim = claimService.getClaimByClaimNumber(claimNumber);
        ClaimResponse response = claimMapper.toResponse(claim);
        return Response.ok(response).build();
    }

    @GET
    @Path("/patient/{patientId}")
    public Response getClaimsByPatient(@PathParam("patientId") Long patientId) {
        List<InsuranceClaim> claims = claimService.getClaimsByPatientId(patientId);
        List<ClaimResponse> responses = claims.stream()
                .map(claimMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @POST
    @Path("/{id}/submit")
    public Response submitClaim(@PathParam("id") Long id) {
        InsuranceClaim claim = claimService.submitClaim(id);
        ClaimResponse response = claimMapper.toResponse(claim);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/approve")
    public Response approveClaim(@PathParam("id") Long id, @Valid ApproveClaimRequest request) {
        InsuranceClaim claim = claimService.approveClaim(id, request.approvedAmount);
        ClaimResponse response = claimMapper.toResponse(claim);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/deny")
    public Response denyClaim(@PathParam("id") Long id, @QueryParam("reason") String reason) {
        InsuranceClaim claim = claimService.denyClaim(id, reason);
        ClaimResponse response = claimMapper.toResponse(claim);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/appeal")
    public Response appealClaim(@PathParam("id") Long id) {
        InsuranceClaim claim = claimService.appealClaim(id);
        ClaimResponse response = claimMapper.toResponse(claim);
        return Response.ok(response).build();
    }

    @GET
    @Path("/pending")
    public Response getPendingClaims() {
        List<InsuranceClaim> claims = claimService.getPendingClaims();
        List<ClaimResponse> responses = claims.stream()
                .map(claimMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @GET
    @Path("/unpaid")
    public Response getUnpaidClaims() {
        List<InsuranceClaim> claims = claimService.getUnpaidClaims();
        List<ClaimResponse> responses = claims.stream()
                .map(claimMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @GET
    @Path("/appealed")
    public Response getAppealedClaims() {
        List<InsuranceClaim> claims = claimService.getAppealedClaims();
        List<ClaimResponse> responses = claims.stream()
                .map(claimMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }
}

