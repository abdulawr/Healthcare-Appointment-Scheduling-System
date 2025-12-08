package com.basit.mapper;

import com.basit.dto.request.RefundRequest;
import com.basit.dto.response.RefundResponse;
import com.basit.entity.Refund;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RefundMapper {

    public Refund toEntity(RefundRequest request) {
        Refund refund = new Refund();
        refund.paymentId = request.paymentId;
        refund.amount = request.amount;
        refund.reason = request.reason;
        return refund;
    }

    public RefundResponse toResponse(Refund refund) {
        RefundResponse response = new RefundResponse();
        response.id = refund.id;
        response.paymentId = refund.paymentId;
        response.invoiceId = refund.invoiceId;
        response.patientId = refund.patientId;
        response.amount = refund.amount;
        response.status = refund.status;
        response.reason = refund.reason;
        response.refundTransactionId = refund.refundTransactionId;
        response.requestDate = refund.requestDate;
        response.processedDate = refund.processedDate;
        response.failureReason = refund.failureReason;
        return response;
    }
}
