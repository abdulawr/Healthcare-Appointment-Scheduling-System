package com.basit.event;

import com.basit.constant.PaymentMethodType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentFailedEvent {

    public Long paymentId;
    public Long invoiceId;
    public Long patientId;
    public BigDecimal amount;
    public PaymentMethodType paymentMethod;
    public String gateway;
    public String failureReason;
    public String errorCode;
    public LocalDateTime attemptDate;
    public LocalDateTime eventTimestamp;

    public PaymentFailedEvent() {
        this.eventTimestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "PaymentFailedEvent{" +
                "paymentId=" + paymentId +
                ", invoiceId=" + invoiceId +
                ", failureReason='" + failureReason + '\'' +
                "}";
    }
}