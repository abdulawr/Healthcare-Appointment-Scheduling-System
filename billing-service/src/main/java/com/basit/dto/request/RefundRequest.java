package com.basit.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class RefundRequest {

    @NotNull(message = "Payment ID is required")
    public Long paymentId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    public BigDecimal amount;

    @NotNull(message = "Reason is required")
    public String reason;
}
