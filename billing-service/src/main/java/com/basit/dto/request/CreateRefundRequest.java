package com.basit.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CreateRefundRequest {

    @NotNull(message = "Payment ID is required")
    public Long paymentId;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    public BigDecimal refundAmount;

    @NotBlank(message = "Reason is required")
    public String reason;

    public String requestedBy;

    public String notes;
}

