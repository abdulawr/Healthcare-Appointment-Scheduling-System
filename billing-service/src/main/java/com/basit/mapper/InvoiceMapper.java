package com.basit.mapper;
import com.basit.dto.request.CreateInvoiceRequest;
import com.basit.dto.response.InvoiceItemResponse;
import com.basit.dto.response.InvoiceResponse;
import com.basit.entity.Invoice;
import com.basit.entity.InvoiceItem;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.stream.Collectors;

@ApplicationScoped
public class InvoiceMapper {

    public Invoice toEntity(CreateInvoiceRequest request) {
        Invoice invoice = new Invoice();
        invoice.appointmentId = request.appointmentId;
        invoice.patientId = request.patientId;
        invoice.doctorId = request.doctorId;
        invoice.invoiceNumber = request.invoiceNumber;
        invoice.issueDate = request.issueDate;
        invoice.dueDate = request.dueDate;
        invoice.taxAmount = request.taxAmount;
        invoice.discountAmount = request.discountAmount != null ? request.discountAmount : java.math.BigDecimal.ZERO;
        invoice.notes = request.notes;

        if (request.items != null && !request.items.isEmpty()) {
            for (CreateInvoiceRequest.InvoiceItemRequest itemReq : request.items) {
                InvoiceItem item = new InvoiceItem();
                item.description = itemReq.description;
                item.serviceCode = itemReq.serviceCode;
                item.quantity = itemReq.quantity;
                item.unitPrice = itemReq.unitPrice;
                invoice.addItem(item);
            }
        }

        return invoice;
    }

    public InvoiceResponse toResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.id = invoice.id;
        response.appointmentId = invoice.appointmentId;
        response.patientId = invoice.patientId;
        response.doctorId = invoice.doctorId;
        response.invoiceNumber = invoice.invoiceNumber;
        response.issueDate = invoice.issueDate;
        response.dueDate = invoice.dueDate;
        response.subtotal = invoice.subtotal;
        response.taxAmount = invoice.taxAmount;
        response.discountAmount = invoice.discountAmount;
        response.totalAmount = invoice.totalAmount;
        response.amountPaid = invoice.amountPaid;
        response.amountDue = invoice.amountDue;
        response.status = invoice.status;
        response.notes = invoice.notes;
        response.insuranceClaimId = invoice.insuranceClaimId;
        response.createdAt = invoice.createdAt;
        response.updatedAt = invoice.updatedAt;

        if (invoice.items != null) {
            response.items = invoice.items.stream()
                    .map(this::toItemResponse)
                    .collect(Collectors.toList());
        }

        return response;
    }

    public InvoiceItemResponse toItemResponse(InvoiceItem item) {
        InvoiceItemResponse response = new InvoiceItemResponse();
        response.id = item.id;
        response.description = item.description;
        response.serviceCode = item.serviceCode;
        response.quantity = item.quantity;
        response.unitPrice = item.unitPrice;
        response.totalPrice = item.totalPrice;
        return response;
    }
}
