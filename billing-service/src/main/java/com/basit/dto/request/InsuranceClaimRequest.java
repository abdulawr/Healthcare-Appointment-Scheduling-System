package com.basit.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class InsuranceClaimRequest {

    @NotNull(message = "Invoice ID is required")
    public Long invoiceId;

    @NotNull(message = "Insurance provider is required")
    public String insuranceProvider;

    @NotNull(message = "Policy number is required")
    public String policyNumber;

    @NotNull(message = "Claimed amount is required")
    @Positive(message = "Claimed amount must be positive")
    public BigDecimal claimedAmount;

    public String notes;
}