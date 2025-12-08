package com.basit.dto.response;

import com.basit.constant.ClaimStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InsuranceClaimResponse {
    public Long id;
    public Long invoiceId;
    public Long patientId;
    public String insuranceProvider;
    public String policyNumber;
    public BigDecimal claimedAmount;
    public BigDecimal approvedAmount;
    public ClaimStatus status;
    public String claimNumber;
    public LocalDateTime submissionDate;
    public LocalDateTime approvalDate;
    public String notes;
}
