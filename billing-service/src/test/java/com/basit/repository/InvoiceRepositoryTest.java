package com.basit.repository;


import com.basit.entity.Invoice;
import com.basit.constant.InvoiceStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class InvoiceRepositoryTest {

    @Inject
    InvoiceRepository invoiceRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        invoiceRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testFindByPatientId() {
        Invoice invoice1 = createTestInvoice(1L, 100L);
        Invoice invoice2 = createTestInvoice(1L, 101L);
        Invoice invoice3 = createTestInvoice(2L, 102L);

        invoiceRepository.persist(invoice1);
        invoiceRepository.persist(invoice2);
        invoiceRepository.persist(invoice3);

        List<Invoice> patient1Invoices = invoiceRepository.findByPatientId(1L);
        assertEquals(2, patient1Invoices.size());

        List<Invoice> patient2Invoices = invoiceRepository.findByPatientId(2L);
        assertEquals(1, patient2Invoices.size());
    }

    @Test
    @Transactional
    public void testFindByAppointmentId() {
        Invoice invoice = createTestInvoice(1L, 100L);
        invoiceRepository.persist(invoice);

        Invoice found = invoiceRepository.findByAppointmentId(100L);
        assertNotNull(found);
        assertEquals(100L, found.appointmentId);
    }

    @Test
    @Transactional
    public void testFindOverdueInvoices() {
        Invoice overdueInvoice = createTestInvoice(1L, 100L);
        overdueInvoice.dueDate = LocalDateTime.now().minusDays(5);
        overdueInvoice.status = InvoiceStatus.PENDING;

        Invoice currentInvoice = createTestInvoice(2L, 101L);
        currentInvoice.dueDate = LocalDateTime.now().plusDays(5);
        currentInvoice.status = InvoiceStatus.PENDING;

        invoiceRepository.persist(overdueInvoice);
        invoiceRepository.persist(currentInvoice);

        List<Invoice> overdue = invoiceRepository.findOverdueInvoices();
        assertEquals(1, overdue.size());
        assertEquals(overdueInvoice.id, overdue.get(0).id);
    }

    private Invoice createTestInvoice(Long patientId, Long appointmentId) {
        Invoice invoice = new Invoice();
        invoice.patientId = patientId;
        invoice.appointmentId = appointmentId;
        invoice.subtotal = new BigDecimal("100.00");
        invoice.tax = new BigDecimal("10.00");
        invoice.total = new BigDecimal("110.00");
        return invoice;
    }
}
