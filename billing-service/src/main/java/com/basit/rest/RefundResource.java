package com.basit.rest;

import com.basit.dto.request.CreateRefundRequest;
import com.basit.dto.response.RefundResponse;
import com.basit.entity.Refund;
import com.basit.service.RefundService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/billing/refunds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RefundResource {

    @Inject
    RefundService refundService;

    @POST
    public Response createRefund(@Valid CreateRefundRequest request) {
        Refund refund = new Refund();
        refund.paymentId = request.paymentId;
        refund.refundAmount = request.refundAmount;
        refund.reason = request.reason;
        refund.requestedBy = request.requestedBy;
        refund.notes = request.notes;

        Refund created = refundService.createRefund(refund);
        RefundResponse response = toResponse(created);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{id}")
    public Response getRefundById(@PathParam("id") Long id) {
        Refund refund = refundService.getRefundById(id);
        RefundResponse response = toResponse(refund);
        return Response.ok(response).build();
    }

    @GET
    @Path("/payment/{paymentId}")
    public Response getRefundsByPayment(@PathParam("paymentId") Long paymentId) {
        List<Refund> refunds = refundService.getRefundsByPaymentId(paymentId);
        List<RefundResponse> responses = refunds.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @GET
    @Path("/patient/{patientId}")
    public Response getRefundsByPatient(@PathParam("patientId") Long patientId) {
        List<Refund> refunds = refundService.getRefundsByPatientId(patientId);
        List<RefundResponse> responses = refunds.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @GET
    @Path("/pending")
    public Response getPendingRefunds() {
        List<Refund> refunds = refundService.getPendingRefunds();
        List<RefundResponse> responses = refunds.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @GET
    @Path("/requiring-approval")
    public Response getRefundsRequiringApproval() {
        List<Refund> refunds = refundService.getRefundsRequiringApproval();
        List<RefundResponse> responses = refunds.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @POST
    @Path("/{id}/approve")
    public Response approveRefund(@PathParam("id") Long id, @QueryParam("approvedBy") String approvedBy) {
        Refund refund = refundService.approveRefund(id, approvedBy);
        RefundResponse response = toResponse(refund);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/process")
    public Response processRefund(@PathParam("id") Long id) {
        Refund refund = refundService.processRefund(id);
        RefundResponse response = toResponse(refund);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/cancel")
    public Response cancelRefund(@PathParam("id") Long id, @QueryParam("reason") String reason) {
        Refund refund = refundService.cancelRefund(id, reason);
        RefundResponse response = toResponse(refund);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/retry")
    public Response retryRefund(@PathParam("id") Long id) {
        Refund refund = refundService.retryRefund(id);
        RefundResponse response = toResponse(refund);
        return Response.ok(response).build();
    }

    private RefundResponse toResponse(Refund refund) {
        RefundResponse response = new RefundResponse();
        response.id = refund.id;
        response.paymentId = refund.paymentId;
        response.invoiceId = refund.invoiceId;
        response.patientId = refund.patientId;
        response.refundAmount = refund.refundAmount;
        response.status = refund.status;
        response.reason = refund.reason;
        response.refundTransactionId = refund.refundTransactionId;
        response.paymentGateway = refund.paymentGateway;
        response.processedAt = refund.processedAt;
        response.failedReason = refund.failedReason;
        response.requestedBy = refund.requestedBy;
        response.approvedBy = refund.approvedBy;
        response.approvedAt = refund.approvedAt;
        response.notes = refund.notes;
        response.createdAt = refund.createdAt;
        response.updatedAt = refund.updatedAt;
        return response;
    }
}

