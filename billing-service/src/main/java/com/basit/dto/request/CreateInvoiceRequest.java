package com.basit.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreateInvoiceRequest {

    @NotNull(message = "Appointment ID is required")
    public Long appointmentId;

    @NotNull(message = "Patient ID is required")
    public Long patientId;

    public Long doctorId;

    @NotBlank(message = "Invoice number is required")
    public String invoiceNumber;

    @NotNull(message = "Issue date is required")
    public LocalDate issueDate;

    @NotNull(message = "Due date is required")
    public LocalDate dueDate;

    @NotNull(message = "Tax amount is required")
    @DecimalMin(value = "0.0", message = "Tax amount must be non-negative")
    public BigDecimal taxAmount;

    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    public BigDecimal discountAmount;

    public String notes;

    public List<InvoiceItemRequest> items;

    public static class InvoiceItemRequest {
        @NotBlank(message = "Description is required")
        public String description;

        public String serviceCode;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        public Integer quantity;

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
        public BigDecimal unitPrice;
    }
}

