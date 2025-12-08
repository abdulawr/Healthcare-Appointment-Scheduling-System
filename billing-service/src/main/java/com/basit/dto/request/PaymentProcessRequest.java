package com.basit.dto.request;

import com.basit.constant.PaymentMethodType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class PaymentProcessRequest {

    @NotNull(message = "Invoice ID is required")
    public Long invoiceId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    public BigDecimal amount;

    @NotNull(message = "Payment method is required")
    public PaymentMethodType paymentMethod;

    public String paymentToken;

    public String gateway;

    public String notes;
}
