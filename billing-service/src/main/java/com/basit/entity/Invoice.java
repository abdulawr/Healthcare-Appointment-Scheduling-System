package com.basit.entity;

import com.basit.constant.InvoiceStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an invoice for healthcare services rendered during an appointment.
 * Aggregates invoice items and tracks payment status.
 */
@Entity
@Table(name = "invoices", indexes = {
        @Index(name = "idx_invoice_patient", columnList = "patient_id"),
        @Index(name = "idx_invoice_appointment", columnList = "appointment_id"),
        @Index(name = "idx_invoice_status", columnList = "status"),
        @Index(name = "idx_invoice_due_date", columnList = "due_date")
})
public class Invoice extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull(message = "Appointment ID is required")
    @Column(name = "appointment_id", nullable = false)
    public Long appointmentId;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    public Long patientId;

    @Column(name = "doctor_id")
    public Long doctorId;

    @NotBlank(message = "Invoice number is required")
    @Column(name = "invoice_number", unique = true, nullable = false, length = 50)
    public String invoiceNumber;

    @NotNull(message = "Issue date is required")
    @Column(name = "issue_date", nullable = false)
    public LocalDate issueDate;

    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    public LocalDate dueDate;

    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.00", message = "Subtotal must be non-negative")
    public BigDecimal subtotal;

    @NotNull(message = "Tax amount is required")
    @DecimalMin(value = "0.0", message = "Tax amount must be non-negative")
    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    public BigDecimal taxAmount;

    @NotNull(message = "Discount amount is required")
    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    public BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    public BigDecimal totalAmount;

    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.0", message = "Amount paid must be non-negative")
    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    public BigDecimal amountPaid = BigDecimal.ZERO;

    @NotNull(message = "Amount due is required")
    @DecimalMin(value = "0.0", message = "Amount due must be non-negative")
    @Column(name = "amount_due", nullable = false, precision = 10, scale = 2)
    public BigDecimal amountDue;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    public InvoiceStatus status;

    @Column(length = 1000)
    public String notes;

    @Column(name = "insurance_claim_id")
    public Long insuranceClaimId;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<InvoiceItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @Version
    public Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = InvoiceStatus.DRAFT;
        }
        if (amountPaid == null) {
            amountPaid = BigDecimal.ZERO;
        }
        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }
        calculateAmounts();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculates total amount and amount due based on items, tax, and discount
     */
    public void calculateAmounts() {
        this.subtotal = items.stream()
                .map(item -> item.unitPrice.multiply(BigDecimal.valueOf(item.quantity)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = subtotal
                .add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);

        this.amountDue = totalAmount.subtract(amountPaid != null ? amountPaid : BigDecimal.ZERO);
    }

    /**
     * Adds an invoice item to this invoice
     */
    public void addItem(InvoiceItem item) {
        items.add(item);
        item.invoice = this;
        calculateAmounts();
    }

    /**
     * Removes an invoice item from this invoice
     */
    public void removeItem(InvoiceItem item) {
        boolean removed = items.removeIf(i -> i == item ||
                (i.description != null && i.description.equals(item.description) &&
                        i.unitPrice != null && i.unitPrice.equals(item.unitPrice)));
        if (removed) {
            item.invoice = null;
            calculateAmounts();
        }
    }

    /**
     * Records a payment against this invoice
     */
    public void recordPayment(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        this.amountPaid = this.amountPaid.add(amount);
        this.amountDue = this.totalAmount.subtract(this.amountPaid);

        updateStatus();
    }

    /**
     * Updates invoice status based on payment status
     */
    public void updateStatus() {
        if (amountDue.compareTo(BigDecimal.ZERO) == 0) {
            this.status = InvoiceStatus.PAID;
        } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
            this.status = InvoiceStatus.PARTIALLY_PAID;
        } else if (LocalDate.now().isAfter(dueDate)) {
            this.status = InvoiceStatus.OVERDUE;
        }
    }

    /**
     * Checks if the invoice is overdue
     */
    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate) &&
                amountDue.compareTo(BigDecimal.ZERO) > 0 &&
                status != InvoiceStatus.PAID;
    }

    /**
     * Checks if the invoice is fully paid
     */
    public boolean isPaid() {
        return status == InvoiceStatus.PAID ||
                amountDue.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invoice)) return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(id, invoice.id) &&
                Objects.equals(invoiceNumber, invoice.invoiceNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, invoiceNumber);
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", patientId=" + patientId +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                '}';
    }
}
