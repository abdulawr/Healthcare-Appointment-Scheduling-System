package com.basit.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateInvoiceRequest {

    public LocalDate dueDate;
    public BigDecimal taxAmount;
    public BigDecimal discountAmount;
    public String notes;
}

