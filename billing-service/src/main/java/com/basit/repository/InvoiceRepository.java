package com.basit.repository;

import com.basit.constant.InvoiceStatus;
import com.basit.entity.Invoice;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Invoice entity with custom query methods
 */
@ApplicationScoped
public class InvoiceRepository implements PanacheRepository<Invoice> {

    /**
     * Find invoice by invoice number
     */
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return find("invoiceNumber", invoiceNumber).firstResultOptional();
    }

    /**
     * Find all invoices for a patient
     */
    public List<Invoice> findByPatientId(Long patientId) {
        return find("patientId", Sort.descending("issueDate"), patientId).list();
    }

    /**
     * Find all invoices for a patient with pagination
     */
    public List<Invoice> findByPatientId(Long patientId, Page page) {
        return find("patientId", Sort.descending("issueDate"), patientId)
                .page(page)
                .list();
    }

    /**
     * Find all invoices for an appointment
     */
    public List<Invoice> findByAppointmentId(Long appointmentId) {
        return find("appointmentId", appointmentId).list();
    }

    /**
     * Find all invoices by status
     */
    public List<Invoice> findByStatus(InvoiceStatus status) {
        return find("status", Sort.descending("issueDate"), status).list();
    }

    /**
     * Find all invoices by status with pagination
     */
    public List<Invoice> findByStatus(InvoiceStatus status, Page page) {
        return find("status", Sort.descending("issueDate"), status)
                .page(page)
                .list();
    }

    /**
     * Find overdue invoices (past due date and not paid)
     */
    public List<Invoice> findOverdueInvoices() {
        return find("dueDate < ?1 and status != ?2",
                LocalDate.now(), InvoiceStatus.PAID)
                .list();
    }

    /**
     * Find invoices due within a date range
     */
    public List<Invoice> findDueBetween(LocalDate startDate, LocalDate endDate) {
        return find("dueDate between ?1 and ?2",
                Sort.ascending("dueDate"), startDate, endDate)
                .list();
    }

    /**
     * Find unpaid invoices for a patient
     */
    public List<Invoice> findUnpaidByPatientId(Long patientId) {
        return find("patientId = ?1 and status != ?2",
                Sort.descending("dueDate"), patientId, InvoiceStatus.PAID)
                .list();
    }

    /**
     * Find invoices by doctor
     */
    public List<Invoice> findByDoctorId(Long doctorId) {
        return find("doctorId", Sort.descending("issueDate"), doctorId).list();
    }

    /**
     * Calculate total outstanding amount for a patient
     */
    public BigDecimal calculateOutstandingAmountForPatient(Long patientId) {
        return find("select sum(i.amountDue) from Invoice i where i.patientId = ?1 and i.status != ?2",
                patientId, InvoiceStatus.PAID)
                .project(BigDecimal.class)
                .firstResult();
    }

    /**
     * Count invoices by status
     */
    public long countByStatus(InvoiceStatus status) {
        return count("status", status);
    }

    /**
     * Count overdue invoices
     */
    public long countOverdueInvoices() {
        return count("dueDate < ?1 and status != ?2",
                LocalDate.now(), InvoiceStatus.PAID);
    }

    /**
     * Find invoices with amount greater than specified
     */
    public List<Invoice> findByTotalAmountGreaterThan(BigDecimal amount) {
        return find("totalAmount > ?1", Sort.descending("totalAmount"), amount).list();
    }

    /**
     * Find recently issued invoices (last N days)
     */
    public List<Invoice> findRecentlyIssued(int days) {
        LocalDate since = LocalDate.now().minusDays(days);
        return find("issueDate >= ?1", Sort.descending("issueDate"), since).list();
    }

    /**
     * Find invoices with partial payments
     */
    public List<Invoice> findPartiallyPaid() {
        return find("status = ?1", Sort.descending("issueDate"), InvoiceStatus.PARTIALLY_PAID).list();
    }

    /**
     * Update invoice status
     */
    public void updateStatus(Long invoiceId, InvoiceStatus newStatus) {
        update("status = ?1 where id = ?2", newStatus, invoiceId);
    }

    /**
     * Find invoices linked to insurance claim
     */
    public List<Invoice> findByInsuranceClaimId(Long insuranceClaimId) {
        return find("insuranceClaimId", insuranceClaimId).list();
    }
}

