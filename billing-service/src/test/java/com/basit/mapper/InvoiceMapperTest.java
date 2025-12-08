package com.basit.mapper;

import com.basit.dto.request.InvoiceCreateRequest;
import com.basit.dto.response.InvoiceResponse;
import com.basit.entity.Invoice;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class InvoiceMapperTest {

    @Inject
    InvoiceMapper invoiceMapper;

    @Test
    public void testToEntity() {
        InvoiceCreateRequest request = new InvoiceCreateRequest();
        request.appointmentId = 1L;
        request.patientId = 1L;
        request.notes = "Test invoice";

        List<InvoiceCreateRequest.InvoiceItemRequest> items = new ArrayList<>();
        InvoiceCreateRequest.InvoiceItemRequest item1 =
                new InvoiceCreateRequest.InvoiceItemRequest();
        item1.description = "Consultation";
        item1.quantity = 1;
        item1.unitPrice = new BigDecimal("100.00");
        items.add(item1);

        request.items = items;

        Invoice invoice = invoiceMapper.toEntity(request);

        assertNotNull(invoice);
        assertEquals(1L, invoice.appointmentId);
        assertEquals(1L, invoice.patientId);
        assertEquals(new BigDecimal("100.00"), invoice.subtotal);
        assertEquals(1, invoice.items.size());
    }

    @Test
    public void testToResponse() {
        Invoice invoice = new Invoice();
        invoice.id = 1L;
        invoice.appointmentId = 1L;
        invoice.patientId = 1L;
        invoice.subtotal = new BigDecimal("100.00");
        invoice.tax = new BigDecimal("10.00");
        invoice.total = new BigDecimal("110.00");

        InvoiceResponse response = invoiceMapper.toResponse(invoice);

        assertNotNull(response);
        assertEquals(1L, response.id);
        assertEquals(new BigDecimal("110.00"), response.total);
    }
}
