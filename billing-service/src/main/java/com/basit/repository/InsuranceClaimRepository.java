package com.basit.repository;

import com.basit.entity.InsuranceClaim;
import com.basit.constant.ClaimStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class InsuranceClaimRepository implements PanacheRepository<InsuranceClaim> {

    public List<InsuranceClaim> findByInvoiceId(Long invoiceId) {
        return list("invoiceId", invoiceId);
    }

    public List<InsuranceClaim> findByPatientId(Long patientId) {
        return list("patientId", patientId);
    }

    public InsuranceClaim findByClaimNumber(String claimNumber) {
        return find("claimNumber", claimNumber).firstResult();
    }

    public List<InsuranceClaim> findByStatus(ClaimStatus status) {
        return list("status", status);
    }

    public List<InsuranceClaim> findByInsuranceProvider(String provider) {
        return list("insuranceProvider", provider);
    }
}
