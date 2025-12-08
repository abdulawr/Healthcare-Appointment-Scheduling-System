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
    public String gateway;
    public LocalDateTime paymentDate;
    public LocalDateTime processedDate;
    public String failureReason;
    public String notes;
}
