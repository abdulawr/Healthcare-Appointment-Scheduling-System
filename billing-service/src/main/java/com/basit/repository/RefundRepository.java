package com.basit.repository;

import com.basit.entity.Refund;
import com.basit.constant.RefundStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class RefundRepository implements PanacheRepository<Refund> {

    public List<Refund> findByPaymentId(Long paymentId) {
        return list("paymentId", paymentId);
    }

    public List<Refund> findByInvoiceId(Long invoiceId) {
        return list("invoiceId", invoiceId);
    }

    public List<Refund> findByPatientId(Long patientId) {
        return list("patientId", patientId);
    }

    public List<Refund> findByStatus(RefundStatus status) {
        return list("status", status);
    }

    public Refund findByRefundTransactionId(String refundTransactionId) {
        return find("refundTransactionId", refundTransactionId).firstResult();
    }
}
