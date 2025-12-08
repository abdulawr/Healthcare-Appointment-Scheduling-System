package com.basit.event;

import com.basit.constant.RefundStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundIssuedEvent {

    public Long refundId;
    public Long paymentId;
    public Long invoiceId;
    public Long patientId;
    public BigDecimal amount;
    public RefundStatus status;
    public String reason;
    public LocalDateTime refundDate;
    public String processedBy;
    public LocalDateTime eventTimestamp;

    public RefundIssuedEvent() {
        this.eventTimestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "RefundIssuedEvent{" +
                "refundId=" + refundId +
                ", paymentId=" + paymentId +
                ", amount=" + amount +
                ", reason='" + reason + '\'' +
                "}";
    }
}
