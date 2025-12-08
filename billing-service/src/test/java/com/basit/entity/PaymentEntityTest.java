package com.basit.entity;

import com.basit.constant.PaymentMethodType;
import com.basit.constant.PaymentStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class PaymentEntityTest {

    @Inject
    EntityManager em;

    @Test
    @Transactional
    public void testCreatePayment() {
        Payment payment = new Payment();
        payment.invoiceId = 1L;
        payment.patientId = 1L;
        payment.amount = new BigDecimal("100.00");
        payment.paymentMethod = PaymentMethodType.CREDIT_CARD;
        payment.gateway = "STRIPE";
        payment.transactionId = "txn_123456";
        payment.persist();

        assertNotNull(payment.id);
        assertEquals(PaymentStatus.PENDING, payment.status);
        assertNotNull(payment.paymentDate);
        assertEquals(0, new BigDecimal("100.00").compareTo(payment.amount));
    }

    @Test
    @Transactional
    public void testPaymentStatusProgression() {
        Payment payment = new Payment();
        payment.invoiceId = 1L;
        payment.patientId = 1L;
        payment.amount = new BigDecimal("100.00");
        payment.paymentMethod = PaymentMethodType.STRIPE;
        payment.gateway = "STRIPE";
        payment.status = PaymentStatus.PENDING;
        payment.transactionId = "txn_001";
        payment.persist();

        payment.status = PaymentStatus.COMPLETED;
        payment.persist();

        assertEquals(PaymentStatus.COMPLETED, payment.status);
    }

    @Test
    @Transactional
    public void testPaymentWithDifferentMethods() {
        // Credit card payment
        Payment ccPayment = new Payment();
        ccPayment.invoiceId = 1L;
        ccPayment.patientId = 1L;
        ccPayment.amount = new BigDecimal("50.00");
        ccPayment.paymentMethod = PaymentMethodType.CREDIT_CARD;
        ccPayment.gateway = "STRIPE";
        ccPayment.transactionId = "txn_cc_001";
        ccPayment.persist();

        // PayPal payment
        Payment paypalPayment = new Payment();
        paypalPayment.invoiceId = 1L;
        paypalPayment.patientId = 1L;
        paypalPayment.amount = new BigDecimal("50.00");
        paypalPayment.paymentMethod = PaymentMethodType.PAYPAL;
        paypalPayment.gateway = "PAYPAL";
        paypalPayment.transactionId = "txn_pp_001";
        paypalPayment.persist();

        assertNotNull(ccPayment.id);
        assertNotNull(paypalPayment.id);
        assertEquals(PaymentMethodType.CREDIT_CARD, ccPayment.paymentMethod);
        assertEquals(PaymentMethodType.PAYPAL, paypalPayment.paymentMethod);
    }

    @Test
    @Transactional
    public void testFailedPayment() {
        Payment payment = new Payment();
        payment.invoiceId = 1L;
        payment.patientId = 1L;
        payment.amount = new BigDecimal("100.00");
        payment.paymentMethod = PaymentMethodType.CREDIT_CARD;
        payment.gateway = "STRIPE";
        payment.transactionId = "txn_failed";
        payment.status = PaymentStatus.FAILED;
        payment.failureReason = "Insufficient funds";
        payment.persist();

        assertEquals(PaymentStatus.FAILED, payment.status);
        assertNotNull(payment.failureReason);
        assertEquals("Insufficient funds", payment.failureReason);
    }
}