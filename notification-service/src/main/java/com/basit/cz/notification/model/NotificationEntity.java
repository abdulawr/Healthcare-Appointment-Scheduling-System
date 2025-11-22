package com.basit.cz.notification.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification")
public class NotificationEntity extends PanacheEntityBase {

    @Id
    @Column(columnDefinition = "uuid")
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public String userId;

    @Column(name = "event_type", nullable = false)
    public String eventType;

    @Column(name = "locale")
    public String locale;

    @Column(name = "brand")
    public String brand;

    @Column(name = "channels", nullable = false)
    public String channels; // comma separated list of channels

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "notification_status")
    public NotificationStatus status;

    @Column(name = "novu_transaction_id")
    public String novuTransactionId;

    @Column(name = "idempotency_key", unique = true)
    public String idempotencyKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "jsonb")
    public String payloadJson;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public OffsetDateTime updatedAt;
}
