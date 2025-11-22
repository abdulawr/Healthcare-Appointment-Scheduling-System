package com.basit.cz.notification.messaging;

import com.basit.cz.notification.model.NotificationChannel;

import java.util.List;
import java.util.Map;

public class NotificationEvent {
    public String idempotencyKey;
    public String userId;
    public String eventType;
    public String locale;
    public String brand;
    public List<NotificationChannel> channels;
    public Map<String, Object> payload;
}
