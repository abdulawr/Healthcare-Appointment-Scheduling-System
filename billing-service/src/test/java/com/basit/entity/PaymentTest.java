package com.basit.entity;

import com.basit.constant.PaymentMethodType;
import com.basit.constant.PaymentStatus;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Payment entity
 */
@QuarkusTest
public class PaymentTest {

    @Test
    void shouldCreatePaymentWithDefaults() {
        // Arrange & Act
        Payment payment = new Payment();
        payment.invoiceId = 1L;
        payment.patientId = 100L;
        payment.amount = new BigDecimal("100.00");
        payment.paymentMethod = PaymentMethodType.CREDIT_CARD;
        payment.transactionId = "TXN-12345";
        payment.paymentGateway = "Stripe";
        payment.onCreate();

        // Assert
        assertThat(payment.status).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.refundedAmount).isEqualTo(BigDecimal.ZERO);
        assertThat(payment.isRefundable).isTrue();
        assertThat(payment.idempotencyKey).isNotNull();
        assertThat(payment.createdAt).isNotNull();
        assertThat(payment.updatedAt).isNotNull();
    }

    @Test
    void shouldMarkPaymentAsCompleted() {
        // Arrange
        Payment payment = new Payment();
        payment.invoiceId = 1L;
        payment.patientId = 100L;
        payment.amount = new BigDecimal("100.00");
        payment.paymentMethod = PaymentMethodType.CREDIT_CARD;
        payment.transactionId = "TXN-12345";
        payment.paymentGateway = "Stripe";
        payment.onCreate();

        // Act
        payment.markCompleted();

        // Assert
        assertThat(payment.status).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.processedAt).isNotNull();
        assertThat(payment.isSuccessful()).isTrue();
    }

    @Test
    void shouldMarkPaymentAsFailed() {
        // Arrange
        Payment payment = new Payment();
        payment.invoiceId = 1L;
        payment.patientId = 100L;
        payment.amount = new BigDecimal("100.00");
        payment.paymentMethod = PaymentMethodType.CREDIT_CARD;
        payment.transactionId = "TXN-12345";
        payment.paymentGateway = "Stripe";
        payment.onCreate();

        // Act
        payment.markFailed("Insufficient funds");

        // Assert
        assertThat(payment.status).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.failedReason).isEqualTo("Insufficient funds");
        assertThat(payment.processedAt).isNotNull();
        assertThat(payment.isSuccessful()).isFalse();
    }

    @Test
    void shouldCheckIfPaymentIsSuccessful() {
        // Arrange
        Payment payment1 = new Payment();
        payment1.status = PaymentStatus.COMPLETED;

        Payment payment2 = new Payment();
        payment2.status = PaymentStatus.FAILED;

        // Act & Assert
        assertThat(payment1.isSuccessful()).isTrue();
        assertThat(payment2.isSuccessful()).isFalse();
    }

    @Test
    void shouldRecordFullRefund() {
        // Arrange
        Payment payment = new Payment();
        payment.invoiceId = 1L;
        payment.patientId = 100L;
        payment.amount = new BigDecimal("100.00");
        payment.paymentMethod = PaymentMethodType.CREDIT_CARD;
        payment.transactionId = "TXN-12345";
        payment.paymentGateway = "Stripe";
        payment.onCreate();
        payment.markCompleted();

        // Act
        payment.recordRefund(new BigDecimal("100.00"));

        // Assert
        assertThat(payment.refundedAmount).isEqualByComparingTo("100.00");
        assertThat(payment.status).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void shouldRecordPartialRefund() {
        // Arrange
        Payment payment = new Payment();
        payment.invoiceId = 1L;
        payment.patientId = 100L;
        payment.amount = new BigDecimal("100.00");
        payment.paymentMethod = PaymentMethodType.CREDIT_CARD;
        payment.transactionId = "TXN-12345";
        payment.paymentGateway = "Stripe";
        payment.onCreate();
        payment.markCompleted();

        // Act
        payment.recordRefund(new BigDecimal("30.00"));

        // Assert
        assertThat(payment.refundedAmount).isEqualByComparingTo("30.00");
        assertThat(payment.status).isEqualTo(PaymentStatus.PARTIALLY_REFUNDED);
    }

    @Test
    void shouldRecordMultiplePartialRefunds() {
        // Arrange
        Payment payment = new Payment();
        payment.invoiceId = 1L;
        payment.patientId = 100L;
        payment.amount = new BigDecimal("100.00");
        payment.paymentMethod = PaymentMethodType.CREDIT_CARD;
        payment.transactionId = "TXN-12345";
        payment.paymentGateway = "Stripe";
        payment.onCreate();
        payment.markCompleted();

        // Act
        payment.recordRefund(new BigDecimal("20.00"));
        payment.recordRefund(new BigDecimal("30.00"));

        // Assert
        assertThat(payment.refundedAmount).isEqualByComparingTo("50.00");
        assertThat(payment.status).isEqualTo(PaymentStatus.PARTIALLY_REFUNDED);
    }

    @Test
    void shouldThrowExceptionWhenRefundExceedsPayment() {
        // Arrange
        Payment payment = new Payment();
        payment.invoiceId = 1L;
        payment.patientId = 100L;
        payment.amount = new BigDecimal("100.00");
        payment.paymentMethod = PaymentMethodType.CREDIT_CARD;
        payment.transactionId = "TXN-12345";
        payment.paymentGateway = "Stripe";
        payment.onCreate();
        payment.markCompleted();

        // Act & Assert
        assertThatThrownBy(() -> payment.recordRefund(new BigDecimal("150.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Total refund amount cannot exceed payment amount");
    }

    @Test
    void shouldCheckIfPaymentCanBeRefunded() {
        // Arrange
        Payment payment1 = new Payment();
        payment1.isRefundable = true;
        payment1.status = PaymentStatus.COMPLETED;
        payment1.amount = new BigDecimal("100.00");
        payment1.refundedAmount = BigDecimal.ZERO;

        Payment payment2 = new Payment();
        payment2.isRefundable = false;
        payment2.status = PaymentStatus.COMPLETED;
        payment2.amount = new BigDecimal("100.00");
        payment2.refundedAmount = BigDecimal.ZERO;

        Payment payment3 = new Payment();
        payment3.isRefundable = true;
        payment3.status = PaymentStatus.FAILED;
        payment3.amount = new BigDecimal("100.00");
        payment3.refundedAmount = BigDecimal.ZERO;

        // Act & Assert
        assertThat(payment1.canBeRefunded()).isTrue();
        assertThat(payment2.canBeRefunded()).isFalse();
        assertThat(payment3.canBeRefunded()).isFalse();
    }

    @Test
    void shouldCalculateRefundableAmount() {
        // Arrange
        Payment payment = new Payment();
        payment.amount = new BigDecimal("100.00");
        payment.refundedAmount = new BigDecimal("30.00");

        // Act
        BigDecimal refundableAmount = payment.getRefundableAmount();

        // Assert
        assertThat(refundableAmount).isEqualByComparingTo("70.00");
    }

    @Test
    void shouldGenerateUniqueIdempotencyKey() {
        // Arrange & Act
        Payment payment1 = new Payment();
        payment1.onCreate();

        Payment payment2 = new Payment();
        payment2.onCreate();

        // Assert
        assertThat(payment1.idempotencyKey).isNotNull();
        assertThat(payment2.idempotencyKey).isNotNull();
        assertThat(payment1.idempotencyKey).isNotEqualTo(payment2.idempotencyKey);
    }

    @Test
    void shouldSupportDifferentPaymentMethods() {
        // Arrange & Act
        Payment payment1 = new Payment();
        payment1.paymentMethod = PaymentMethodType.CREDIT_CARD;

        Payment payment2 = new Payment();
        payment2.paymentMethod = PaymentMethodType.PAYPAL;

        Payment payment3 = new Payment();
        payment3.paymentMethod = PaymentMethodType.BANK_ACCOUNT;

        // Assert
        assertThat(payment1.paymentMethod).isEqualTo(PaymentMethodType.CREDIT_CARD);
        assertThat(payment2.paymentMethod).isEqualTo(PaymentMethodType.PAYPAL);
        assertThat(payment3.paymentMethod).isEqualTo(PaymentMethodType.BANK_ACCOUNT);
    }
}
