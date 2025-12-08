package com.basit.service;

import com.basit.dto.request.InsuranceClaimRequest;
import com.basit.dto.response.InsuranceClaimResponse;
import com.basit.entity.InsuranceClaim;
import com.basit.entity.Invoice;
import com.basit.constant.ClaimStatus;
import com.basit.event.BillingEventProducer;
import com.basit.event.InsuranceClaimSubmittedEvent;
import com.basit.mapper.InsuranceClaimMapper;
import com.basit.repository.InsuranceClaimRepository;
import com.basit.repository.InvoiceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class InsuranceService {

    @Inject
    InsuranceClaimRepository claimRepository;

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    InsuranceClaimMapper claimMapper;

    @Inject
    BillingEventProducer eventProducer;

    @Transactional
    public InsuranceClaimResponse submitClaim(InsuranceClaimRequest request) {
        // Validate invoice exists
        Invoice invoice = invoiceRepository.findByIdOptional(request.invoiceId)
                .orElseThrow(() -> new NotFoundException(
                        "Invoice not found with id: " + request.invoiceId));

        // Validate claim amount doesn't exceed invoice total
        if (request.claimedAmount.compareTo(invoice.total) > 0) {
            throw new IllegalArgumentException(
                    "Claimed amount cannot exceed invoice total");
        }

        // Check if claim already exists for this invoice
        List<InsuranceClaim> existingClaims = claimRepository.findByInvoiceId(request.invoiceId);
        if (!existingClaims.isEmpty()) {
            throw new IllegalStateException(
                    "Insurance claim already exists for invoice: " + request.invoiceId);
        }

        // Create claim
        InsuranceClaim claim = claimMapper.toEntity(request);
        claim.patientId = invoice.patientId;
        claim.claimNumber = generateClaimNumber();
        claim.status = ClaimStatus.SUBMITTED;

        claimRepository.persist(claim);

        // ✅ ADD THIS: Publish event
        InsuranceClaimSubmittedEvent event = new InsuranceClaimSubmittedEvent();
        event.claimId = claim.id;
        event.invoiceId = invoice.id;
        event.patientId = invoice.patientId;
        event.insuranceProvider = claim.insuranceProvider;
        event.policyNumber = claim.policyNumber;
        event.claimAmount = request.claimedAmount;
        event.status = claim.status;
        event.submittedDate = null;
        event.submittedBy = null;

        eventProducer.publishInsuranceClaimSubmitted(event);
        // ✅ END OF ADDITION

        return claimMapper.toResponse(claim);
    }

    @Transactional
    public InsuranceClaimResponse verifyCoverage(InsuranceClaimRequest request) {
        // Simulate insurance verification
        // In production, this would call insurance provider API

        InsuranceClaim claim = claimMapper.toEntity(request);
        claim.claimNumber = "VERIFY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        claim.status = ClaimStatus.UNDER_REVIEW;

        // Simulate coverage verification (70% approval rate)
        boolean isCovered = Math.random() > 0.3;

        if (isCovered) {
            claim.approvedAmount = request.claimedAmount;
        } else {
            claim.approvedAmount = BigDecimal.ZERO;
        }

        return claimMapper.toResponse(claim);
    }

    @Transactional
    public InsuranceClaimResponse approveClaim(Long claimId, BigDecimal approvedAmount) {
        InsuranceClaim claim = claimRepository.findByIdOptional(claimId)
                .orElseThrow(() -> new NotFoundException(
                        "Claim not found with id: " + claimId));

        if (claim.status != ClaimStatus.SUBMITTED &&
                claim.status != ClaimStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "Can only approve submitted or under-review claims");
        }

        claim.approvedAmount = approvedAmount;
        claim.status = ClaimStatus.APPROVED;
        claim.approvalDate = LocalDateTime.now();

        // Update invoice with insurance payment
        Invoice invoice = invoiceRepository.findByIdOptional(claim.invoiceId)
                .orElseThrow(() -> new NotFoundException(
                        "Invoice not found with id: " + claim.invoiceId));

        invoice.amountPaid = invoice.amountPaid.add(approvedAmount);
        invoice.calculateBalance();
        invoiceRepository.persist(invoice);

        claimRepository.persist(claim);
        return claimMapper.toResponse(claim);
    }

    @Transactional
    public InsuranceClaimResponse rejectClaim(Long claimId, String reason) {
        InsuranceClaim claim = claimRepository.findByIdOptional(claimId)
                .orElseThrow(() -> new NotFoundException(
                        "Claim not found with id: " + claimId));

        if (claim.status != ClaimStatus.SUBMITTED &&
                claim.status != ClaimStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "Can only reject submitted or under-review claims");
        }

        claim.status = ClaimStatus.REJECTED;
        claim.notes = reason;
        claim.approvalDate = LocalDateTime.now();

        claimRepository.persist(claim);
        return claimMapper.toResponse(claim);
    }

    private String generateClaimNumber() {
        return "CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public InsuranceClaimResponse getClaim(Long id) {
        InsuranceClaim claim = claimRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException(
                        "Claim not found with id: " + id));
        return claimMapper.toResponse(claim);
    }

    public InsuranceClaimResponse getClaimByNumber(String claimNumber) {
        InsuranceClaim claim = claimRepository.findByClaimNumber(claimNumber);
        if (claim == null) {
            throw new NotFoundException(
                    "Claim not found with number: " + claimNumber);
        }
        return claimMapper.toResponse(claim);
    }

    public List<InsuranceClaimResponse> getClaimsByInvoice(Long invoiceId) {
        return claimRepository.findByInvoiceId(invoiceId).stream()
                .map(claimMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<InsuranceClaimResponse> getClaimsByPatient(Long patientId) {
        return claimRepository.findByPatientId(patientId).stream()
                .map(claimMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<InsuranceClaimResponse> getClaimsByProvider(String provider) {
        return claimRepository.findByInsuranceProvider(provider).stream()
                .map(claimMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<InsuranceClaimResponse> getClaimsByStatus(ClaimStatus status) {
        return claimRepository.findByStatus(status).stream()
                .map(claimMapper::toResponse)
                .collect(Collectors.toList());
    }
}
