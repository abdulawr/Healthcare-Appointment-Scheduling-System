package com.basit.dto.response;

import com.basit.constant.PaymentMethodType;
import java.time.LocalDateTime;

public class PaymentMethodResponse {
    public Long id;
    public Long patientId;
    public PaymentMethodType type;
    public String cardholderName;
    public String lastFourDigits;
    public String cardBrand;
    public Integer expiryMonth;
    public Integer expiryYear;
    public Boolean isDefault;
    public Boolean isActive;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
