package com.basit.event;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@ApplicationScoped
public class BillingEventProducer {

    private static final Logger LOG = Logger.getLogger(BillingEventProducer.class);

    @Channel("invoice-created")
    @Broadcast
    Emitter<InvoiceCreatedEvent> invoiceCreatedEmitter;

    @Channel("payment-processed")
    @Broadcast
    Emitter<PaymentProcessedEvent> paymentProcessedEmitter;

    @Channel("payment-failed")
    @Broadcast
    Emitter<PaymentFailedEvent> paymentFailedEmitter;

    @Channel("refund-issued")
    @Broadcast
    Emitter<RefundIssuedEvent> refundIssuedEmitter;

    @Channel("insurance-claim-submitted")
    @Broadcast
    Emitter<InsuranceClaimSubmittedEvent> insuranceClaimSubmittedEmitter;

    @Channel("payment-reminder-sent")
    @Broadcast
    Emitter<PaymentReminderSentEvent> paymentReminderSentEmitter;

    /**
     * Publish invoice created event
     */
    public void publishInvoiceCreated(InvoiceCreatedEvent event) {
        LOG.infof("Publishing InvoiceCreatedEvent: %s", event);
        invoiceCreatedEmitter.send(event);
    }

    /**
     * Publish payment processed event
     */
    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        LOG.infof("Publishing PaymentProcessedEvent: %s", event);
        paymentProcessedEmitter.send(event);
    }

    /**
     * Publish payment failed event
     */
    public void publishPaymentFailed(PaymentFailedEvent event) {
        LOG.warnf("Publishing PaymentFailedEvent: %s", event);
        paymentFailedEmitter.send(event);
    }

    /**
     * Publish refund issued event
     */
    public void publishRefundIssued(RefundIssuedEvent event) {
        LOG.infof("Publishing RefundIssuedEvent: %s", event);
        refundIssuedEmitter.send(event);
    }

    /**
     * Publish insurance claim submitted event
     */
    public void publishInsuranceClaimSubmitted(InsuranceClaimSubmittedEvent event) {
        LOG.infof("Publishing InsuranceClaimSubmittedEvent: %s", event);
        insuranceClaimSubmittedEmitter.send(event);
    }

    /**
     * Publish payment reminder sent event
     */
    public void publishPaymentReminderSent(PaymentReminderSentEvent event) {
        LOG.infof("Publishing PaymentReminderSentEvent: %s", event);
        paymentReminderSentEmitter.send(event);
    }
}
