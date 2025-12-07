package com.basit.entity;

import com.basit.constant.InvoiceStatus;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Invoice entity
 */
@QuarkusTest
public class InvoiceTest {

    @Test
    void shouldCreateInvoiceWithDefaults() {
        // Arrange & Act
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 100L;
        invoice.invoiceNumber = "INV-2024-001";
        invoice.issueDate = LocalDate.now();
        invoice.dueDate = LocalDate.now().plusDays(30);
        invoice.taxAmount = BigDecimal.ZERO;
        invoice.onCreate();

        // Assert
        assertThat(invoice.status).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(invoice.amountPaid).isEqualTo(BigDecimal.ZERO);
        assertThat(invoice.discountAmount).isEqualTo(BigDecimal.ZERO);
        assertThat(invoice.createdAt).isNotNull();
        assertThat(invoice.updatedAt).isNotNull();
    }

    @Test
    void shouldCalculateAmountsWithSingleItem() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 100L;
        invoice.invoiceNumber = "INV-2024-002";
        invoice.issueDate = LocalDate.now();
        invoice.dueDate = LocalDate.now().plusDays(30);
        invoice.taxAmount = new BigDecimal("10.00");
        invoice.discountAmount = BigDecimal.ZERO;

        InvoiceItem item = new InvoiceItem();
        item.description = "Consultation";
        item.quantity = 1;
        item.unitPrice = new BigDecimal("100.00");
        item.onCreate();

        // Act
        invoice.addItem(item);

