package com.basit.service;

import com.basit.dto.request.InvoiceCreateRequest;
import com.basit.dto.response.InvoiceResponse;
import com.basit.entity.Invoice;
import com.basit.constant.InvoiceStatus;
import com.basit.event.BillingEventProducer;
import com.basit.event.InvoiceCreatedEvent;
import com.basit.mapper.InvoiceMapper;
import com.basit.repository.InvoiceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class InvoiceService {

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    InvoiceMapper invoiceMapper;

    @Inject
    BillingEventProducer eventProducer;

    @Transactional
    public InvoiceResponse createInvoice(InvoiceCreateRequest request) {
        // Check if invoice already exists for appointment
        Invoice existing = invoiceRepository.findByAppointmentId(
                request.appointmentId);
        if (existing != null) {
            throw new IllegalArgumentException(
                    "Invoice already exists for appointment: " + request.appointmentId);
        }

        Invoice invoice = invoiceMapper.toEntity(request);
        invoiceRepository.persist(invoice);

        InvoiceCreatedEvent event = new InvoiceCreatedEvent();
        event.invoiceId = invoice.id;
        event.appointmentId = invoice.appointmentId;
        event.patientId = invoice.patientId;
        event.subtotal = invoice.subtotal;
        event.tax = invoice.tax;
        event.total = invoice.total;
        event.status = invoice.status;
        event.issueDate = invoice.issueDate;
        event.dueDate = invoice.dueDate;
        event.notes = invoice.notes;

        eventProducer.publishInvoiceCreated(event);

        return invoiceMapper.toResponse(invoice);
    }

    public InvoiceResponse getInvoice(Long id) {
        Invoice invoice = invoiceRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException(
                        "Invoice not found with id: " + id));
        return invoiceMapper.toResponse(invoice);
    }

    public List<InvoiceResponse> getPatientInvoices(Long patientId) {
        return invoiceRepository.findByPatientId(patientId).stream()
                .map(invoiceMapper::toResponse)
                .collect(Collectors.toList());
    }

    public InvoiceResponse getInvoiceByAppointment(Long appointmentId) {
        Invoice invoice = invoiceRepository.findByAppointmentId(appointmentId);
        if (invoice == null) {
            throw new NotFoundException(
                    "Invoice not found for appointment: " + appointmentId);
        }
        return invoiceMapper.toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse updateInvoice(Long id, InvoiceCreateRequest request) {
        Invoice invoice = invoiceRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException(
                        "Invoice not found with id: " + id));

        if (invoice.status != InvoiceStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot update invoice with status: " + invoice.status);
        }

        // Update logic here
        invoice.notes = request.notes;
        invoiceRepository.persist(invoice);

        return invoiceMapper.toResponse(invoice);
    }

    @Transactional
    public void cancelInvoice(Long id) {
        Invoice invoice = invoiceRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException(
                        "Invoice not found with id: " + id));

        if (invoice.amountPaid.compareTo(invoice.total) == 0) {
            throw new IllegalStateException(
                    "Cannot cancel fully paid invoice");
        }

        invoice.status = InvoiceStatus.CANCELLED;
        invoiceRepository.persist(invoice);
    }
}
