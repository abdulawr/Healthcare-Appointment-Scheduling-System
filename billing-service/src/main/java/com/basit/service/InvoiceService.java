package com.basit.service;

import com.basit.constant.InvoiceStatus;
import com.basit.entity.Invoice;
import com.basit.entity.InvoiceItem;
import com.basit.repository.InvoiceRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for Invoice business logic
 */
@ApplicationScoped
public class InvoiceService {

    @Inject
    InvoiceRepository invoiceRepository;

    /**
     * Create a new invoice
     */
    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        // Set default status if not set
        if (invoice.status == null) {
            invoice.status = InvoiceStatus.DRAFT;
        }

        // Calculate amounts
        invoice.calculateAmounts();

        // Persist invoice (onCreate() will be called automatically by @PrePersist)
        invoiceRepository.persist(invoice);

        return invoice;
    }

    /**
     * Get invoice by ID
     */
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }

    /**
     * Get invoice by invoice number
     */
    public Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found with number: " + invoiceNumber));
    }

    /**
     * Get all invoices for a patient
     */
    public List<Invoice> getInvoicesByPatientId(Long patientId) {
        return invoiceRepository.findByPatientId(patientId);
    }

    /**
     * Get invoices for a patient with pagination
     */
    public List<Invoice> getInvoicesByPatientId(Long patientId, int pageIndex, int pageSize) {
        return invoiceRepository.findByPatientId(patientId, Page.of(pageIndex, pageSize));
    }

    /**
     * Update invoice
     */
    @Transactional
    public Invoice updateInvoice(Long id, Invoice updatedInvoice) {
        Invoice invoice = getInvoiceById(id);

        // Update fields
        if (updatedInvoice.dueDate != null) {
            invoice.dueDate = updatedInvoice.dueDate;
        }
        if (updatedInvoice.taxAmount != null) {
            invoice.taxAmount = updatedInvoice.taxAmount;
        }
        if (updatedInvoice.discountAmount != null) {
            invoice.discountAmount = updatedInvoice.discountAmount;
        }
        if (updatedInvoice.notes != null) {
            invoice.notes = updatedInvoice.notes;
        }

        // Recalculate amounts
        invoice.calculateAmounts();
        // onUpdate() will be called automatically by @PreUpdate

        return invoice;
    }

    /**
     * Add item to invoice
     */
    @Transactional
    public Invoice addItemToInvoice(Long invoiceId, InvoiceItem item) {
        Invoice invoice = getInvoiceById(invoiceId);

        // Ensure invoice is still in DRAFT status
        if (invoice.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Can only add items to draft invoices");
        }

        invoice.addItem(item);

        return invoice;
    }

    /**
     * Remove item from invoice
     */
    @Transactional
    public Invoice removeItemFromInvoice(Long invoiceId, Long itemId) {
        Invoice invoice = getInvoiceById(invoiceId);

        // Ensure invoice is still in DRAFT status
        if (invoice.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Can only remove items from draft invoices");
        }

        InvoiceItem itemToRemove = invoice.items.stream()
                .filter(item -> item.id.equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found"));

        invoice.removeItem(itemToRemove);

        return invoice;
    }

    /**
     * Issue invoice (change from DRAFT to ISSUED)
     */
    @Transactional
    public Invoice issueInvoice(Long invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);

        if (invoice.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only draft invoices can be issued");
        }

        if (invoice.items == null || invoice.items.isEmpty()) {
            throw new IllegalStateException("Cannot issue invoice without items");
        }

        invoice.updateStatus();
        invoice.status = InvoiceStatus.ISSUED;

        return invoice;
    }

    /**
     * Record payment against invoice
     */
    @Transactional
    public Invoice recordPayment(Long invoiceId, BigDecimal amount) {
        Invoice invoice = getInvoiceById(invoiceId);
        invoice.recordPayment(amount);
        return invoice;
    }

    /**
     * Get overdue invoices
     */
    public List<Invoice> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices();
    }

    /**
     * Get invoices by status
     */
    public List<Invoice> getInvoicesByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    /**
     * Get unpaid invoices for patient
     */
    public List<Invoice> getUnpaidInvoicesByPatientId(Long patientId) {
        return invoiceRepository.findUnpaidByPatientId(patientId);
    }

    /**
     * Calculate outstanding amount for patient
     */
    public BigDecimal calculateOutstandingAmount(Long patientId) {
        BigDecimal amount = invoiceRepository.calculateOutstandingAmountForPatient(patientId);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    /**
     * Cancel invoice
     */
    @Transactional
    public Invoice cancelInvoice(Long invoiceId, String reason) {
        Invoice invoice = getInvoiceById(invoiceId);

        if (invoice.status == InvoiceStatus.PAID) {
            throw new IllegalStateException("Cannot cancel paid invoice");
        }

        invoice.status = InvoiceStatus.CANCELLED;
        invoice.notes = (invoice.notes != null ? invoice.notes + "; " : "") + "CANCELLED: " + reason;

        return invoice;
    }

    /**
     * Get recently issued invoices
     */
    public List<Invoice> getRecentlyIssuedInvoices(int days) {
        return invoiceRepository.findRecentlyIssued(days);
    }

    /**
     * Delete invoice (only DRAFT invoices)
     */
    @Transactional
    public void deleteInvoice(Long invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);

        if (invoice.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Can only delete draft invoices");
        }

        invoiceRepository.delete(invoice);
    }

    /**
     * Get all invoices
     */
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.listAll();
    }

    /**
     * Count invoices by status
     */
    public long countByStatus(InvoiceStatus status) {
        return invoiceRepository.countByStatus(status);
    }
}
