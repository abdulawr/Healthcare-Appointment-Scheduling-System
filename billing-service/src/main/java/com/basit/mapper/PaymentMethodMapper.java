package com.basit.mapper;

import com.basit.dto.request.PaymentMethodRequest;
import com.basit.dto.response.PaymentMethodResponse;
import com.basit.entity.PaymentMethod;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentMethodMapper {

    public PaymentMethod toEntity(PaymentMethodRequest request) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.patientId = request.patientId;
        paymentMethod.type = request.type;
        paymentMethod.cardholderName = request.cardholderName;
        paymentMethod.lastFourDigits = request.lastFourDigits;
        paymentMethod.cardBrand = request.cardBrand;
        paymentMethod.expiryMonth = request.expiryMonth;
        paymentMethod.expiryYear = request.expiryYear;
        paymentMethod.token = request.token;
        paymentMethod.isDefault = request.isDefault != null ? request.isDefault : false;
        return paymentMethod;
    }

    public PaymentMethodResponse toResponse(PaymentMethod paymentMethod) {
        PaymentMethodResponse response = new PaymentMethodResponse();
        response.id = paymentMethod.id;
        response.patientId = paymentMethod.patientId;
        response.type = paymentMethod.type;
        response.cardholderName = paymentMethod.cardholderName;
        response.lastFourDigits = paymentMethod.lastFourDigits;
        response.cardBrand = paymentMethod.cardBrand;
        response.expiryMonth = paymentMethod.expiryMonth;
        response.expiryYear = paymentMethod.expiryYear;
        response.isDefault = paymentMethod.isDefault;
        response.isActive = paymentMethod.isActive;
        response.createdAt = paymentMethod.createdAt;
        response.updatedAt = paymentMethod.updatedAt;
        return response;
    }
}
