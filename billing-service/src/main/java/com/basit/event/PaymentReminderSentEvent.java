package com.basit.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentReminderSentEvent {

    public Long invoiceId;
    public Long patientId;
    public BigDecimal outstandingBalance;
    public LocalDateTime dueDate;
    public String reminderType; // "FIRST", "SECOND", "FINAL"
    public String channel; // "EMAIL", "SMS"
    public LocalDateTime sentDate;
    public LocalDateTime eventTimestamp;

    public PaymentReminderSentEvent() {
        this.eventTimestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "PaymentReminderSentEvent{" +
                "invoiceId=" + invoiceId +
                ", patientId=" + patientId +
                ", outstandingBalance=" + outstandingBalance +
                ", reminderType='" + reminderType + '\'' +
                "}";
    }
}
