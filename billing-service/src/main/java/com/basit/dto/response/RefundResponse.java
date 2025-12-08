package com.basit.dto.response;

import com.basit.constant.RefundStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundResponse {
    public Long id;
    public Long paymentId;
    public Long invoiceId;
    public Long patientId;
    public BigDecimal amount;
    public RefundStatus status;
    public String reason;
    public String refundTransactionId;
    public LocalDateTime requestDate;
    public LocalDateTime processedDate;
    public String failureReason;
}
