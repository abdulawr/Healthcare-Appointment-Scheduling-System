package com.basit.mapper;

import com.basit.dto.request.ProcessPaymentRequest;
import com.basit.dto.response.PaymentResponse;
import com.basit.entity.Payment;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentMapper {

    public Payment toEntity(ProcessPaymentRequest request) {
        Payment payment = new Payment();
        payment.invoiceId = request.invoiceId;
        payment.patientId = request.patientId;
        payment.amount = request.amount;
        payment.paymentMethod = request.paymentMethod;
        payment.paymentGateway = request.paymentGateway;
        payment.paymentMethodId = request.paymentMethodId;
        payment.idempotencyKey = request.idempotencyKey;
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
        response.paymentGateway = payment.paymentGateway;
        response.paymentMethodId = payment.paymentMethodId;
        response.idempotencyKey = payment.idempotencyKey;
        response.processedAt = payment.processedAt;
        response.failedReason = payment.failedReason;
        response.notes = payment.notes;
        response.refundedAmount = payment.refundedAmount;
        response.isRefundable = payment.isRefundable;
        response.createdAt = payment.createdAt;
        response.updatedAt = payment.updatedAt;
        return response;
    }
}
