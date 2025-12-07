package com.basit.dto.request;

import com.basit.constant.PaymentMethodType;
import jakarta.validation.constraints.*;

public class SavePaymentMethodRequest {

    @NotNull(message = "Patient ID is required")
    public Long patientId;

    @NotNull(message = "Payment type is required")
    public PaymentMethodType paymentType;

    @NotBlank(message = "Payment token is required")
    public String paymentToken;

    @NotBlank(message = "Payment gateway is required")
    public String paymentGateway;

    public String cardLastFour;
    public String cardBrand;
    public Integer cardExpiryMonth;
    public Integer cardExpiryYear;
    public String bankName;
    public String accountLastFour;
    public String billingAddress;
    public String billingZipCode;
    public Boolean isDefault;
    public String nickname;
}

