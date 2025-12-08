package com.basit.mapper;

import com.basit.dto.request.InsuranceClaimRequest;
import com.basit.dto.response.InsuranceClaimResponse;
import com.basit.entity.InsuranceClaim;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InsuranceClaimMapper {

    public InsuranceClaim toEntity(InsuranceClaimRequest request) {
        InsuranceClaim claim = new InsuranceClaim();
        claim.invoiceId = request.invoiceId;
        claim.insuranceProvider = request.insuranceProvider;
        claim.policyNumber = request.policyNumber;
        claim.claimedAmount = request.claimedAmount;
        claim.notes = request.notes;
        return claim;
    }

    public InsuranceClaimResponse toResponse(InsuranceClaim claim) {
        InsuranceClaimResponse response = new InsuranceClaimResponse();
        response.id = claim.id;
        response.invoiceId = claim.invoiceId;
        response.patientId = claim.patientId;
        response.insuranceProvider = claim.insuranceProvider;
        response.policyNumber = claim.policyNumber;
        response.claimedAmount = claim.claimedAmount;
        response.approvedAmount = claim.approvedAmount;
        response.status = claim.status;
        response.claimNumber = claim.claimNumber;
        response.submissionDate = claim.submissionDate;
        response.approvalDate = claim.approvalDate;
        response.notes = claim.notes;
        return response;
    }
}
