package com.basit.dto.response;

import com.basit.constant.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundResponse {
    public Long id;
    public Long paymentId;
    public Long invoiceId;
    public Long patientId;
    public BigDecimal refundAmount;
    public RefundStatus status;
    public String reason;
    public String refundTransactionId;
    public String paymentGateway;
    public LocalDateTime processedAt;
    public String failedReason;
    public String requestedBy;
    public String approvedBy;
    public LocalDateTime approvedAt;
    public String notes;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
