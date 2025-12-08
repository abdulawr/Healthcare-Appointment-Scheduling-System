package com.basit.mapper;

import com.basit.dto.request.InvoiceCreateRequest;
import com.basit.dto.response.InvoiceResponse;
import com.basit.entity.Invoice;
import com.basit.entity.InvoiceItem;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@ApplicationScoped
public class InvoiceMapper {

    public Invoice toEntity(InvoiceCreateRequest request) {
        Invoice invoice = new Invoice();
        invoice.appointmentId = request.appointmentId;
        invoice.patientId = request.patientId;
        invoice.notes = request.notes;

        BigDecimal subtotal = BigDecimal.ZERO;
        for (InvoiceCreateRequest.InvoiceItemRequest itemReq : request.items) {
            InvoiceItem item = new InvoiceItem();
            item.invoice = invoice;
            item.description = itemReq.description;
            item.quantity = itemReq.quantity;
            item.unitPrice = itemReq.unitPrice;
            invoice.items.add(item);
            subtotal = subtotal.add(itemReq.unitPrice.multiply(
                    BigDecimal.valueOf(itemReq.quantity)));
        }

        invoice.subtotal = subtotal;
        invoice.tax = subtotal.multiply(new BigDecimal("0.10")); // 10% tax
        invoice.total = invoice.subtotal.add(invoice.tax);

        return invoice;
    }

    public InvoiceResponse toResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.id = invoice.id;
        response.appointmentId = invoice.appointmentId;
        response.patientId = invoice.patientId;
        response.subtotal = invoice.subtotal;
        response.tax = invoice.tax;
        response.total = invoice.total;
        response.amountPaid = invoice.amountPaid;
        response.balance = invoice.balance;
        response.status = invoice.status;
        response.issueDate = invoice.issueDate;
        response.dueDate = invoice.dueDate;
        response.paidDate = invoice.paidDate;
        response.notes = invoice.notes;

        response.items = invoice.items.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return response;
    }

    private InvoiceResponse.InvoiceItemResponse toItemResponse(InvoiceItem item) {
        InvoiceResponse.InvoiceItemResponse response =
                new InvoiceResponse.InvoiceItemResponse();
        response.id = item.id;
        response.description = item.description;
        response.quantity = item.quantity;
        response.unitPrice = item.unitPrice;
        response.amount = item.amount;
        return response;
    }
}
