package com.basit.cz.notification.api;

import com.basit.cz.notification.model.NotificationChannel;
import com.basit.cz.notification.model.NotificationStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class NotificationResponse {
    public UUID id;
    public String userId;
    public String eventType;
    public String locale;
    public String brand;
    public List<NotificationChannel> channels;
    public NotificationStatus status;
    public String novuTransactionId;
    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;
}
