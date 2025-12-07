package com.basit.service;

import com.basit.constant.ClaimStatus;
import com.basit.entity.InsuranceClaim;
import com.basit.repository.InsuranceClaimRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class InsuranceClaimService {

    @Inject
    InsuranceClaimRepository claimRepository;

    @Transactional
    public InsuranceClaim createClaim(InsuranceClaim claim) {
        claim.status = ClaimStatus.DRAFT;
        claim.appealCount = 0;
        claim.paidAmount = BigDecimal.ZERO;
        // onCreate() will be called automatically by @PrePersist
        claimRepository.persist(claim);
        return claim;
    }

    public InsuranceClaim getClaimById(Long id) {
        return claimRepository.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + id));
    }

    public InsuranceClaim getClaimByClaimNumber(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new RuntimeException("Claim not found with number: " + claimNumber));
    }

    public List<InsuranceClaim> getClaimsByPatientId(Long patientId) {
        return claimRepository.findByPatientId(patientId);
    }

    public List<InsuranceClaim> getClaimsByStatus(ClaimStatus status) {
        return claimRepository.findByStatus(status);
    }

    @Transactional
    public InsuranceClaim submitClaim(Long claimId) {
        InsuranceClaim claim = getClaimById(claimId);
        claim.submit();
        claim.submissionDate = LocalDate.now();
        return claim;
    }

    @Transactional
    public InsuranceClaim approveClaim(Long claimId, BigDecimal approvedAmount) {
        InsuranceClaim claim = getClaimById(claimId);
        claim.approve(approvedAmount);
        return claim;
    }

    @Transactional
    public InsuranceClaim denyClaim(Long claimId, String reason) {
        InsuranceClaim claim = getClaimById(claimId);
        claim.deny(reason);
        return claim;
    }

    @Transactional
    public InsuranceClaim recordPayment(Long claimId, BigDecimal amount) {
        InsuranceClaim claim = getClaimById(claimId);
        claim.recordPayment(amount);
        return claim;
    }

    @Transactional
    public InsuranceClaim appealClaim(Long claimId) {
        InsuranceClaim claim = getClaimById(claimId);
        claim.appeal();
        return claim;
    }

    public List<InsuranceClaim> getPendingClaims() {
        return claimRepository.findPendingClaims();
    }

    public List<InsuranceClaim> getUnpaidClaims() {
        return claimRepository.findUnpaidClaims();
    }

    public List<InsuranceClaim> getAppealedClaims() {
        return claimRepository.findAppealedClaims();
    }

    public BigDecimal calculateTotalClaimAmount(Long patientId) {
        BigDecimal total = claimRepository.calculateTotalClaimAmountForPatient(patientId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal calculatePatientResponsibility(Long patientId) {
        BigDecimal total = claimRepository.calculatePatientResponsibilityForPatient(patientId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<InsuranceClaim> getRecentlySubmitted(int days) {
        return claimRepository.findRecentlySubmitted(days);
    }

    public List<InsuranceClaim> getAllClaims() {
        return claimRepository.listAll();
    }

    public long countByStatus(ClaimStatus status) {
        return claimRepository.countByStatus(status);
    }
}