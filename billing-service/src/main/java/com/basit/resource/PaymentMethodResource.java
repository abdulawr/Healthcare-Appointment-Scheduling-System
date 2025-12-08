package com.basit.resource;

import com.basit.dto.request.PaymentMethodRequest;
import com.basit.dto.response.PaymentMethodResponse;
import com.basit.service.PaymentMethodService;
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

@Path("/api/billing/payment-methods")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Payment Methods", description = "Payment method management operations")
public class PaymentMethodResource {

    @Inject
    PaymentMethodService paymentMethodService;

    @POST
    @Operation(summary = "Save payment method", description = "Save a new payment method for a patient")
    @APIResponse(responseCode = "201", description = "Payment method saved successfully")
    @APIResponse(responseCode = "400", description = "Invalid request")
    public Response savePaymentMethod(@Valid PaymentMethodRequest request) {
        PaymentMethodResponse response = paymentMethodService.savePaymentMethod(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update payment method", description = "Update an existing payment method")
    @APIResponse(responseCode = "200", description = "Payment method updated successfully")
    @APIResponse(responseCode = "400", description = "Invalid request")
    @APIResponse(responseCode = "404", description = "Payment method not found")
    public Response updatePaymentMethod(
            @Parameter(description = "Payment Method ID", required = true)
            @PathParam("id") Long id,
            @Valid PaymentMethodRequest request) {
        PaymentMethodResponse response = paymentMethodService.updatePaymentMethod(id, request);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete payment method", description = "Delete (deactivate) a payment method")
    @APIResponse(responseCode = "204", description = "Payment method deleted successfully")
    @APIResponse(responseCode = "404", description = "Payment method not found")
    public Response deletePaymentMethod(
            @Parameter(description = "Payment Method ID", required = true)
            @PathParam("id") Long id) {
        paymentMethodService.deletePaymentMethod(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/set-default")
    @Operation(summary = "Set as default", description = "Set a payment method as default for a patient")
    @APIResponse(responseCode = "200", description = "Payment method set as default")
    @APIResponse(responseCode = "400", description = "Invalid request")
    @APIResponse(responseCode = "404", description = "Payment method not found")
    public Response setAsDefault(
            @Parameter(description = "Payment Method ID", required = true)
            @PathParam("id") Long id,
            @Parameter(description = "Patient ID", required = true)
            @QueryParam("patientId") Long patientId) {
        PaymentMethodResponse response = paymentMethodService.setAsDefault(id, patientId);
        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get payment method", description = "Retrieve payment method by ID")
    @APIResponse(responseCode = "200", description = "Payment method found")
    @APIResponse(responseCode = "404", description = "Payment method not found")
    public Response getPaymentMethod(
            @Parameter(description = "Payment Method ID", required = true)
            @PathParam("id") Long id) {
        PaymentMethodResponse response = paymentMethodService.getPaymentMethod(id);
        return Response.ok(response).build();
    }

    @GET
    @Path("/patient/{patientId}")
    @Operation(summary = "Get patient payment methods", description = "Get all payment methods for a patient")
    @APIResponse(responseCode = "200", description = "Payment methods retrieved successfully")
    public Response getPatientPaymentMethods(
            @Parameter(description = "Patient ID", required = true)
            @PathParam("patientId") Long patientId) {
        List<PaymentMethodResponse> responses = paymentMethodService.getPatientPaymentMethods(patientId);
        return Response.ok(responses).build();
    }

    @GET
    @Path("/patient/{patientId}/default")
    @Operation(summary = "Get default payment method", description = "Get the default payment method for a patient")
    @APIResponse(responseCode = "200", description = "Default payment method found")
    @APIResponse(responseCode = "404", description = "No default payment method found")
    public Response getDefaultPaymentMethod(
            @Parameter(description = "Patient ID", required = true)
            @PathParam("patientId") Long patientId) {
        PaymentMethodResponse response = paymentMethodService.getDefaultPaymentMethod(patientId);
        return Response.ok(response).build();
    }

    @GET
    @Path("/token/{token}")
    @Operation(summary = "Get payment method by token", description = "Retrieve payment method by token")
    @APIResponse(responseCode = "200", description = "Payment method found")
    @APIResponse(responseCode = "404", description = "Payment method not found")
    public Response getPaymentMethodByToken(
            @Parameter(description = "Payment Token", required = true)
            @PathParam("token") String token) {
        PaymentMethodResponse response = paymentMethodService.getPaymentMethodByToken(token);
        return Response.ok(response).build();
    }
}
