package com.basit.event;

import com.basit.constant.PaymentMethodType;
import com.basit.constant.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentProcessedEvent {

    public Long paymentId;
    public Long invoiceId;
    public Long patientId;
    public BigDecimal amount;
    public PaymentMethodType paymentMethod;
    public PaymentStatus status;
    public String gateway;
    public String transactionId;
    public LocalDateTime paymentDate;
    public String notes;
    public LocalDateTime eventTimestamp;

    public PaymentProcessedEvent() {
        this.eventTimestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "PaymentProcessedEvent{" +
                "paymentId=" + paymentId +
                ", invoiceId=" + invoiceId +
                ", amount=" + amount +
                ", status=" + status +
                "}";
    }
}
