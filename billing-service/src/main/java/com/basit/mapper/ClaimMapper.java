package com.basit.mapper;


import com.basit.dto.request.SubmitClaimRequest;
import com.basit.dto.response.ClaimResponse;
import com.basit.entity.InsuranceClaim;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class ClaimMapper {

    public InsuranceClaim toEntity(SubmitClaimRequest request) {
        InsuranceClaim claim = new InsuranceClaim();
        claim.invoiceId = request.invoiceId;
        claim.patientId = request.patientId;
        claim.claimNumber = request.claimNumber;
        claim.insuranceProvider = request.insuranceProvider;
        claim.policyNumber = request.policyNumber;
        claim.groupNumber = request.groupNumber;
        claim.claimAmount = request.claimAmount;
        claim.diagnosisCodes = request.diagnosisCodes;
        claim.procedureCodes = request.procedureCodes;
        claim.providerNPI = request.providerNPI;
        claim.facilityCode = request.facilityCode;
        claim.notes = request.notes;
        return claim;
    }

    public ClaimResponse toResponse(InsuranceClaim claim) {
        ClaimResponse response = new ClaimResponse();
        response.id = claim.id;
        response.invoiceId = claim.invoiceId;
        response.patientId = claim.patientId;
        response.claimNumber = claim.claimNumber;
        response.insuranceProvider = claim.insuranceProvider;
        response.policyNumber = claim.policyNumber;
        response.groupNumber = claim.groupNumber;
        response.claimAmount = claim.claimAmount;
        response.approvedAmount = claim.approvedAmount;
        response.paidAmount = claim.paidAmount;
        response.patientResponsibility = claim.patientResponsibility;
        response.status = claim.status;
        response.submissionDate = claim.submissionDate;
        response.processedDate = claim.processedDate;
        response.paidDate = claim.paidDate;
        response.denialReason = claim.denialReason;
        response.diagnosisCodes = claim.diagnosisCodes;
        response.procedureCodes = claim.procedureCodes;
        response.providerNPI = claim.providerNPI;
        response.facilityCode = claim.facilityCode;
        response.notes = claim.notes;
        response.externalClaimId = claim.externalClaimId;
        response.appealCount = claim.appealCount;
        response.lastAppealDate = claim.lastAppealDate;
        response.createdAt = claim.createdAt;
        response.updatedAt = claim.updatedAt;
        return response;
    }
}

