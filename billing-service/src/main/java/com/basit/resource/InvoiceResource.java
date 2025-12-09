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

        // Call the service to fetch invoice response
        InvoiceResponse response = invoiceService.getInvoiceByAppointment(appointmentId);

        // If the response is not null (invoice found), return 200 OK with the response
        if (response != null) {
            return Response.ok(response).build();
        }

        // If no invoice is found (response is null), return 204 No Content (empty response)
        return Response.status(Response.Status.NO_CONTENT).build();
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
