package com.basit.dto.response;


import com.basit.constant.PaymentMethodType;
import com.basit.constant.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    public Long id;
    public Long invoiceId;
    public Long patientId;
    public BigDecimal amount;
    public PaymentMethodType paymentMethod;
    public PaymentStatus status;
    public String transactionId;
    public String paymentGateway;
    public Long paymentMethodId;
    public String idempotencyKey;
    public LocalDateTime processedAt;
    public String failedReason;
    public String notes;
    public BigDecimal refundedAmount;
    public Boolean isRefundable;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

