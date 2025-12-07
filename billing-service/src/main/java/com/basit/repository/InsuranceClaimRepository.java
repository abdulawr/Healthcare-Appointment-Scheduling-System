package com.basit.repository;

import com.basit.constant.ClaimStatus;
import com.basit.entity.InsuranceClaim;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for InsuranceClaim entity with custom query methods
 */
@ApplicationScoped
public class InsuranceClaimRepository implements PanacheRepository<InsuranceClaim> {

    /**
     * Find claim by claim number
     */
    public Optional<InsuranceClaim> findByClaimNumber(String claimNumber) {
        return find("claimNumber", claimNumber).firstResultOptional();
    }

    /**
     * Find claim by external claim ID
     */
    public Optional<InsuranceClaim> findByExternalClaimId(String externalClaimId) {
        return find("externalClaimId", externalClaimId).firstResultOptional();
    }

    /**
     * Find all claims for an invoice
     */
    public List<InsuranceClaim> findByInvoiceId(Long invoiceId) {
        return find("invoiceId", Sort.descending("submissionDate"), invoiceId).list();
    }

    /**
     * Find all claims for a patient
     */
    public List<InsuranceClaim> findByPatientId(Long patientId) {
        return find("patientId", Sort.descending("submissionDate"), patientId).list();
    }

    /**
     * Find all claims for a patient with pagination
     */
    public List<InsuranceClaim> findByPatientId(Long patientId, Page page) {
        return find("patientId", Sort.descending("submissionDate"), patientId)
                .page(page)
                .list();
    }

    /**
     * Find claims by status
     */
    public List<InsuranceClaim> findByStatus(ClaimStatus status) {
        return find("status", Sort.descending("submissionDate"), status).list();
    }

    /**
     * Find claims by status with pagination
     */
    public List<InsuranceClaim> findByStatus(ClaimStatus status, Page page) {
        return find("status", Sort.descending("submissionDate"), status)
                .page(page)
                .list();
    }

    /**
     * Find claims by insurance provider
     */
    public List<InsuranceClaim> findByInsuranceProvider(String provider) {
        return find("insuranceProvider", Sort.descending("submissionDate"), provider).list();
    }

    /**
     * Find claims by policy number
     */
    public List<InsuranceClaim> findByPolicyNumber(String policyNumber) {
        return find("policyNumber", Sort.descending("submissionDate"), policyNumber).list();
    }

    /**
     * Find pending claims (submitted but not processed)
     */
    public List<InsuranceClaim> findPendingClaims() {
        return find("status in (?1, ?2, ?3)", Sort.ascending("submissionDate"),
                ClaimStatus.SUBMITTED, ClaimStatus.IN_REVIEW, ClaimStatus.INFO_REQUESTED)
                .list();
    }

    /**
     * Find approved claims
     */
    public List<InsuranceClaim> findApprovedClaims() {
        return find("status in (?1, ?2)", Sort.descending("processedDate"),
                ClaimStatus.APPROVED, ClaimStatus.PARTIALLY_APPROVED)
                .list();
    }

    /**
     * Find denied claims
     */
    public List<InsuranceClaim> findDeniedClaims() {
        return find("status = ?1", Sort.descending("processedDate"), ClaimStatus.DENIED).list();
    }

    /**
     * Find unpaid claims (approved but not paid in full)
     */
    public List<InsuranceClaim> findUnpaidClaims() {
        return find("status in (?1, ?2) and paidAmount < approvedAmount",
                Sort.descending("processedDate"),
                ClaimStatus.APPROVED, ClaimStatus.PARTIALLY_APPROVED)
                .list();
    }

    /**
     * Find claims submitted within date range
     */
    public List<InsuranceClaim> findSubmittedBetween(LocalDate startDate, LocalDate endDate) {
        return find("submissionDate between ?1 and ?2",
                Sort.descending("submissionDate"), startDate, endDate)
                .list();
    }

    /**
     * Find claims processed within date range
     */
    public List<InsuranceClaim> findProcessedBetween(LocalDate startDate, LocalDate endDate) {
        return find("processedDate between ?1 and ?2",
                Sort.descending("processedDate"), startDate, endDate)
                .list();
    }

    /**
     * Find appealed claims
     */
    public List<InsuranceClaim> findAppealedClaims() {
        return find("status = ?1", Sort.descending("lastAppealDate"), ClaimStatus.APPEALED).list();
    }

    /**
     * Find claims with multiple appeals
     */
    public List<InsuranceClaim> findWithMultipleAppeals() {
        return find("appealCount > 1", Sort.descending("lastAppealDate")).list();
    }

    /**
     * Calculate total claim amount for patient
     */
    public BigDecimal calculateTotalClaimAmountForPatient(Long patientId) {
        return find("select sum(c.claimAmount) from InsuranceClaim c where c.patientId = ?1",
                patientId)
                .project(BigDecimal.class)
                .firstResult();
    }

    /**
     * Calculate total approved amount for patient
     */
    public BigDecimal calculateTotalApprovedForPatient(Long patientId) {
        return find("select sum(c.approvedAmount) from InsuranceClaim c where c.patientId = ?1 and c.approvedAmount is not null",
                patientId)
                .project(BigDecimal.class)
                .firstResult();
    }

    /**
     * Calculate total paid amount for patient
     */
    public BigDecimal calculateTotalPaidForPatient(Long patientId) {
        return find("select sum(c.paidAmount) from InsuranceClaim c where c.patientId = ?1",
                patientId)
                .project(BigDecimal.class)
                .firstResult();
    }

    /**
     * Calculate patient responsibility for patient
     */
    public BigDecimal calculatePatientResponsibilityForPatient(Long patientId) {
        return find("select sum(c.patientResponsibility) from InsuranceClaim c where c.patientId = ?1 and c.patientResponsibility is not null",
                patientId)
                .project(BigDecimal.class)
                .firstResult();
    }

    /**
     * Count claims by status
     */
    public long countByStatus(ClaimStatus status) {
        return count("status", status);
    }

    /**
     * Count claims by insurance provider
     */
    public long countByInsuranceProvider(String provider) {
        return count("insuranceProvider", provider);
    }

    /**
     * Find claims requiring action (submitted long ago but not processed)
     */
    public List<InsuranceClaim> findRequiringAction(int daysOld) {
        LocalDate cutoff = LocalDate.now().minusDays(daysOld);
        return find("status in (?1, ?2) and submissionDate < ?3",
                Sort.ascending("submissionDate"),
                ClaimStatus.SUBMITTED, ClaimStatus.IN_REVIEW, cutoff)
                .list();
    }

    /**
     * Find recently submitted claims (last N days)
     */
    public List<InsuranceClaim> findRecentlySubmitted(int days) {
        LocalDate since = LocalDate.now().minusDays(days);
        return find("submissionDate >= ?1", Sort.descending("submissionDate"), since).list();
    }

    /**
     * Find claims by provider NPI
     */
    public List<InsuranceClaim> findByProviderNPI(String providerNPI) {
        return find("providerNPI", Sort.descending("submissionDate"), providerNPI).list();
    }
}