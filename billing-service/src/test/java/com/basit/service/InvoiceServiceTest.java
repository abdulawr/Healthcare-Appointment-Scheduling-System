package com.basit.service;

import com.basit.dto.request.InvoiceCreateRequest;
import com.basit.dto.response.InvoiceResponse;
import com.basit.entity.Invoice;
import com.basit.repository.InvoiceRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class InvoiceServiceTest {

    @Inject
    InvoiceService invoiceService;

    @Inject
    InvoiceRepository invoiceRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        invoiceRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testCreateInvoice() {
        InvoiceCreateRequest request = createTestRequest();

        InvoiceResponse response = invoiceService.createInvoice(request);

        assertNotNull(response);
        assertNotNull(response.id);
        assertEquals(1L, response.appointmentId);
        assertEquals(1L, response.patientId);
    }

    @Test
    @Transactional
    public void testCreateDuplicateInvoice() {
        InvoiceCreateRequest request = createTestRequest();

        invoiceService.createInvoice(request);

        assertThrows(IllegalArgumentException.class, () -> {
            invoiceService.createInvoice(request);
        });
    }

    @Test
    @Transactional
    public void testGetInvoice() {
        InvoiceCreateRequest request = createTestRequest();
        InvoiceResponse created = invoiceService.createInvoice(request);

        InvoiceResponse retrieved = invoiceService.getInvoice(created.id);

        assertNotNull(retrieved);
        assertEquals(created.id, retrieved.id);
    }

    private InvoiceCreateRequest createTestRequest() {
        InvoiceCreateRequest request = new InvoiceCreateRequest();
        request.appointmentId = 1L;
        request.patientId = 1L;

        List<InvoiceCreateRequest.InvoiceItemRequest> items = new ArrayList<>();
        InvoiceCreateRequest.InvoiceItemRequest item =
                new InvoiceCreateRequest.InvoiceItemRequest();
        item.description = "Consultation";
        item.quantity = 1;
        item.unitPrice = new BigDecimal("100.00");
        items.add(item);

        request.items = items;
        return request;
    }
}
