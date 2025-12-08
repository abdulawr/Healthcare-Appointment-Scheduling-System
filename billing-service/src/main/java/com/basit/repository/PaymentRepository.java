package com.basit.repository;

import com.basit.constant.PaymentStatus;
import com.basit.entity.Payment;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class PaymentRepository implements PanacheRepository<Payment> {

    public List<Payment> findByInvoiceId(Long invoiceId) {
        return list("invoiceId", invoiceId);
    }

    public List<Payment> findByPatientId(Long patientId) {
        return list("patientId", patientId);
    }

    public Payment findByTransactionId(String transactionId) {
        return find("transactionId", transactionId).firstResult();
    }

    public List<Payment> findByStatus(PaymentStatus status) {
        return list("status", status);
    }

    public List<Payment> findFailedPayments() {
        return list("status", PaymentStatus.FAILED);
    }
}
