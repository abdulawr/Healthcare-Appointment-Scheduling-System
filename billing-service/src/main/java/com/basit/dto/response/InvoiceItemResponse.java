package com.basit.dto.response;

import java.math.BigDecimal;

public class InvoiceItemResponse {
    public Long id;
    public String description;
    public String serviceCode;
    public Integer quantity;
    public BigDecimal unitPrice;
    public BigDecimal totalPrice;
}

