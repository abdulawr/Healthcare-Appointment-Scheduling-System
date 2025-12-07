package com.basit.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class AddInvoiceItemRequest {

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

