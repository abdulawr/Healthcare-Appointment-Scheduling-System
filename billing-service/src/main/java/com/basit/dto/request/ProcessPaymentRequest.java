package com.basit.dto.request;

import jakarta.validation.constraints.*;
import com.basit.constant.PaymentMethodType;

import java.math.BigDecimal;

public class ProcessPaymentRequest {

    @NotNull(message = "Invoice ID is required")
    public Long invoiceId;

    @NotNull(message = "Patient ID is required")
    public Long patientId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    public BigDecimal amount;

    @NotNull(message = "Payment method is required")
    public PaymentMethodType paymentMethod;

    @NotBlank(message = "Payment gateway is required")
    public String paymentGateway;

    public Long paymentMethodId;

    public String idempotencyKey;

    public String notes;
}

