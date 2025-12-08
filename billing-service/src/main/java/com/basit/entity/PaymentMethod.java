package com.basit.entity;

import com.basit.constant.PaymentMethodType;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_methods")
public class PaymentMethod extends PanacheEntity {

    @Column(nullable = false)
    public Long patientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public PaymentMethodType type;

    @Column(length = 100)
    public String cardholderName;

    @Column(length = 4)
    public String lastFourDigits;

    @Column(length = 50)
    public String cardBrand; // "VISA", "MASTERCARD", etc.

    public Integer expiryMonth;

    public Integer expiryYear;

    @Column(length = 255)
    public String token; // Stripe/PayPal token

    @Column(nullable = false)
    public Boolean isDefault = false;

    @Column(nullable = false)
    public Boolean isActive = true;

    @Column(nullable = false)
    public LocalDateTime createdAt;

    public LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
