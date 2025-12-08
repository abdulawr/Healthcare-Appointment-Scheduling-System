package com.basit.repository;

import com.basit.constant.InvoiceStatus;
import com.basit.entity.Invoice;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class InvoiceRepository implements PanacheRepository<Invoice> {

    public List<Invoice> findByPatientId(Long patientId) {
        return list("patientId", patientId);
    }

    public Invoice findByAppointmentId(Long appointmentId) {
        return find("appointmentId", appointmentId).firstResult();
    }

    public List<Invoice> findByStatus(InvoiceStatus status) {
        return list("status", status);
    }

    public List<Invoice> findOverdueInvoices() {
        return list("dueDate < ?1 and status = ?2",
                LocalDateTime.now(), InvoiceStatus.PENDING);
    }

    public List<Invoice> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return list("issueDate >= ?1 and issueDate <= ?2", startDate, endDate);
    }
}