package com.basit.resource;

import com.basit.dto.request.RefundRequest;
import com.basit.dto.response.RefundResponse;
import com.basit.service.RefundService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.List;

@Path("/api/billing/refunds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Refund Management", description = "Refund processing operations")
public class RefundResource {

    @Inject
    RefundService refundService;

    @POST
    @Operation(summary = "Process refund", description = "Process a refund for a payment")
    @APIResponse(responseCode = "201", description = "Refund processed successfully")
    @APIResponse(responseCode = "400", description = "Invalid request")
    @APIResponse(responseCode = "404", description = "Payment not found")
    public Response processRefund(@Valid RefundRequest request) {
        RefundResponse response = refundService.processRefund(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get refund details", description = "Retrieve refund by ID")
    @APIResponse(responseCode = "200", description = "Refund found")
    @APIResponse(responseCode = "404", description = "Refund not found")
    public Response getRefund(
            @Parameter(description = "Refund ID", required = true)
            @PathParam("id") Long id) {
        RefundResponse response = refundService.getRefund(id);
        return Response.ok(response).build();
    }

    @GET
    @Path("/payment/{paymentId}")
    @Operation(summary = "Get refunds by payment", description = "Get all refunds for a specific payment")
    @APIResponse(responseCode = "200", description = "Refunds retrieved successfully")
    public Response getRefundsByPayment(
            @Parameter(description = "Payment ID", required = true)
            @PathParam("paymentId") Long paymentId) {
        List<RefundResponse> responses = refundService.getRefundsByPayment(paymentId);
        return Response.ok(responses).build();
    }

    @GET
    @Path("/invoice/{invoiceId}")
    @Operation(summary = "Get refunds by invoice", description = "Get all refunds for a specific invoice")
    @APIResponse(responseCode = "200", description = "Refunds retrieved successfully")
    public Response getRefundsByInvoice(
            @Parameter(description = "Invoice ID", required = true)
            @PathParam("invoiceId") Long invoiceId) {
        List<RefundResponse> responses = refundService.getRefundsByInvoice(invoiceId);
        return Response.ok(responses).build();
    }

    @GET
    @Path("/patient/{patientId}")
    @Operation(summary = "Get refunds by patient", description = "Get all refunds for a specific patient")
    @APIResponse(responseCode = "200", description = "Refunds retrieved successfully")
    public Response getRefundsByPatient(
            @Parameter(description = "Patient ID", required = true)
            @PathParam("patientId") Long patientId) {
        List<RefundResponse> responses = refundService.getRefundsByPatient(patientId);
        return Response.ok(responses).build();
    }

    @GET
    @Path("/transaction/{refundTransactionId}")
    @Operation(summary = "Get refund by transaction ID", description = "Retrieve refund by transaction ID")
    @APIResponse(responseCode = "200", description = "Refund found")
    @APIResponse(responseCode = "404", description = "Refund not found")
    public Response getRefundByTransactionId(
            @Parameter(description = "Refund Transaction ID", required = true)
            @PathParam("refundTransactionId") String refundTransactionId) {
        RefundResponse response = refundService.getRefundByTransactionId(refundTransactionId);
        return Response.ok(response).build();
    }
}
