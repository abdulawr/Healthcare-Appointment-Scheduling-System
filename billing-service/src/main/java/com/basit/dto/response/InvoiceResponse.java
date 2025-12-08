package com.basit.dto.response;

import com.basit.constant.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class InvoiceResponse {
    public Long id;
    public Long appointmentId;
    public Long patientId;
    public BigDecimal subtotal;
    public BigDecimal tax;
    public BigDecimal total;
    public BigDecimal amountPaid;
    public BigDecimal balance;
    public InvoiceStatus status;
    public LocalDateTime issueDate;
    public LocalDateTime dueDate;
    public LocalDateTime paidDate;
    public List<InvoiceItemResponse> items;
    public String notes;

    public static class InvoiceItemResponse {
        public Long id;
        public String description;
        public Integer quantity;
        public BigDecimal unitPrice;
        public BigDecimal amount;
    }
}
