package com.basit.repository;

import com.basit.entity.PaymentMethod;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class PaymentMethodRepository implements PanacheRepository<PaymentMethod> {

    public List<PaymentMethod> findByPatientId(Long patientId) {
        return list("patientId = ?1 and isActive = true", patientId);
    }

    public PaymentMethod findDefaultByPatientId(Long patientId) {
        return find("patientId = ?1 and isDefault = true and isActive = true",
                patientId).firstResult();
    }

    public PaymentMethod findByToken(String token) {
        return find("token", token).firstResult();
    }
}
