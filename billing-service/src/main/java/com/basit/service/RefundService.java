package com.basit.service;

import com.basit.dto.request.RefundRequest;
import com.basit.dto.response.RefundResponse;
import com.basit.entity.Invoice;
import com.basit.entity.Payment;
import com.basit.entity.Refund;
import com.basit.constant.PaymentStatus;
import com.basit.constant.RefundStatus;
import com.basit.event.BillingEventProducer;
import com.basit.event.RefundIssuedEvent;
import com.basit.mapper.RefundMapper;
import com.basit.repository.InvoiceRepository;
import com.basit.repository.PaymentRepository;
import com.basit.repository.RefundRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RefundService {

    @Inject
    RefundRepository refundRepository;

    @Inject
    PaymentRepository paymentRepository;

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    RefundMapper refundMapper;

    @Inject
    BillingEventProducer eventProducer;

    @Transactional
    public RefundResponse processRefund(RefundRequest request) {
        // Validate payment exists
        Payment payment = paymentRepository.findByIdOptional(request.paymentId)
                .orElseThrow(() -> new NotFoundException(
                        "Payment not found with id: " + request.paymentId));

        // Validate payment was successful
        if (payment.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Can only refund completed payments. Payment status: " + payment.status);
        }

        // Validate refund amount doesn't exceed payment amount
        if (request.amount.compareTo(payment.amount) > 0) {
            throw new IllegalArgumentException(
                    "Refund amount cannot exceed payment amount");
        }

        // Create refund record
        Refund refund = refundMapper.toEntity(request);
        refund.invoiceId = payment.invoiceId;
        refund.patientId = payment.patientId;
        refund.refundTransactionId = generateRefundTransactionId();
        refund.status = RefundStatus.PROCESSING;

        refundRepository.persist(refund);

        // Process refund with gateway (simulated)
        boolean refundSuccess = processWithGateway(refund, payment.gateway);

        if (refundSuccess) {
            refund.status = RefundStatus.COMPLETED;

            // Update payment status
            payment.status = PaymentStatus.REFUNDED;
            paymentRepository.persist(payment);

            // Update invoice
            Invoice invoice = invoiceRepository.findByIdOptional(payment.invoiceId)
                    .orElseThrow(() -> new NotFoundException(
                            "Invoice not found with id: " + payment.invoiceId));

            invoice.amountPaid = invoice.amountPaid.subtract(request.amount);
            invoice.calculateBalance();
            invoiceRepository.persist(invoice);
        } else {
            refund.status = RefundStatus.FAILED;
            refund.failureReason = "Refund gateway processing failed";
        }

        refundRepository.persist(refund);

        // ✅ ADD THIS: Publish event
        RefundIssuedEvent event = new RefundIssuedEvent();
        event.refundId = refund.id;
        event.paymentId = payment.id;
        event.invoiceId = payment.invoiceId;
        event.patientId = payment.patientId;
        event.amount = refund.amount;
        event.status = refund.status;
        event.reason = refund.reason;
        event.refundDate = null;
        event.processedBy = null;

        eventProducer.publishRefundIssued(event);
        // ✅ END OF ADDITION

        return refundMapper.toResponse(refund);
    }

    private boolean processWithGateway(Refund refund, String gateway) {
        // Simulate refund processing
        // In production, integrate with actual payment gateway (Stripe/PayPal)
        return true;
    }

    private String generateRefundTransactionId() {
        return "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public RefundResponse getRefund(Long id) {
        Refund refund = refundRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException(
                        "Refund not found with id: " + id));
        return refundMapper.toResponse(refund);
    }

    public List<RefundResponse> getRefundsByPayment(Long paymentId) {
        return refundRepository.findByPaymentId(paymentId).stream()
                .map(refundMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<RefundResponse> getRefundsByInvoice(Long invoiceId) {
        return refundRepository.findByInvoiceId(invoiceId).stream()
                .map(refundMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<RefundResponse> getRefundsByPatient(Long patientId) {
        return refundRepository.findByPatientId(patientId).stream()
                .map(refundMapper::toResponse)
                .collect(Collectors.toList());
    }

    public RefundResponse getRefundByTransactionId(String refundTransactionId) {
        Refund refund = refundRepository.findByRefundTransactionId(refundTransactionId);
        if (refund == null) {
            throw new NotFoundException(
                    "Refund not found with transaction id: " + refundTransactionId);
        }
        return refundMapper.toResponse(refund);
    }
}
