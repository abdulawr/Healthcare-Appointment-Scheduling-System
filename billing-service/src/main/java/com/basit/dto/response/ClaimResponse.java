package com.basit.dto.response;



import com.basit.constant.ClaimStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ClaimResponse {
    public Long id;
    public Long invoiceId;
    public Long patientId;
    public String claimNumber;
    public String insuranceProvider;
    public String policyNumber;
    public String groupNumber;
    public BigDecimal claimAmount;
    public BigDecimal approvedAmount;
    public BigDecimal paidAmount;
    public BigDecimal patientResponsibility;
    public ClaimStatus status;
    public LocalDate submissionDate;
    public LocalDate processedDate;
    public LocalDate paidDate;
    public String denialReason;
    public String diagnosisCodes;
    public String procedureCodes;
    public String providerNPI;
    public String facilityCode;
    public String notes;
    public String externalClaimId;
    public Integer appealCount;
    public LocalDate lastAppealDate;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

