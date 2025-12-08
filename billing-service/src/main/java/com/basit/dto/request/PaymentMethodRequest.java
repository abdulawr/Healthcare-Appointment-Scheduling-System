package com.basit.dto.request;


import com.basit.constant.PaymentMethodType;
import jakarta.validation.constraints.NotNull;

public class PaymentMethodRequest {

    @NotNull(message = "Patient ID is required")
    public Long patientId;

    @NotNull(message = "Payment method type is required")
    public PaymentMethodType type;

    public String cardholderName;

    public String lastFourDigits;

    public String cardBrand;

    public Integer expiryMonth;

    public Integer expiryYear;

    public String token;

    public Boolean isDefault = false;
}
