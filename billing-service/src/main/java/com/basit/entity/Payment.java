package com.basit.entity;

import com.basit.constant.PaymentMethodType;
import com.basit.constant.PaymentStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment extends PanacheEntity {

    @Column(nullable = false)
    public Long invoiceId;

    @Column(nullable = false)
    public Long patientId;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public PaymentMethodType paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public PaymentStatus status = PaymentStatus.PENDING;

    @Column(unique = true, length = 255)
    public String transactionId;

    @Column(length = 100)
    public String gateway; // "STRIPE", "PAYPAL", etc.

    @Column(nullable = false)
    public LocalDateTime paymentDate;

    public LocalDateTime processedDate;

    @Column(length = 500)
    public String failureReason;

    @Column(length = 1000)
    public String notes;

    @PrePersist
    public void prePersist() {
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
    }
}
