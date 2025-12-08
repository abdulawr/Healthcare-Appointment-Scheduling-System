package com.basit.entity;

import com.basit.constant.InvoiceStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class InvoiceEntityTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    public void testCreateInvoice() {
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 1L;
        invoice.subtotal = new BigDecimal("100.00");
        invoice.tax = new BigDecimal("10.00");
        invoice.total = new BigDecimal("110.00");
        invoice.persist();

        assertNotNull(invoice.id);
        assertEquals(InvoiceStatus.PENDING, invoice.status);
        assertNotNull(invoice.issueDate);
        assertEquals(0, invoice.total.compareTo(invoice.balance));
    }

    @Test
    @Transactional
    public void testInvoiceBalanceCalculation() {
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 1L;
        invoice.subtotal = new BigDecimal("100.00");
        invoice.tax = new BigDecimal("10.00");
        invoice.total = new BigDecimal("110.00");
        invoice.amountPaid = new BigDecimal("50.00");
        invoice.persist();

        // Use compareTo for BigDecimal comparison
        assertEquals(0, new BigDecimal("60.00").compareTo(invoice.balance));
    }

    @Test
    @Transactional
    public void testInvoiceFullyPaid() {
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 1L;
        invoice.subtotal = new BigDecimal("100.00");
        invoice.tax = new BigDecimal("10.00");
        invoice.total = new BigDecimal("110.00");
        invoice.amountPaid = new BigDecimal("110.00");
        invoice.persist();

        // Use compareTo for BigDecimal comparison
        // compareTo returns 0 if values are equal
        assertEquals(0, BigDecimal.ZERO.compareTo(invoice.balance),
                "Balance should be zero");
        assertEquals(InvoiceStatus.PAID, invoice.status);
        assertNotNull(invoice.paidDate);
    }

    @Test
    @Transactional
    public void testInvoiceWithItems() {
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 1L;
        invoice.subtotal = new BigDecimal("100.00");
        invoice.tax = new BigDecimal("10.00");
        invoice.total = new BigDecimal("110.00");

        InvoiceItem item1 = new InvoiceItem();
        item1.invoice = invoice;
        item1.description = "Consultation";
        item1.quantity = 1;
        item1.unitPrice = new BigDecimal("80.00");
        invoice.items.add(item1);

        InvoiceItem item2 = new InvoiceItem();
        item2.invoice = invoice;
        item2.description = "Lab Test";
        item2.quantity = 2;
        item2.unitPrice = new BigDecimal("10.00");
        invoice.items.add(item2);

        invoice.persist();

        assertEquals(2, invoice.items.size());
        assertEquals(0, new BigDecimal("80.00").compareTo(item1.amount));
        assertEquals(0, new BigDecimal("20.00").compareTo(item2.amount));
    }

    @Test
    @Transactional
    public void testInvoiceDueDateAutoSet() {
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 1L;
        invoice.subtotal = new BigDecimal("100.00");
        invoice.tax = new BigDecimal("10.00");
        invoice.total = new BigDecimal("110.00");
        invoice.persist();

        assertNotNull(invoice.issueDate);
        assertNotNull(invoice.dueDate);
        assertTrue(invoice.dueDate.isAfter(invoice.issueDate));
    }

    @Test
    @Transactional
    public void testPartialPayment() {
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 1L;
        invoice.subtotal = new BigDecimal("100.00");
        invoice.tax = new BigDecimal("10.00");
        invoice.total = new BigDecimal("110.00");
        invoice.amountPaid = new BigDecimal("30.00");
        invoice.persist();

        assertEquals(0, new BigDecimal("80.00").compareTo(invoice.balance));
        assertEquals(InvoiceStatus.PENDING, invoice.status);
        assertNull(invoice.paidDate);
    }
}
