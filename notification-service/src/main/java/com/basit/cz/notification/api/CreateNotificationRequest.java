package com.basit.cz.notification.api;

import com.basit.cz.notification.model.NotificationChannel;

import java.util.List;
import java.util.Map;

public class CreateNotificationRequest {
    public String idempotencyKey;
    public String userId;
    public String eventType;        // e.g. "order.shipped"
    public String locale;          // e.g. "en-US"
    public String brand;           // e.g. "default"
    public List<NotificationChannel> channels;
    public Map<String, Object> payload; // arbitrary data for templates
}
