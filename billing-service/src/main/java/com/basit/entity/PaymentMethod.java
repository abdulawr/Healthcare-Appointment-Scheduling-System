package com.basit.entity;

import com.basit.constant.PaymentMethodType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a saved payment method for a patient.
 * Stores tokenized payment information for secure reuse.
 */
@Entity
@Table(name = "payment_methods", indexes = {
        @Index(name = "idx_payment_method_patient", columnList = "patient_id"),
        @Index(name = "idx_payment_method_token", columnList = "payment_token")
})
public class PaymentMethod extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    public Long patientId;

    @NotNull(message = "Payment method type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 30)
    public PaymentMethodType paymentType;

    @NotBlank(message = "Payment token is required")
    @Column(name = "payment_token", nullable = false, unique = true, length = 200)
    public String paymentToken;

    @NotBlank(message = "Payment gateway is required")
    @Column(name = "payment_gateway", nullable = false, length = 50)
    public String paymentGateway;

    @Column(name = "card_last_four", length = 4)
    public String cardLastFour;

    @Column(name = "card_brand", length = 50)
    public String cardBrand;

    @Column(name = "card_expiry_month")
    public Integer cardExpiryMonth;

    @Column(name = "card_expiry_year")
    public Integer cardExpiryYear;

    @Column(name = "bank_name", length = 200)
    public String bankName;

    @Column(name = "account_last_four", length = 4)
    public String accountLastFour;

    @Column(name = "billing_address", length = 500)
    public String billingAddress;

    @Column(name = "billing_zip_code", length = 20)
    public String billingZipCode;

    @NotNull(message = "Is default flag is required")
    @Column(name = "is_default", nullable = false)
    public Boolean isDefault = false;

    @NotNull(message = "Is active flag is required")
    @Column(name = "is_active", nullable = false)
    public Boolean isActive = true;

    @Column(name = "nickname", length = 100)
    public String nickname;

    @Column(name = "last_used_at")
    public LocalDateTime lastUsedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @Version
    public Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isDefault == null) {
            isDefault = false;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Marks this payment method as the default for the patient
     */
    public void setAsDefault() {
        this.isDefault = true;
    }

    /**
     * Removes the default flag from this payment method
     */
    public void unsetDefault() {
        this.isDefault = false;
    }

    /**
     * Deactivates this payment method
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Records usage of this payment method
     */
    public void recordUsage() {
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * Checks if the card is expired
     */
    public boolean isCardExpired() {
        if (paymentType != PaymentMethodType.CREDIT_CARD &&
                paymentType != PaymentMethodType.DEBIT_CARD) {
            return false;
        }

        if (cardExpiryMonth == null || cardExpiryYear == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        if (cardExpiryYear < currentYear) {
            return true;
        }

        return cardExpiryYear == currentYear && cardExpiryMonth < currentMonth;
    }

    /**
     * Gets a masked display string for the payment method
     */
    public String getMaskedDisplay() {
        switch (paymentType) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                return String.format("%s ending in %s",
                        cardBrand != null ? cardBrand : paymentType.name(),
                        cardLastFour);
            case BANK_ACCOUNT:
                return String.format("%s account ending in %s",
                        bankName != null ? bankName : "Bank",
                        accountLastFour);
            case PAYPAL:
                return "PayPal";
            default:
                return paymentType.name();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentMethod)) return false;
        PaymentMethod that = (PaymentMethod) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(paymentToken, that.paymentToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, paymentToken);
    }

    @Override
    public String toString() {
        return "PaymentMethod{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", paymentType=" + paymentType +
                ", isDefault=" + isDefault +
                ", isActive=" + isActive +
                ", maskedDisplay='" + getMaskedDisplay() + '\'' +
                '}';
    }
}