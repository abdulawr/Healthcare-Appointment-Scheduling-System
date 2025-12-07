package com.basit.dto.response;

import java.time.LocalDateTime;
import com.basit.constant.PaymentMethodType;

public class PaymentMethodResponse {
    public Long id;
    public Long patientId;
    public PaymentMethodType paymentType;
    public String paymentGateway;
    public String cardLastFour;
    public String cardBrand;
    public Integer cardExpiryMonth;
    public Integer cardExpiryYear;
    public String bankName;
    public String accountLastFour;
    public String billingZipCode;
    public Boolean isDefault;
    public Boolean isActive;
    public String nickname;
    public LocalDateTime lastUsedAt;
    public String maskedDisplay;
    public LocalDateTime createdAt;
}

