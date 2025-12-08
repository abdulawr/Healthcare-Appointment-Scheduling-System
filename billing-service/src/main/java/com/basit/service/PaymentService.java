package com.basit.service;

import com.basit.dto.request.PaymentProcessRequest;
import com.basit.dto.response.PaymentResponse;
import com.basit.entity.Invoice;
import com.basit.entity.Payment;
import com.basit.constant.PaymentStatus;
import com.basit.event.BillingEventProducer;
import com.basit.mapper.PaymentMapper;
import com.basit.repository.InvoiceRepository;
import com.basit.repository.PaymentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.util.UUID;

@ApplicationScoped
public class PaymentService {

    @Inject
    PaymentRepository paymentRepository;

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    PaymentMapper paymentMapper;

    @Inject
    BillingEventProducer eventProducer;

    @Transactional
    public PaymentResponse processPayment(PaymentProcessRequest request) {
        // Validate invoice exists
        Invoice invoice = invoiceRepository.findByIdOptional(request.invoiceId)
                .orElseThrow(() -> new NotFoundException(
                        "Invoice not found with id: " + request.invoiceId));

        // Validate payment amount
        if (request.amount.compareTo(invoice.balance) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount exceeds invoice balance");
        }

        // Create payment record
        Payment payment = paymentMapper.toEntity(request);
        payment.patientId = invoice.patientId;
        payment.transactionId = generateTransactionId();
        payment.status = PaymentStatus.PROCESSING;

        paymentRepository.persist(payment);

        // Process payment with gateway (simulated)
        boolean paymentSuccess = processWithGateway(payment);

        if (paymentSuccess) {
            payment.status = PaymentStatus.COMPLETED;

            // Update invoice
            invoice.amountPaid = invoice.amountPaid.add(request.amount);
            invoice.calculateBalance();
            invoiceRepository.persist(invoice);
        } else {
            payment.status = PaymentStatus.FAILED;
            payment.failureReason = "Payment gateway declined";
        }

        paymentRepository.persist(payment);
        return paymentMapper.toResponse(payment);
    }

    private boolean processWithGateway(Payment payment) {
        // Simulate payment processing
        // In production, integrate with Stripe/PayPal SDK
        return payment.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public PaymentResponse getPayment(Long id) {
        Payment payment = paymentRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException(
                        "Payment not found with id: " + id));
        return paymentMapper.toResponse(payment);
    }
}
