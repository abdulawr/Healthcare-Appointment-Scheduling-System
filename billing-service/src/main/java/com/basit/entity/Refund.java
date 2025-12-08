package com.basit.entity;

import com.basit.constant.RefundStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
public class Refund extends PanacheEntity {

    @Column(nullable = false)
    public Long paymentId;

    @Column(nullable = false)
    public Long invoiceId;

    @Column(nullable = false)
    public Long patientId;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public RefundStatus status = RefundStatus.PENDING;

    @Column(nullable = false, length = 500)
    public String reason;

    @Column(unique = true, length = 255)
    public String refundTransactionId;

    @Column(nullable = false)
    public LocalDateTime requestDate;

    public LocalDateTime processedDate;

    @Column(length = 500)
    public String failureReason;

    @PrePersist
    public void prePersist() {
        if (requestDate == null) {
            requestDate = LocalDateTime.now();
        }
    }
}
