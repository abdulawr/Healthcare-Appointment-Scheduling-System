package com.basit.rest;

import com.basit.dto.request.SavePaymentMethodRequest;
import com.basit.dto.response.PaymentMethodResponse;
import com.basit.entity.PaymentMethod;
import com.basit.service.PaymentMethodService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/billing/payment-methods")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentMethodResource {

    @Inject
    PaymentMethodService paymentMethodService;

    @POST
    public Response savePaymentMethod(@Valid SavePaymentMethodRequest request) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.patientId = request.patientId;
        paymentMethod.paymentType = request.paymentType;
        paymentMethod.paymentToken = request.paymentToken;
        paymentMethod.paymentGateway = request.paymentGateway;
        paymentMethod.cardLastFour = request.cardLastFour;
        paymentMethod.cardBrand = request.cardBrand;
        paymentMethod.cardExpiryMonth = request.cardExpiryMonth;
        paymentMethod.cardExpiryYear = request.cardExpiryYear;
        paymentMethod.bankName = request.bankName;
        paymentMethod.accountLastFour = request.accountLastFour;
        paymentMethod.billingAddress = request.billingAddress;
        paymentMethod.billingZipCode = request.billingZipCode;
        paymentMethod.isDefault = request.isDefault != null ? request.isDefault : false;
        paymentMethod.nickname = request.nickname;

        PaymentMethod saved = paymentMethodService.savePaymentMethod(paymentMethod);

        if (paymentMethod.isDefault) {
            paymentMethodService.setAsDefault(saved.id, saved.patientId);
        }

        PaymentMethodResponse response = toResponse(saved);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{id}")
    public Response getPaymentMethodById(@PathParam("id") Long id) {
        PaymentMethod paymentMethod = paymentMethodService.getPaymentMethodById(id);
        PaymentMethodResponse response = toResponse(paymentMethod);
        return Response.ok(response).build();
    }

    @GET
    @Path("/patient/{patientId}")
    public Response getPaymentMethodsByPatient(@PathParam("patientId") Long patientId) {
        List<PaymentMethod> paymentMethods = paymentMethodService.getPaymentMethodsByPatientId(patientId);
        List<PaymentMethodResponse> responses = paymentMethods.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @GET
    @Path("/patient/{patientId}/active")
    public Response getActivePaymentMethods(@PathParam("patientId") Long patientId) {
        List<PaymentMethod> paymentMethods = paymentMethodService.getActivePaymentMethods(patientId);
        List<PaymentMethodResponse> responses = paymentMethods.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @GET
    @Path("/patient/{patientId}/default")
    public Response getDefaultPaymentMethod(@PathParam("patientId") Long patientId) {
        PaymentMethod paymentMethod = paymentMethodService.getDefaultPaymentMethod(patientId);
        if (paymentMethod == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        PaymentMethodResponse response = toResponse(paymentMethod);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/set-default")
    public Response setAsDefault(@PathParam("id") Long id, @QueryParam("patientId") Long patientId) {
        PaymentMethod paymentMethod = paymentMethodService.setAsDefault(id, patientId);
        PaymentMethodResponse response = toResponse(paymentMethod);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/deactivate")
    public Response deactivatePaymentMethod(@PathParam("id") Long id) {
        paymentMethodService.deactivatePaymentMethod(id);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletePaymentMethod(@PathParam("id") Long id) {
        paymentMethodService.deletePaymentMethod(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/expired")
    public Response getExpiredCards() {
        List<PaymentMethod> paymentMethods = paymentMethodService.getExpiredCards();
        List<PaymentMethodResponse> responses = paymentMethods.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    @GET
    @Path("/expiring-soon")
    public Response getCardsExpiringSoon(@QueryParam("months") @DefaultValue("3") int months) {
        List<PaymentMethod> paymentMethods = paymentMethodService.getCardsExpiringSoon(months);
        List<PaymentMethodResponse> responses = paymentMethods.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Response.ok(responses).build();
    }

    private PaymentMethodResponse toResponse(PaymentMethod method) {
        PaymentMethodResponse response = new PaymentMethodResponse();
        response.id = method.id;
        response.patientId = method.patientId;
        response.paymentType = method.paymentType;
        response.paymentGateway = method.paymentGateway;
        response.cardLastFour = method.cardLastFour;
        response.cardBrand = method.cardBrand;
        response.cardExpiryMonth = method.cardExpiryMonth;
        response.cardExpiryYear = method.cardExpiryYear;
        response.bankName = method.bankName;
        response.accountLastFour = method.accountLastFour;
        response.billingZipCode = method.billingZipCode;
        response.isDefault = method.isDefault;
        response.isActive = method.isActive;
        response.nickname = method.nickname;
        response.lastUsedAt = method.lastUsedAt;
        response.maskedDisplay = method.getMaskedDisplay();
        response.createdAt = method.createdAt;
        return response;
    }
}

