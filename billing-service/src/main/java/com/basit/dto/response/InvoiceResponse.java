package com.basit.dto.response;


import com.basit.constant.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class InvoiceResponse {
    public Long id;
    public Long appointmentId;
    public Long patientId;
    public Long doctorId;
    public String invoiceNumber;
    public LocalDate issueDate;
    public LocalDate dueDate;
    public BigDecimal subtotal;
    public BigDecimal taxAmount;
    public BigDecimal discountAmount;
    public BigDecimal totalAmount;
    public BigDecimal amountPaid;
    public BigDecimal amountDue;
    public InvoiceStatus status;
    public String notes;
    public Long insuranceClaimId;
    public List<InvoiceItemResponse> items;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

