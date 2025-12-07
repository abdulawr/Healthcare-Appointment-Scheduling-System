package com.basit.service;

import com.basit.entity.PaymentMethod;
import com.basit.repository.PaymentMethodRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PaymentMethodService {

    @Inject
    PaymentMethodRepository paymentMethodRepository;

    @Transactional
    public PaymentMethod savePaymentMethod(PaymentMethod paymentMethod) {
        // onCreate() will be called automatically by @PrePersist
        paymentMethodRepository.persist(paymentMethod);
        return paymentMethod;
    }

    public PaymentMethod getPaymentMethodById(Long id) {
        return paymentMethodRepository.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Payment method not found with id: " + id));
    }

    public List<PaymentMethod> getPaymentMethodsByPatientId(Long patientId) {
        return paymentMethodRepository.findByPatientId(patientId);
    }

    public List<PaymentMethod> getActivePaymentMethods(Long patientId) {
        return paymentMethodRepository.findActiveByPatientId(patientId);
    }

    public PaymentMethod getDefaultPaymentMethod(Long patientId) {
        return paymentMethodRepository.findDefaultByPatientId(patientId)
                .orElse(null);
    }

    @Transactional
    public PaymentMethod setAsDefault(Long paymentMethodId, Long patientId) {
        // Unset all defaults for this patient
        paymentMethodRepository.unsetDefaultForPatient(patientId);

        // Set new default
        PaymentMethod paymentMethod = getPaymentMethodById(paymentMethodId);
        paymentMethod.setAsDefault();

        return paymentMethod;
    }

    @Transactional
    public void deactivatePaymentMethod(Long paymentMethodId) {
        PaymentMethod paymentMethod = getPaymentMethodById(paymentMethodId);
        paymentMethod.deactivate();
    }

    @Transactional
    public PaymentMethod recordUsage(Long paymentMethodId) {
        PaymentMethod paymentMethod = getPaymentMethodById(paymentMethodId);
        paymentMethod.recordUsage();
        return paymentMethod;
    }

    public List<PaymentMethod> getExpiredCards() {
        return paymentMethodRepository.findExpiredCards();
    }

    public List<PaymentMethod> getCardsExpiringSoon(int months) {
        return paymentMethodRepository.findCardsExpiringSoon(months);
    }

    @Transactional
    public void deletePaymentMethod(Long paymentMethodId) {
        PaymentMethod paymentMethod = getPaymentMethodById(paymentMethodId);
        paymentMethodRepository.delete(paymentMethod);
    }

    public boolean hasActivePaymentMethods(Long patientId) {
        return paymentMethodRepository.hasActivePaymentMethods(patientId);
    }

    public long countActivePaymentMethods(Long patientId) {
        return paymentMethodRepository.countActiveByPatientId(patientId);
    }

    public List<PaymentMethod> getAllPaymentMethods() {
        return paymentMethodRepository.listAll();
    }
}