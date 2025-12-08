package com.basit.mapper;

import com.basit.dto.request.PaymentProcessRequest;
import com.basit.dto.response.PaymentResponse;
import com.basit.entity.Payment;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentMapper {

    public Payment toEntity(PaymentProcessRequest request) {
        Payment payment = new Payment();
        payment.invoiceId = request.invoiceId;
        payment.amount = request.amount;
        payment.paymentMethod = request.paymentMethod;
        payment.gateway = request.gateway;
        payment.notes = request.notes;
        return payment;
    }

    public PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.id = payment.id;
        response.invoiceId = payment.invoiceId;
        response.patientId = payment.patientId;
        response.amount = payment.amount;
        response.paymentMethod = payment.paymentMethod;
        response.status = payment.status;
        response.transactionId = payment.transactionId;
        response.gateway = payment.gateway;
        response.paymentDate = payment.paymentDate;
        response.processedDate = payment.processedDate;
        response.failureReason = payment.failureReason;
        response.notes = payment.notes;
        return response;
    }
}
