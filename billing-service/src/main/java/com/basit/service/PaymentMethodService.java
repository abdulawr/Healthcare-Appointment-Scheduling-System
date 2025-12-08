package com.basit.service;

import com.basit.dto.request.PaymentMethodRequest;
import com.basit.dto.response.PaymentMethodResponse;
import com.basit.entity.PaymentMethod;
import com.basit.mapper.PaymentMethodMapper;
import com.basit.repository.PaymentMethodRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PaymentMethodService {

    @Inject
    PaymentMethodRepository paymentMethodRepository;

    @Inject
    PaymentMethodMapper paymentMethodMapper;

    @Transactional
    public PaymentMethodResponse savePaymentMethod(PaymentMethodRequest request) {
        // If this is set as default, unset other default methods for this patient
        if (request.isDefault != null && request.isDefault) {
            List<PaymentMethod> existingMethods =
                    paymentMethodRepository.findByPatientId(request.patientId);

            for (PaymentMethod method : existingMethods) {
                if (method.isDefault) {
                    method.isDefault = false;
                    paymentMethodRepository.persist(method);
                }
            }
        }

        PaymentMethod paymentMethod = paymentMethodMapper.toEntity(request);
        paymentMethodRepository.persist(paymentMethod);

        return paymentMethodMapper.toResponse(paymentMethod);
    }

    @Transactional
    public PaymentMethodResponse updatePaymentMethod(Long id, PaymentMethodRequest request) {
        PaymentMethod paymentMethod = paymentMethodRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException(
                        "Payment method not found with id: " + id));

        // Validate patient ID matches
        if (!paymentMethod.patientId.equals(request.patientId)) {
            throw new IllegalArgumentException(
                    "Cannot change patient ID of payment method");
        }

        // If setting as default, unset other defaults
        if (request.isDefault != null && request.isDefault && !paymentMethod.isDefault) {
            List<PaymentMethod> existingMethods =
                    paymentMethodRepository.findByPatientId(request.patientId);

            for (PaymentMethod method : existingMethods) {
                if (method.isDefault && !method.id.equals(id)) {
                    method.isDefault = false;
                    paymentMethodRepository.persist(method);
                }
            }
        }

        // Update fields
        paymentMethod.type = request.type;
        paymentMethod.cardholderName = request.cardholderName;
        paymentMethod.lastFourDigits = request.lastFourDigits;
        paymentMethod.cardBrand = request.cardBrand;
        paymentMethod.expiryMonth = request.expiryMonth;
        paymentMethod.expiryYear = request.expiryYear;
        paymentMethod.token = request.token;

        if (request.isDefault != null) {
            paymentMethod.isDefault = request.isDefault;
        }

        paymentMethodRepository.persist(paymentMethod);
        return paymentMethodMapper.toResponse(paymentMethod);
    }

    @Transactional
    public void deletePaymentMethod(Long id) {
        PaymentMethod paymentMethod = paymentMethodRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException(
                        "Payment method not found with id: " + id));

        // Soft delete - mark as inactive instead of deleting
        paymentMethod.isActive = false;
        paymentMethodRepository.persist(paymentMethod);
    }

    @Transactional
    public PaymentMethodResponse setAsDefault(Long id, Long patientId) {
        PaymentMethod paymentMethod = paymentMethodRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException(
                        "Payment method not found with id: " + id));

        // Validate patient ID matches
        if (!paymentMethod.patientId.equals(patientId)) {
            throw new IllegalArgumentException(
                    "Payment method does not belong to patient: " + patientId);
        }

        // Unset other default methods
        List<PaymentMethod> existingMethods =
                paymentMethodRepository.findByPatientId(patientId);

        for (PaymentMethod method : existingMethods) {
            if (method.isDefault) {
                method.isDefault = false;
                paymentMethodRepository.persist(method);
            }
        }

        // Set this one as default
        paymentMethod.isDefault = true;
        paymentMethodRepository.persist(paymentMethod);

        return paymentMethodMapper.toResponse(paymentMethod);
    }

    public PaymentMethodResponse getPaymentMethod(Long id) {
        PaymentMethod paymentMethod = paymentMethodRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException(
                        "Payment method not found with id: " + id));
        return paymentMethodMapper.toResponse(paymentMethod);
    }

    public List<PaymentMethodResponse> getPatientPaymentMethods(Long patientId) {
        return paymentMethodRepository.findByPatientId(patientId).stream()
                .map(paymentMethodMapper::toResponse)
                .collect(Collectors.toList());
    }

    public PaymentMethodResponse getDefaultPaymentMethod(Long patientId) {
        PaymentMethod paymentMethod = paymentMethodRepository.findDefaultByPatientId(patientId);
        if (paymentMethod == null) {
            throw new NotFoundException(
                    "No default payment method found for patient: " + patientId);
        }
        return paymentMethodMapper.toResponse(paymentMethod);
    }

    public PaymentMethodResponse getPaymentMethodByToken(String token) {
        PaymentMethod paymentMethod = paymentMethodRepository.findByToken(token);
        if (paymentMethod == null) {
            throw new NotFoundException(
                    "Payment method not found with token: " + token);
        }
        return paymentMethodMapper.toResponse(paymentMethod);
    }
}
