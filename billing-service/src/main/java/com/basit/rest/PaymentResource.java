package com.basit.rest;

import com.basit.dto.request.ProcessPaymentRequest;
import com.basit.dto.response.PaymentResponse;
import com.basit.entity.Payment;
import com.basit.mapper.PaymentMapper;
import com.basit.service.PaymentService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/billing/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    @Inject
    PaymentService paymentService;

    @Inject
    PaymentMapper paymentMapper;

    @POST
    public Response processPayment(@Valid ProcessPaymentRequest request) {
        Payment payment = paymentMapper.toEntity(request);
        Payment processed = paymentService.processPayment(payment);
        PaymentResponse response = paymentMapper.toResponse(processed);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{id}")
    public Response getPaymentById(@PathParam("id") Long id) {
        Payment payment = paymentService.getPaymentById(id);
        PaymentResponse response = paymentMapper.toResponse(payment);
        return Response.ok(response).build();
    }

    @GET
    @Path("/transaction/{transactionId}")
    public Response getPaymentByTransactionId(@PathParam("transactionId") String transactionId) {
        Payment payment = paymentService.getPaymentByTransactionId(transactionId);
        PaymentResponse response = paymentMapper.toResponse(payment);
        return Response.ok(response).build();
    }

    @GET
    @Path("/invoice/{invoiceId}")
    public Response getPaymentsByInvoice(@PathParam("invoiceId") Long invoiceId) {
        List<Payment> payments = paymentService.getPaymentsByInvoiceId(invoiceId);
        List<PaymentResponse> responses = payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @GET
    @Path("/patient/{patientId}")
    public Response getPaymentsByPatient(@PathParam("patientId") Long patientId) {
        List<Payment> payments = paymentService.getPaymentsByPatientId(patientId);
        List<PaymentResponse> responses = payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @GET
    @Path("/successful")
    public Response getSuccessfulPayments() {
        List<Payment> payments = paymentService.getSuccessfulPayments();
        List<PaymentResponse> responses = payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @GET
    @Path("/failed")
    public Response getFailedPayments() {
        List<Payment> payments = paymentService.getFailedPayments();
        List<PaymentResponse> responses = payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @GET
    @Path("/pending")
    public Response getPendingPayments() {
        List<Payment> payments = paymentService.getPendingPayments();
        List<PaymentResponse> responses = payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @POST
    @Path("/{id}/retry")
    public Response retryPayment(@PathParam("id") Long id) {
        Payment payment = paymentService.retryPayment(id);
        PaymentResponse response = paymentMapper.toResponse(payment);
        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}/refundable")
    public Response canRefund(@PathParam("id") Long id) {
        boolean canRefund = paymentService.canRefund(id);
        return Response.ok().entity(new RefundableResponse(canRefund)).build();
    }

    public static class RefundableResponse {
        public boolean canRefund;
        public RefundableResponse(boolean canRefund) {
            this.canRefund = canRefund;
        }
    }
}