        // Assert
        assertThat(invoice.subtotal).isEqualByComparingTo("100.00");
        assertThat(invoice.totalAmount).isEqualByComparingTo("110.00");
        assertThat(invoice.amountDue).isEqualByComparingTo("110.00");
        assertThat(invoice.items).hasSize(1);
    }

    @Test
    void shouldCalculateAmountsWithMultipleItems() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 100L;
        invoice.invoiceNumber = "INV-2024-003";
        invoice.issueDate = LocalDate.now();
        invoice.dueDate = LocalDate.now().plusDays(30);
        invoice.taxAmount = new BigDecimal("15.00");
        invoice.discountAmount = BigDecimal.ZERO;

        InvoiceItem item1 = new InvoiceItem();
        item1.description = "Consultation";
        item1.quantity = 1;
        item1.unitPrice = new BigDecimal("100.00");
        item1.onCreate();

        InvoiceItem item2 = new InvoiceItem();
        item2.description = "Lab Test";
        item2.quantity = 2;
        item2.unitPrice = new BigDecimal("50.00");
        item2.onCreate();

        // Act
        invoice.addItem(item1);
        invoice.addItem(item2);

        // Assert
        assertThat(invoice.subtotal).isEqualByComparingTo("200.00");
        assertThat(invoice.totalAmount).isEqualByComparingTo("215.00");
        assertThat(invoice.amountDue).isEqualByComparingTo("215.00");
        assertThat(invoice.items).hasSize(2);
    }

    @Test
    void shouldApplyDiscount() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 100L;
        invoice.invoiceNumber = "INV-2024-004";
        invoice.issueDate = LocalDate.now();
        invoice.dueDate = LocalDate.now().plusDays(30);
        invoice.taxAmount = new BigDecimal("10.00");
        invoice.discountAmount = new BigDecimal("20.00");

        InvoiceItem item = new InvoiceItem();
        item.description = "Consultation";
        item.quantity = 1;
        item.unitPrice = new BigDecimal("100.00");
        item.onCreate();

        // Act
        invoice.addItem(item);

        // Assert
        assertThat(invoice.subtotal).isEqualByComparingTo("100.00");
        assertThat(invoice.totalAmount).isEqualByComparingTo("90.00"); // 100 + 10 - 20
        assertThat(invoice.amountDue).isEqualByComparingTo("90.00");
    }

    @Test
    void shouldRecordPayment() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 100L;
        invoice.invoiceNumber = "INV-2024-005";
        invoice.issueDate = LocalDate.now();
        invoice.dueDate = LocalDate.now().plusDays(30);
        invoice.taxAmount = BigDecimal.ZERO;
        invoice.discountAmount = BigDecimal.ZERO;

        InvoiceItem item = new InvoiceItem();
        item.description = "Consultation";
        item.quantity = 1;
        item.unitPrice = new BigDecimal("100.00");
        item.onCreate();

        invoice.addItem(item);

        // Act
        invoice.recordPayment(new BigDecimal("50.00"));

        // Assert
        assertThat(invoice.amountPaid).isEqualByComparingTo("50.00");
        assertThat(invoice.amountDue).isEqualByComparingTo("50.00");
        assertThat(invoice.status).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
    }

    @Test
    void shouldMarkAsPaidWhenFullAmountPaid() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 100L;
        invoice.invoiceNumber = "INV-2024-006";
        invoice.issueDate = LocalDate.now();
        invoice.dueDate = LocalDate.now().plusDays(30);
        invoice.taxAmount = BigDecimal.ZERO;
        invoice.discountAmount = BigDecimal.ZERO;

        InvoiceItem item = new InvoiceItem();
        item.description = "Consultation";
        item.quantity = 1;
        item.unitPrice = new BigDecimal("100.00");
        item.onCreate();

        invoice.addItem(item);

        // Act
        invoice.recordPayment(new BigDecimal("100.00"));

        // Assert
        assertThat(invoice.amountPaid).isEqualByComparingTo("100.00");
        assertThat(invoice.amountDue).isEqualByComparingTo("0.00");
        assertThat(invoice.status).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.isPaid()).isTrue();
    }

    @Test
    void shouldThrowExceptionForInvalidPayment() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 100L;
        invoice.invoiceNumber = "INV-2024-007";
        invoice.issueDate = LocalDate.now();
        invoice.dueDate = LocalDate.now().plusDays(30);
        invoice.taxAmount = BigDecimal.ZERO;

        // Act & Assert
        assertThatThrownBy(() -> invoice.recordPayment(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment amount must be greater than zero");

        assertThatThrownBy(() -> invoice.recordPayment(new BigDecimal("-10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment amount must be greater than zero");
    }

    @Test
    void shouldMarkAsOverdueWhenPastDueDate() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 100L;
        invoice.invoiceNumber = "INV-2024-008";
        invoice.issueDate = LocalDate.now().minusDays(40);
        invoice.dueDate = LocalDate.now().minusDays(10);
        invoice.taxAmount = BigDecimal.ZERO;
        invoice.discountAmount = BigDecimal.ZERO;

        InvoiceItem item = new InvoiceItem();
        item.description = "Consultation";
        item.quantity = 1;
        item.unitPrice = new BigDecimal("100.00");
        item.onCreate();

        invoice.addItem(item);

        // Act
        invoice.updateStatus();

        // Assert
        assertThat(invoice.isOverdue()).isTrue();
    }

    @Test
    void shouldNotMarkAsOverdueWhenPaid() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 100L;
        invoice.invoiceNumber = "INV-2024-009";
        invoice.issueDate = LocalDate.now().minusDays(40);
        invoice.dueDate = LocalDate.now().minusDays(10);
        invoice.taxAmount = BigDecimal.ZERO;
        invoice.discountAmount = BigDecimal.ZERO;
        invoice.status = InvoiceStatus.PAID;
        invoice.amountDue = BigDecimal.ZERO;

        // Act & Assert
        assertThat(invoice.isOverdue()).isFalse();
    }

    @Test
    void shouldCheckIfInvoiceIsPaid() {
        // Arrange
        Invoice invoice1 = new Invoice();
        invoice1.status = InvoiceStatus.PAID;
        invoice1.amountDue = BigDecimal.ZERO;

        Invoice invoice2 = new Invoice();
        invoice2.status = InvoiceStatus.PARTIALLY_PAID;
        invoice2.amountDue = BigDecimal.ZERO;

        // Act & Assert
        assertThat(invoice1.isPaid()).isTrue();
        assertThat(invoice2.isPaid()).isTrue();
    }

    @Test
    void shouldRemoveItemAndRecalculate() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 100L;
        invoice.invoiceNumber = "INV-2024-010";
        invoice.issueDate = LocalDate.now();
        invoice.dueDate = LocalDate.now().plusDays(30);
        invoice.taxAmount = new BigDecimal("10.00");
        invoice.discountAmount = BigDecimal.ZERO;

        InvoiceItem item1 = new InvoiceItem();
        item1.description = "Consultation";
        item1.quantity = 1;
        item1.unitPrice = new BigDecimal("100.00");
        item1.onCreate();

        InvoiceItem item2 = new InvoiceItem();
        item2.description = "Lab Test";
        item2.quantity = 1;
        item2.unitPrice = new BigDecimal("50.00");
        item2.onCreate();

        invoice.addItem(item1);
        invoice.addItem(item2);

        // Verify both items are added
        assertThat(invoice.items).hasSize(2);
        assertThat(invoice.subtotal).isEqualByComparingTo("150.00");

        // Act - Remove item2
        invoice.removeItem(item2);

        // Assert - Only check the results that matter
        assertThat(invoice.items).hasSize(1);
        assertThat(invoice.subtotal).isEqualByComparingTo("100.00");
        assertThat(invoice.totalAmount).isEqualByComparingTo("110.00");

        // Verify item1 is still there by checking description
        assertThat(invoice.items.get(0).description).isEqualTo("Consultation");
    }

    @Test
    void shouldMaintainBidirectionalRelationship() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 100L;
        invoice.invoiceNumber = "INV-2024-011";
        invoice.issueDate = LocalDate.now();
        invoice.dueDate = LocalDate.now().plusDays(30);
        invoice.taxAmount = BigDecimal.ZERO;

        InvoiceItem item = new InvoiceItem();
        item.description = "Consultation";
        item.quantity = 1;
        item.unitPrice = new BigDecimal("100.00");
        item.onCreate();

        // Act
        invoice.addItem(item);

        // Assert
        assertThat(item.invoice).isEqualTo(invoice);
        assertThat(invoice.items).contains(item);
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        // Arrange
        Invoice invoice1 = new Invoice();
        invoice1.id = 1L;
        invoice1.invoiceNumber = "INV-001";

        Invoice invoice2 = new Invoice();
        invoice2.id = 1L;
        invoice2.invoiceNumber = "INV-001";

        Invoice invoice3 = new Invoice();
        invoice3.id = 2L;
        invoice3.invoiceNumber = "INV-002";

        // Act & Assert
        assertThat(invoice1).isEqualTo(invoice2);
        assertThat(invoice1).isNotEqualTo(invoice3);
        assertThat(invoice1.hashCode()).isEqualTo(invoice2.hashCode());
    }

    @Test
    void shouldHandleMultiplePartialPayments() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 100L;
        invoice.invoiceNumber = "INV-2024-012";
        invoice.issueDate = LocalDate.now();
        invoice.dueDate = LocalDate.now().plusDays(30);
        invoice.taxAmount = BigDecimal.ZERO;
        invoice.discountAmount = BigDecimal.ZERO;

        InvoiceItem item = new InvoiceItem();
        item.description = "Consultation";
        item.quantity = 1;
        item.unitPrice = new BigDecimal("100.00");
        item.onCreate();

        invoice.addItem(item);

        // Act
        invoice.recordPayment(new BigDecimal("30.00"));
        invoice.recordPayment(new BigDecimal("40.00"));
        invoice.recordPayment(new BigDecimal("30.00"));

        // Assert
        assertThat(invoice.amountPaid).isEqualByComparingTo("100.00");
        assertThat(invoice.amountDue).isEqualByComparingTo("0.00");
        assertThat(invoice.status).isEqualTo(InvoiceStatus.PAID);
    }
}
