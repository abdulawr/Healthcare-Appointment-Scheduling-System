package com.basit.resource;

import com.basit.dto.request.InvoiceCreateRequest;
import com.basit.dto.response.InvoiceResponse;
import com.basit.service.InvoiceService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.List;

@Path("/api/billing/invoices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Invoice Management", description = "Invoice operations")
public class InvoiceResource {

    @Inject
    InvoiceService invoiceService;

    @POST
    @Operation(summary = "Create invoice for appointment")
    public Response createInvoice(@Valid InvoiceCreateRequest request) {
        InvoiceResponse response = invoiceService.createInvoice(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get invoice details")
    public Response getInvoice(@PathParam("id") Long id) {
        InvoiceResponse response = invoiceService.getInvoice(id);
        return Response.ok(response).build();
    }

    @GET
    @Path("/patient/{patientId}")
    @Operation(summary = "Get patient invoices")
    public Response getPatientInvoices(@PathParam("patientId") Long patientId) {
        List<InvoiceResponse> responses =
                invoiceService.getPatientInvoices(patientId);
        return Response.ok(responses).build();
    }

    @GET
    @Path("/appointment/{appointmentId}")
    @Operation(summary = "Get invoice by appointment")
    public Response getInvoiceByAppointment(
            @PathParam("appointmentId") Long appointmentId) {
        InvoiceResponse response =
                invoiceService.getInvoiceByAppointment(appointmentId);
        return Response.ok(response).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update invoice")
    public Response updateInvoice(
            @PathParam("id") Long id,
            @Valid InvoiceCreateRequest request) {
        InvoiceResponse response = invoiceService.updateInvoice(id, request);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Cancel invoice")
    public Response cancelInvoice(@PathParam("id") Long id) {
        invoiceService.cancelInvoice(id);
        return Response.noContent().build();
    }
}
