package com.basit.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public class InvoiceCreateRequest {

    @NotNull(message = "Appointment ID is required")
    public Long appointmentId;

    @NotNull(message = "Patient ID is required")
    public Long patientId;

    @NotNull(message = "Items are required")
    public List<InvoiceItemRequest> items;

    public String notes;

    public static class InvoiceItemRequest {
        @NotNull(message = "Description is required")
        public String description;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        public Integer quantity;

        @NotNull(message = "Unit price is required")
        @Positive(message = "Unit price must be positive")
        public BigDecimal unitPrice;
    }
}
