package com.basit.entity;

import com.basit.constant.InvoiceStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
public class Invoice extends PanacheEntity {

    @Column(nullable = false)
    public Long appointmentId;

    @Column(nullable = false)
    public Long patientId;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal tax;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal total;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public InvoiceStatus status = InvoiceStatus.PENDING;

    @Column(nullable = false)
    public LocalDateTime issueDate;

    @Column(nullable = false)
    public LocalDateTime dueDate;

    public LocalDateTime paidDate;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<InvoiceItem> items = new ArrayList<>();

    @Column(length = 1000)
    public String notes;

    @PrePersist
    public void prePersist() {
        if (issueDate == null) {
            issueDate = LocalDateTime.now();
        }
        if (dueDate == null) {
            dueDate = issueDate.plusDays(30);
        }
        calculateBalance();
    }

    @PreUpdate
    public void preUpdate() {
        calculateBalance();
    }

    public void calculateBalance() {
        this.balance = this.total.subtract(this.amountPaid);
        if (this.balance.compareTo(BigDecimal.ZERO) == 0 &&
                this.status == InvoiceStatus.PENDING) {
            this.status = InvoiceStatus.PAID;
            this.paidDate = LocalDateTime.now();
        }
    }
}
