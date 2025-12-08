package com.basit.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    public Invoice invoice;

    @Column(nullable = false, length = 255)
    public String description;

    @Column(nullable = false)
    public Integer quantity = 1;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal amount;

    @PrePersist
    @PreUpdate
    public void calculateAmount() {
        this.amount = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
}
