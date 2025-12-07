package com.basit.service;

import com.basit.constant.InvoiceStatus;
import com.basit.entity.Invoice;
import com.basit.entity.InvoiceItem;
import com.basit.repository.InvoiceRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InvoiceServiceTest {

    @Inject
    InvoiceService invoiceService;

    @Inject
    InvoiceRepository invoiceRepository;

    private static Long testInvoiceId;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up before each test
        invoiceRepository.deleteAll();
    }

    @Test
    @Order(1)
    @Transactional
    void testCreateInvoice() {
        // Given
        Invoice invoice = createTestInvoice();

        // When
        Invoice created = invoiceService.createInvoice(invoice);

        // Then
        assertNotNull(created.id);
        assertEquals(InvoiceStatus.DRAFT, created.status);
        assertEquals(BigDecimal.ZERO, created.amountPaid);
        assertNotNull(created.createdAt);
        testInvoiceId = created.id;
    }

    @Test
    @Order(2)
    @Transactional
    void testCreateInvoiceWithItems() {
        // Given
        Invoice invoice = createTestInvoice();
        InvoiceItem item1 = createInvoiceItem("Consultation", new BigDecimal("100.00"), 1);
        InvoiceItem item2 = createInvoiceItem("Lab Test", new BigDecimal("50.00"), 2);
        invoice.addItem(item1);
        invoice.addItem(item2);

        // When
        Invoice created = invoiceService.createInvoice(invoice);

        // Then
        assertNotNull(created.id);
        assertEquals(2, created.items.size());
        assertEquals(new BigDecimal("200.00"), created.subtotal); // 100 + (50*2)
        assertTrue(created.totalAmount.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @Order(3)
    @Transactional
    void testGetInvoiceById() {
        // Given
        Invoice invoice = createTestInvoice();
        Invoice created = invoiceService.createInvoice(invoice);

        // When
        Invoice found = invoiceService.getInvoiceById(created.id);

        // Then
        assertNotNull(found);
        assertEquals(created.id, found.id);
        assertEquals(created.invoiceNumber, found.invoiceNumber);
    }

    @Test
    @Order(4)
    void testGetInvoiceById_NotFound() {
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            invoiceService.getInvoiceById(99999L);
        });
    }

    @Test
    @Order(5)
    @Transactional
    void testGetInvoiceByNumber() {
        // Given
        Invoice invoice = createTestInvoice();
        Invoice created = invoiceService.createInvoice(invoice);

        // When
        Invoice found = invoiceService.getInvoiceByNumber(created.invoiceNumber);

        // Then
        assertNotNull(found);
        assertEquals(created.invoiceNumber, found.invoiceNumber);
    }

    @Test
    @Order(6)
    @Transactional
    void testUpdateInvoice() {
        // Given
        Invoice invoice = createTestInvoice();
        Invoice created = invoiceService.createInvoice(invoice);

        Invoice updateData = new Invoice();
        updateData.notes = "Updated notes";
        updateData.taxAmount = new BigDecimal("15.00");

        // When
        Invoice updated = invoiceService.updateInvoice(created.id, updateData);

        // Then
        assertEquals("Updated notes", updated.notes);
        assertEquals(new BigDecimal("15.00"), updated.taxAmount);
        assertNotNull(updated.updatedAt);
    }

    @Test
    @Order(7)
    @Transactional
    void testAddItemToInvoice() {
        // Given
        Invoice invoice = createTestInvoice();
        Invoice created = invoiceService.createInvoice(invoice);
        InvoiceItem newItem = createInvoiceItem("X-Ray", new BigDecimal("75.00"), 1);

        // When
        Invoice updated = invoiceService.addItemToInvoice(created.id, newItem);

        // Then
        assertTrue(updated.items.size() > 0);
        assertEquals(new BigDecimal("75.00"), updated.subtotal);
    }

    @Test
    @Order(8)
    @Transactional
    void testAddItemToNonDraftInvoice_ShouldFail() {
        // Given
        Invoice invoice = createTestInvoice();
        Invoice created = invoiceService.createInvoice(invoice);
        created.status = InvoiceStatus.ISSUED;
        invoiceRepository.persist(created);

        InvoiceItem newItem = createInvoiceItem("Test", new BigDecimal("50.00"), 1);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            invoiceService.addItemToInvoice(created.id, newItem);
        });
    }

    @Test
    @Order(9)
    @Transactional
    void testIssueInvoice() {
        // Given
        Invoice invoice = createTestInvoice();
        InvoiceItem item = createInvoiceItem("Service", new BigDecimal("100.00"), 1);
        invoice.addItem(item);
        Invoice created = invoiceService.createInvoice(invoice);

        // When
        Invoice issued = invoiceService.issueInvoice(created.id);

        // Then
        assertEquals(InvoiceStatus.ISSUED, issued.status);
    }

    @Test
    @Order(10)
    @Transactional
    void testIssueInvoiceWithoutItems_ShouldFail() {
        // Given
        Invoice invoice = createTestInvoice();
        Invoice created = invoiceService.createInvoice(invoice);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            invoiceService.issueInvoice(created.id);
        });
    }

    @Test
    @Order(11)
    @Transactional
    void testRecordPayment() {
        // Given
        Invoice invoice = createTestInvoice();
        InvoiceItem item = createInvoiceItem("Service", new BigDecimal("100.00"), 1);
        invoice.addItem(item);
        invoice.status = InvoiceStatus.ISSUED;
        Invoice created = invoiceService.createInvoice(invoice);

        BigDecimal paymentAmount = new BigDecimal("50.00");

        // When
        Invoice updated = invoiceService.recordPayment(created.id, paymentAmount);

        // Then
        assertEquals(new BigDecimal("50.00"), updated.amountPaid);
        assertEquals(InvoiceStatus.PARTIALLY_PAID, updated.status);
    }

    @Test
    @Order(12)
    @Transactional
    void testCancelInvoice() {
        // Given
        Invoice invoice = createTestInvoice();
        Invoice created = invoiceService.createInvoice(invoice);

        // When
        Invoice cancelled = invoiceService.cancelInvoice(created.id, "Customer request");

        // Then
        assertEquals(InvoiceStatus.CANCELLED, cancelled.status);
        assertTrue(cancelled.notes.contains("CANCELLED"));
    }

    @Test
    @Order(13)
    @Transactional
    void testDeleteDraftInvoice() {
        // Given
        Invoice invoice = createTestInvoice();
        Invoice created = invoiceService.createInvoice(invoice);

        // When
        invoiceService.deleteInvoice(created.id);

        // Then
        assertFalse(invoiceRepository.findByIdOptional(created.id).isPresent());
    }

    @Test
    @Order(14)
    @Transactional
    void testDeleteNonDraftInvoice_ShouldFail() {
        // Given
        Invoice invoice = createTestInvoice();
        Invoice created = invoiceService.createInvoice(invoice);
        created.status = InvoiceStatus.ISSUED;
        invoiceRepository.persist(created);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            invoiceService.deleteInvoice(created.id);
        });
    }

    @Test
    @Order(15)
    @Transactional
    void testCalculateOutstandingAmount() {
        // Given
        Long patientId = 1L;
        Invoice invoice1 = createTestInvoice();
        invoice1.patientId = patientId;
        InvoiceItem item = createInvoiceItem("Service", new BigDecimal("100.00"), 1);
        invoice1.addItem(item);
        invoiceService.createInvoice(invoice1);

        // When
        BigDecimal outstanding = invoiceService.calculateOutstandingAmount(patientId);

        // Then
        assertTrue(outstanding.compareTo(BigDecimal.ZERO) > 0);
    }

    // Helper methods - NOT @Transactional (not private anymore)
    Invoice createTestInvoice() {
        Invoice invoice = new Invoice();
        invoice.appointmentId = 1L;
        invoice.patientId = 1L;
        invoice.doctorId = 1L;
        invoice.invoiceNumber = "INV-" + System.currentTimeMillis();
        invoice.issueDate = LocalDate.now();
        invoice.dueDate = LocalDate.now().plusDays(30);
        invoice.taxAmount = new BigDecimal("10.00");
        invoice.discountAmount = BigDecimal.ZERO;
        return invoice;
    }

    InvoiceItem createInvoiceItem(String description, BigDecimal unitPrice, int quantity) {
        InvoiceItem item = new InvoiceItem();
        item.description = description;
        item.serviceCode = "SVC-001";
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        return item;
    }
}