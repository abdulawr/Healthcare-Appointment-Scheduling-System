package com.basit.resource;

import com.basit.dto.request.PaymentProcessRequest;
import com.basit.dto.response.PaymentResponse;
import com.basit.service.PaymentService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/billing/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Payment Processing", description = "Payment operations")
public class PaymentResource {

    @Inject
    PaymentService paymentService;

    @POST
    @Operation(summary = "Process payment")
    public Response processPayment(@Valid PaymentProcessRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get payment details")
    public Response getPayment(@PathParam("id") Long id) {
        PaymentResponse response = paymentService.getPayment(id);
        return Response.ok(response).build();
    }

    // Additional endpoints...
}
