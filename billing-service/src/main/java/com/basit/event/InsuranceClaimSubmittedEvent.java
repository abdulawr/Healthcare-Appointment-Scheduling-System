package com.basit.event;

import com.basit.constant.ClaimStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InsuranceClaimSubmittedEvent {

    public Long claimId;
    public Long invoiceId;
    public Long patientId;
    public String insuranceProvider;
    public String policyNumber;
    public BigDecimal claimAmount;
    public ClaimStatus status;
    public LocalDateTime submittedDate;
    public String submittedBy;
    public LocalDateTime eventTimestamp;

    public InsuranceClaimSubmittedEvent() {
        this.eventTimestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "InsuranceClaimSubmittedEvent{" +
                "claimId=" + claimId +
                ", invoiceId=" + invoiceId +
                ", insuranceProvider='" + insuranceProvider + '\'' +
                ", claimAmount=" + claimAmount +
                "}";
    }
}
