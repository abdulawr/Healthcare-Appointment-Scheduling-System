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

@ApplicationScoped
public class InvoiceService {

    @Inject
    InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        // Initialize status if not set
        if (invoice.status == null) {
            invoice.status = InvoiceStatus.DRAFT;
        }

        // Initialize amounts if not set
        if (invoice.amountPaid == null) {
            invoice.amountPaid = BigDecimal.ZERO;
        }

        // Calculate totals manually (invoice might not have calculateTotals method)
        invoice.subtotal = invoice.items.stream()
                .map(item -> item.unitPrice.multiply(BigDecimal.valueOf(item.quantity)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.totalAmount = invoice.subtotal
                .add(invoice.taxAmount != null ? invoice.taxAmount : BigDecimal.ZERO)
                .subtract(invoice.discountAmount != null ? invoice.discountAmount : BigDecimal.ZERO);

        invoiceRepository.persist(invoice);
        return invoice;
    }

    public Invoice getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id);
        if (invoice == null) {
            throw new RuntimeException("Invoice not found with id: " + id);
        }
        return invoice;
    }

    public Invoice getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber).orElse(null);
        if (invoice == null) {
            throw new RuntimeException("Invoice not found with number: " + invoiceNumber);
        }
        return invoice;
    }

    public List<Invoice> getInvoicesByPatientId(Long patientId, int page, int size) {
        return invoiceRepository.findByPatientId(patientId, Page.of(page, size));
    }

    @Transactional
    public Invoice updateInvoice(Long id, Invoice updateData) {
        Invoice invoice = getInvoiceById(id);

        // Only allow updates to DRAFT invoices
        if (invoice.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Can only update DRAFT invoices");
        }

        // Update allowed fields
        if (updateData.dueDate != null) {
            invoice.dueDate = updateData.dueDate;
        }
        if (updateData.taxAmount != null) {
            invoice.taxAmount = updateData.taxAmount;
        }
        if (updateData.discountAmount != null) {
            invoice.discountAmount = updateData.discountAmount;
        }
        if (updateData.notes != null) {
            invoice.notes = updateData.notes;
        }

        // Recalculate totals
        invoice.subtotal = invoice.items.stream()
                .map(item -> item.unitPrice.multiply(BigDecimal.valueOf(item.quantity)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.totalAmount = invoice.subtotal
                .add(invoice.taxAmount)
                .subtract(invoice.discountAmount);

        return invoice;
    }

    @Transactional
    public Invoice addItemToInvoice(Long invoiceId, InvoiceItem item) {
        Invoice invoice = getInvoiceById(invoiceId);

        // Only allow adding items to DRAFT invoices
        if (invoice.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Can only add items to DRAFT invoices");
        }

        item.invoice = invoice;
        invoice.items.add(item);

        // Recalculate totals
        invoice.subtotal = invoice.items.stream()
                .map(i -> i.unitPrice.multiply(BigDecimal.valueOf(i.quantity)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.totalAmount = invoice.subtotal
                .add(invoice.taxAmount)
                .subtract(invoice.discountAmount);

        return invoice;
    }

    @Transactional
    public Invoice removeItemFromInvoice(Long invoiceId, Long itemId) {
        Invoice invoice = getInvoiceById(invoiceId);

        // Only allow removing items from DRAFT invoices
        if (invoice.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Can only remove items from DRAFT invoices");
        }

        invoice.items.removeIf(item -> item.id.equals(itemId));

        // Recalculate totals
        invoice.subtotal = invoice.items.stream()
                .map(item -> item.unitPrice.multiply(BigDecimal.valueOf(item.quantity)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.totalAmount = invoice.subtotal
                .add(invoice.taxAmount)
                .subtract(invoice.discountAmount);

        return invoice;
    }

    @Transactional
    public Invoice issueInvoice(Long id) {
        Invoice invoice = getInvoiceById(id);

        // Validate invoice can be issued
        if (invoice.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT invoices can be issued");
        }

        if (invoice.items.isEmpty()) {
            throw new IllegalStateException("Cannot issue invoice without items");
        }

        if (invoice.subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Cannot issue invoice with zero or negative subtotal");
        }

        invoice.status = InvoiceStatus.ISSUED;

        return invoice;
    }

    @Transactional
    public Invoice recordPayment(Long id, BigDecimal paymentAmount) {
        Invoice invoice = getInvoiceById(id);

        if (invoice.status != InvoiceStatus.ISSUED &&
                invoice.status != InvoiceStatus.PARTIALLY_PAID &&
                invoice.status != InvoiceStatus.OVERDUE) {
            throw new IllegalStateException("Cannot record payment for invoice in status: " + invoice.status);
        }

        invoice.amountPaid = invoice.amountPaid.add(paymentAmount);

        // Update status based on payment
        BigDecimal remaining = invoice.totalAmount.subtract(invoice.amountPaid);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.status = InvoiceStatus.PAID;
        } else {
            invoice.status = InvoiceStatus.PARTIALLY_PAID;
        }

        return invoice;
    }

    public List<Invoice> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices();
    }

    public List<Invoice> getInvoicesByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    public List<Invoice> getUnpaidInvoicesByPatientId(Long patientId) {
        return invoiceRepository.findUnpaidByPatientId(patientId);
    }

    public BigDecimal calculateOutstandingAmount(Long patientId) {
        List<Invoice> unpaidInvoices = invoiceRepository.findUnpaidByPatientId(patientId);
        return unpaidInvoices.stream()
                .map(invoice -> invoice.totalAmount.subtract(invoice.amountPaid))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Invoice cancelInvoice(Long id, String reason) {
        Invoice invoice = getInvoiceById(id);

        if (invoice.status == InvoiceStatus.PAID || invoice.status == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel invoice in status: " + invoice.status);
        }

        invoice.status = InvoiceStatus.CANCELLED;
        invoice.notes = (invoice.notes != null ? invoice.notes + "\n" : "") +
                "CANCELLED: " + reason;

        return invoice;
    }

    @Transactional
    public void deleteInvoice(Long id) {
        Invoice invoice = getInvoiceById(id);

        // Only allow deletion of DRAFT invoices
        if (invoice.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Can only delete DRAFT invoices");
        }

        invoiceRepository.delete(invoice);
    }
}


