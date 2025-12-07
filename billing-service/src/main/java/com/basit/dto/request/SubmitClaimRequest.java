package com.basit.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class SubmitClaimRequest {

    @NotNull(message = "Invoice ID is required")
    public Long invoiceId;

    @NotNull(message = "Patient ID is required")
    public Long patientId;

    @NotBlank(message = "Claim number is required")
    public String claimNumber;

    @NotBlank(message = "Insurance provider is required")
    public String insuranceProvider;

    @NotBlank(message = "Policy number is required")
    public String policyNumber;

    public String groupNumber;

    @NotNull(message = "Claim amount is required")
    @DecimalMin(value = "0.01", message = "Claim amount must be greater than 0")
    public BigDecimal claimAmount;

    public String diagnosisCodes;
    public String procedureCodes;
    public String providerNPI;
    public String facilityCode;
    public String notes;
}

