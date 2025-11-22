package com.basit.cz.notification.novu;

import java.util.Map;

public class NovuWebhookEvent {
    public String transactionId;
    public String status; // delivered, failed, etc.
    public Map<String, Object> raw; // keep the rest
}
