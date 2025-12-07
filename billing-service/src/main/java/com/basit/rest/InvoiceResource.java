package com.basit.rest;

import com.basit.dto.request.AddInvoiceItemRequest;
import com.basit.dto.request.CreateInvoiceRequest;
import com.basit.dto.request.UpdateInvoiceRequest;
import com.basit.dto.response.InvoiceResponse;
import com.basit.entity.Invoice;
import com.basit.entity.InvoiceItem;
import com.basit.mapper.InvoiceMapper;
import com.basit.service.InvoiceService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/billing/invoices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InvoiceResource {

    @Inject
    InvoiceService invoiceService;

    @Inject
    InvoiceMapper invoiceMapper;

    @POST
    public Response createInvoice(@Valid CreateInvoiceRequest request) {
        Invoice invoice = invoiceMapper.toEntity(request);
        Invoice created = invoiceService.createInvoice(invoice);
        InvoiceResponse response = invoiceMapper.toResponse(created);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{id}")
    public Response getInvoiceById(@PathParam("id") Long id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        InvoiceResponse response = invoiceMapper.toResponse(invoice);
        return Response.ok(response).build();
    }

    @GET
    @Path("/number/{invoiceNumber}")
    public Response getInvoiceByNumber(@PathParam("invoiceNumber") String invoiceNumber) {
        Invoice invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
        InvoiceResponse response = invoiceMapper.toResponse(invoice);
        return Response.ok(response).build();
    }

    @GET
    @Path("/patient/{patientId}")
    public Response getInvoicesByPatient(@PathParam("patientId") Long patientId,
                                         @QueryParam("page") @DefaultValue("0") int page,
                                         @QueryParam("size") @DefaultValue("20") int size) {
        List<Invoice> invoices = invoiceService.getInvoicesByPatientId(patientId, page, size);
        List<InvoiceResponse> responses = invoices.stream()
                .map(invoiceMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @GET
    @Path("/overdue")
    public Response getOverdueInvoices() {
        List<Invoice> invoices = invoiceService.getOverdueInvoices();
        List<InvoiceResponse> responses = invoices.stream()
                .map(invoiceMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateInvoice(@PathParam("id") Long id, @Valid UpdateInvoiceRequest request) {
        Invoice updateData = new Invoice();
        updateData.dueDate = request.dueDate;
        updateData.taxAmount = request.taxAmount;
        updateData.discountAmount = request.discountAmount;
        updateData.notes = request.notes;

        Invoice updated = invoiceService.updateInvoice(id, updateData);
        InvoiceResponse response = invoiceMapper.toResponse(updated);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/items")
    public Response addItemToInvoice(@PathParam("id") Long id, @Valid AddInvoiceItemRequest request) {
        InvoiceItem item = new InvoiceItem();
        item.description = request.description;
        item.serviceCode = request.serviceCode;
        item.quantity = request.quantity;
        item.unitPrice = request.unitPrice;

        Invoice invoice = invoiceService.addItemToInvoice(id, item);
        InvoiceResponse response = invoiceMapper.toResponse(invoice);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}/items/{itemId}")
    public Response removeItemFromInvoice(@PathParam("id") Long id, @PathParam("itemId") Long itemId) {
        Invoice invoice = invoiceService.removeItemFromInvoice(id, itemId);
        InvoiceResponse response = invoiceMapper.toResponse(invoice);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/issue")
    public Response issueInvoice(@PathParam("id") Long id) {
        Invoice invoice = invoiceService.issueInvoice(id);
        InvoiceResponse response = invoiceMapper.toResponse(invoice);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/cancel")
    public Response cancelInvoice(@PathParam("id") Long id, @QueryParam("reason") String reason) {
        Invoice invoice = invoiceService.cancelInvoice(id, reason);
        InvoiceResponse response = invoiceMapper.toResponse(invoice);
        return Response.ok(response).build();
    }

    @GET
    @Path("/patient/{patientId}/outstanding")
    public Response getOutstandingAmount(@PathParam("patientId") Long patientId) {
        BigDecimal amount = invoiceService.calculateOutstandingAmount(patientId);
        return Response.ok().entity(new OutstandingResponse(amount)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteInvoice(@PathParam("id") Long id) {
        invoiceService.deleteInvoice(id);
        return Response.noContent().build();
    }

    public static class OutstandingResponse {
        public BigDecimal outstandingAmount;
        public OutstandingResponse(BigDecimal amount) {
            this.outstandingAmount = amount;
        }
    }
}
