package com.basit.cz.notification.api;

import com.basit.cz.notification.model.NotificationChannel;
import com.basit.cz.notification.model.NotificationEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class NotificationMapper {

    public static NotificationResponse toResponse(NotificationEntity entity) {
        NotificationResponse r = new NotificationResponse();
        r.id = entity.id;
        r.userId = entity.userId;
        r.eventType = entity.eventType;
        r.locale = entity.locale;
        r.brand = entity.brand;
        r.channels = channelsFromString(entity.channels);
        r.status = entity.status;
        r.novuTransactionId = entity.novuTransactionId;
        r.createdAt = entity.createdAt;
        r.updatedAt = entity.updatedAt;
        return r;
    }

    public static String channelsToString(List<NotificationChannel> channels) {
        if (channels == null || channels.isEmpty()) return "";
        return String.join(",", channels.stream().map(Enum::name).toList());
    }

    public static List<NotificationChannel> channelsFromString(String s) {
        if (s == null || s.isBlank()) return List.of();
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .map(NotificationChannel::valueOf)
                .toList();
    }
}
