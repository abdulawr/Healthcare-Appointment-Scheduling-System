package com.basit.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ApproveClaimRequest {

    @NotNull(message = "Approved amount is required")
    @DecimalMin(value = "0.0", message = "Approved amount must be non-negative")
    public BigDecimal approvedAmount;

    public String notes;
}

