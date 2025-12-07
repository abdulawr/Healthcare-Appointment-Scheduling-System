package com.basit.entity;

import com.basit.constant.ClaimStatus;
import com.basit.constant.PaymentMethodType;
import com.basit.constant.RefundStatus;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Refund, InsuranceClaim, and PaymentMethod entities
 */
@QuarkusTest
public class AdditionalEntitiesTest {

    // ==================== REFUND TESTS ====================

    @Test
    void shouldCreateRefundWithDefaults() {
        // Arrange & Act
        Refund refund = new Refund();
        refund.paymentId = 1L;
        refund.invoiceId = 1L;
        refund.patientId = 100L;
        refund.refundAmount = new BigDecimal("50.00");
        refund.reason = "Customer request";
        refund.onCreate();

        // Assert
        assertThat(refund.status).isEqualTo(RefundStatus.PENDING);
        assertThat(refund.createdAt).isNotNull();
        assertThat(refund.updatedAt).isNotNull();
    }

    @Test
    void shouldMarkRefundAsCompleted() {
        // Arrange
        Refund refund = new Refund();
        refund.paymentId = 1L;
        refund.invoiceId = 1L;
        refund.patientId = 100L;
        refund.refundAmount = new BigDecimal("50.00");
        refund.reason = "Customer request";
        refund.onCreate();

        // Act
        refund.markCompleted();

        // Assert
        assertThat(refund.status).isEqualTo(RefundStatus.COMPLETED);
        assertThat(refund.processedAt).isNotNull();
        assertThat(refund.isCompleted()).isTrue();
    }

    @Test
    void shouldMarkRefundAsFailed() {
        // Arrange
        Refund refund = new Refund();
        refund.paymentId = 1L;
        refund.invoiceId = 1L;
        refund.patientId = 100L;
        refund.refundAmount = new BigDecimal("50.00");
        refund.reason = "Customer request";
        refund.onCreate();

        // Act
        refund.markFailed("Gateway error");

        // Assert
        assertThat(refund.status).isEqualTo(RefundStatus.FAILED);
        assertThat(refund.failedReason).isEqualTo("Gateway error");
        assertThat(refund.processedAt).isNotNull();
    }

    @Test
    void shouldApproveRefund() {
        // Arrange
        Refund refund = new Refund();
        refund.paymentId = 1L;
        refund.invoiceId = 1L;
        refund.patientId = 100L;
        refund.refundAmount = new BigDecimal("50.00");
        refund.reason = "Customer request";
        refund.onCreate();

        // Act
        refund.approve("admin@healthcare.com");

        // Assert
        assertThat(refund.approvedBy).isEqualTo("admin@healthcare.com");
        assertThat(refund.approvedAt).isNotNull();
        assertThat(refund.status).isEqualTo(RefundStatus.PROCESSING);
    }

    @Test
    void shouldCheckIfRefundIsCompleted() {
        // Arrange
        Refund refund1 = new Refund();
        refund1.status = RefundStatus.COMPLETED;

        Refund refund2 = new Refund();
        refund2.status = RefundStatus.PENDING;

        // Act & Assert
        assertThat(refund1.isCompleted()).isTrue();
        assertThat(refund2.isCompleted()).isFalse();
    }

    @Test
    void shouldCheckIfRefundRequiresApproval() {
        // Arrange
        Refund refund1 = new Refund();
        refund1.status = RefundStatus.REQUIRES_REVIEW;

        Refund refund2 = new Refund();
        refund2.status = RefundStatus.PENDING;

        // Act & Assert
        assertThat(refund1.requiresApproval()).isTrue();
        assertThat(refund2.requiresApproval()).isFalse();
    }

    // ==================== INSURANCE CLAIM TESTS ====================

