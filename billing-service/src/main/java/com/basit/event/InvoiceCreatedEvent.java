package com.basit.event;

import com.basit.constant.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceCreatedEvent {

    public Long invoiceId;
    public Long appointmentId;
    public Long patientId;
    public BigDecimal subtotal;
    public BigDecimal tax;
    public BigDecimal total;
    public InvoiceStatus status;
    public LocalDateTime issueDate;
    public LocalDateTime dueDate;
    public String notes;
    public LocalDateTime eventTimestamp;

    // Default constructor for JSON deserialization
    public InvoiceCreatedEvent() {
        this.eventTimestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "InvoiceCreatedEvent{" +
                "invoiceId=" + invoiceId +
                ", appointmentId=" + appointmentId +
                ", patientId=" + patientId +
                ", total=" + total +
                ", issueDate=" + issueDate +
                "}";
    }
}