    @Test
    void shouldCreateClaimWithDefaults() {
        // Arrange & Act
        InsuranceClaim claim = new InsuranceClaim();
        claim.invoiceId = 1L;
        claim.patientId = 100L;
        claim.claimNumber = "CLM-2024-001";
        claim.insuranceProvider = "Blue Cross";
        claim.policyNumber = "POL-12345";
        claim.claimAmount = new BigDecimal("500.00");
        claim.submissionDate = LocalDate.now();
        claim.onCreate();

        // Assert
        assertThat(claim.status).isEqualTo(ClaimStatus.DRAFT);
        assertThat(claim.appealCount).isEqualTo(0);
        assertThat(claim.paidAmount).isEqualTo(BigDecimal.ZERO);
        assertThat(claim.createdAt).isNotNull();
        assertThat(claim.updatedAt).isNotNull();
    }

    @Test
    void shouldSubmitClaim() {
        // Arrange
        InsuranceClaim claim = new InsuranceClaim();
        claim.invoiceId = 1L;
        claim.patientId = 100L;
        claim.claimNumber = "CLM-2024-002";
        claim.insuranceProvider = "Blue Cross";
        claim.policyNumber = "POL-12345";
        claim.claimAmount = new BigDecimal("500.00");
        claim.submissionDate = LocalDate.now();
        claim.onCreate();

        // Act
        claim.submit();

        // Assert
        assertThat(claim.status).isEqualTo(ClaimStatus.SUBMITTED);
        assertThat(claim.submissionDate).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenSubmittingNonDraftClaim() {
        // Arrange
        InsuranceClaim claim = new InsuranceClaim();
        claim.status = ClaimStatus.SUBMITTED;

        // Act & Assert
        assertThatThrownBy(() -> claim.submit())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only draft claims can be submitted");
    }

    @Test
    void shouldApproveClaimWithFullAmount() {
        // Arrange
        InsuranceClaim claim = new InsuranceClaim();
        claim.invoiceId = 1L;
        claim.patientId = 100L;
        claim.claimNumber = "CLM-2024-003";
        claim.insuranceProvider = "Blue Cross";
        claim.policyNumber = "POL-12345";
        claim.claimAmount = new BigDecimal("500.00");
        claim.submissionDate = LocalDate.now();
        claim.onCreate();

        // Act
        claim.approve(new BigDecimal("500.00"));

        // Assert
        assertThat(claim.status).isEqualTo(ClaimStatus.APPROVED);
        assertThat(claim.approvedAmount).isEqualByComparingTo("500.00");
        assertThat(claim.patientResponsibility).isEqualByComparingTo("0.00");
        assertThat(claim.processedDate).isNotNull();
    }

    @Test
    void shouldApproveClaimWithPartialAmount() {
        // Arrange
        InsuranceClaim claim = new InsuranceClaim();
        claim.invoiceId = 1L;
        claim.patientId = 100L;
        claim.claimNumber = "CLM-2024-004";
        claim.insuranceProvider = "Blue Cross";
        claim.policyNumber = "POL-12345";
        claim.claimAmount = new BigDecimal("500.00");
        claim.submissionDate = LocalDate.now();
        claim.onCreate();

        // Act
        claim.approve(new BigDecimal("400.00"));

        // Assert
        assertThat(claim.status).isEqualTo(ClaimStatus.APPROVED);
        assertThat(claim.approvedAmount).isEqualByComparingTo("400.00");
        assertThat(claim.patientResponsibility).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldDenyClaimWithReason() {
        // Arrange
        InsuranceClaim claim = new InsuranceClaim();
        claim.invoiceId = 1L;
        claim.patientId = 100L;
        claim.claimNumber = "CLM-2024-005";
        claim.insuranceProvider = "Blue Cross";
        claim.policyNumber = "POL-12345";
        claim.claimAmount = new BigDecimal("500.00");
        claim.submissionDate = LocalDate.now();
        claim.onCreate();

        // Act
        claim.deny("Service not covered");

        // Assert
        assertThat(claim.status).isEqualTo(ClaimStatus.DENIED);
        assertThat(claim.denialReason).isEqualTo("Service not covered");
        assertThat(claim.patientResponsibility).isEqualByComparingTo("500.00");
        assertThat(claim.processedDate).isNotNull();
    }

    @Test
    void shouldRecordPayment() {
        // Arrange
        InsuranceClaim claim = new InsuranceClaim();
        claim.invoiceId = 1L;
        claim.patientId = 100L;
        claim.claimNumber = "CLM-2024-006";
        claim.insuranceProvider = "Blue Cross";
        claim.policyNumber = "POL-12345";
        claim.claimAmount = new BigDecimal("500.00");
        claim.submissionDate = LocalDate.now();
        claim.onCreate();
        claim.approve(new BigDecimal("400.00"));

        // Act
        claim.recordPayment(new BigDecimal("400.00"));

        // Assert
        assertThat(claim.paidAmount).isEqualByComparingTo("400.00");
        assertThat(claim.status).isEqualTo(ClaimStatus.PAID);
        assertThat(claim.paidDate).isNotNull();
    }

    @Test
    void shouldRecordPartialPayment() {
        // Arrange
        InsuranceClaim claim = new InsuranceClaim();
        claim.invoiceId = 1L;
        claim.patientId = 100L;
        claim.claimNumber = "CLM-2024-007";
        claim.insuranceProvider = "Blue Cross";
        claim.policyNumber = "POL-12345";
        claim.claimAmount = new BigDecimal("500.00");
        claim.submissionDate = LocalDate.now();
        claim.onCreate();
        claim.approve(new BigDecimal("400.00"));

        // Act
        claim.recordPayment(new BigDecimal("200.00"));

        // Assert
        assertThat(claim.paidAmount).isEqualByComparingTo("200.00");
        assertThat(claim.status).isEqualTo(ClaimStatus.APPROVED);
    }

    @Test
    void shouldAppealDeniedClaim() {
        // Arrange
        InsuranceClaim claim = new InsuranceClaim();
        claim.invoiceId = 1L;
        claim.patientId = 100L;
        claim.claimNumber = "CLM-2024-008";
        claim.insuranceProvider = "Blue Cross";
        claim.policyNumber = "POL-12345";
        claim.claimAmount = new BigDecimal("500.00");
        claim.submissionDate = LocalDate.now();
        claim.onCreate();
        claim.deny("Service not covered");

        // Act
        claim.appeal();

        // Assert
        assertThat(claim.status).isEqualTo(ClaimStatus.APPEALED);
        assertThat(claim.appealCount).isEqualTo(1);
        assertThat(claim.lastAppealDate).isNotNull();
    }

    @Test
    void shouldCheckIfClaimIsPaidInFull() {
        // Arrange
        InsuranceClaim claim1 = new InsuranceClaim();
        claim1.status = ClaimStatus.PAID;
        claim1.approvedAmount = new BigDecimal("400.00");
        claim1.paidAmount = new BigDecimal("400.00");

        InsuranceClaim claim2 = new InsuranceClaim();
        claim2.status = ClaimStatus.APPROVED;
        claim2.approvedAmount = new BigDecimal("400.00");
        claim2.paidAmount = new BigDecimal("200.00");

        // Act & Assert
        assertThat(claim1.isPaidInFull()).isTrue();
        assertThat(claim2.isPaidInFull()).isFalse();
    }

    // ==================== PAYMENT METHOD TESTS ====================

    @Test
    void shouldCreatePaymentMethodWithDefaults() {
        // Arrange & Act
        PaymentMethod method = new PaymentMethod();
        method.patientId = 100L;
        method.paymentType = PaymentMethodType.CREDIT_CARD;
        method.paymentToken = "tok_visa_4242";
        method.paymentGateway = "Stripe";
        method.onCreate();

        // Assert
        assertThat(method.isDefault).isFalse();
        assertThat(method.isActive).isTrue();
        assertThat(method.createdAt).isNotNull();
        assertThat(method.updatedAt).isNotNull();
    }

    @Test
    void shouldSetAsDefault() {
        // Arrange
        PaymentMethod method = new PaymentMethod();
        method.patientId = 100L;
        method.paymentType = PaymentMethodType.CREDIT_CARD;
        method.paymentToken = "tok_visa_4242";
        method.paymentGateway = "Stripe";
        method.onCreate();

        // Act
        method.setAsDefault();

        // Assert
        assertThat(method.isDefault).isTrue();
    }

    @Test
    void shouldUnsetDefault() {
        // Arrange
        PaymentMethod method = new PaymentMethod();
        method.patientId = 100L;
        method.paymentType = PaymentMethodType.CREDIT_CARD;
        method.paymentToken = "tok_visa_4242";
        method.paymentGateway = "Stripe";
        method.isDefault = true;

        // Act
        method.unsetDefault();

        // Assert
        assertThat(method.isDefault).isFalse();
    }

    @Test
    void shouldDeactivatePaymentMethod() {
        // Arrange
        PaymentMethod method = new PaymentMethod();
        method.patientId = 100L;
        method.paymentType = PaymentMethodType.CREDIT_CARD;
        method.paymentToken = "tok_visa_4242";
        method.paymentGateway = "Stripe";
        method.onCreate();

        // Act
        method.deactivate();

        // Assert
        assertThat(method.isActive).isFalse();
    }

    @Test
    void shouldRecordUsage() {
        // Arrange
        PaymentMethod method = new PaymentMethod();
        method.patientId = 100L;
        method.paymentType = PaymentMethodType.CREDIT_CARD;
        method.paymentToken = "tok_visa_4242";
        method.paymentGateway = "Stripe";
        method.onCreate();

        // Act
        method.recordUsage();

        // Assert
        assertThat(method.lastUsedAt).isNotNull();
    }

    @Test
    void shouldCheckIfCardIsExpired() {
        // Arrange
        PaymentMethod expiredCard = new PaymentMethod();
        expiredCard.paymentType = PaymentMethodType.CREDIT_CARD;
        expiredCard.cardExpiryMonth = 1;
        expiredCard.cardExpiryYear = 2020;

        PaymentMethod validCard = new PaymentMethod();
        validCard.paymentType = PaymentMethodType.CREDIT_CARD;
        validCard.cardExpiryMonth = 12;
        validCard.cardExpiryYear = 2030;

        // Act & Assert
        assertThat(expiredCard.isCardExpired()).isTrue();
        assertThat(validCard.isCardExpired()).isFalse();
    }

    @Test
    void shouldGetMaskedDisplayForCreditCard() {
        // Arrange
        PaymentMethod method = new PaymentMethod();
        method.paymentType = PaymentMethodType.CREDIT_CARD;
        method.cardBrand = "Visa";
        method.cardLastFour = "4242";

        // Act
        String display = method.getMaskedDisplay();

        // Assert
        assertThat(display).isEqualTo("Visa ending in 4242");
    }

    @Test
    void shouldGetMaskedDisplayForBankAccount() {
        // Arrange
        PaymentMethod method = new PaymentMethod();
        method.paymentType = PaymentMethodType.BANK_ACCOUNT;
        method.bankName = "Chase";
        method.accountLastFour = "5678";

        // Act
        String display = method.getMaskedDisplay();

        // Assert
        assertThat(display).isEqualTo("Chase account ending in 5678");
    }

    @Test
    void shouldGetMaskedDisplayForPayPal() {
        // Arrange
        PaymentMethod method = new PaymentMethod();
        method.paymentType = PaymentMethodType.PAYPAL;

        // Act
        String display = method.getMaskedDisplay();

        // Assert
        assertThat(display).isEqualTo("PayPal");
    }
}
