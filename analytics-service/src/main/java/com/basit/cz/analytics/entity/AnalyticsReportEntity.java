package com.basit.cz.analytics.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "analytics_report")
public class AnalyticsReportEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "report_id", nullable = false, unique = true)
    public Long reportId;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false, columnDefinition = "jsonb")
    public String parameters;  // serialized JSON

    @Column(nullable = false)
    public String status;  // PENDING, COMPLETED, FAILED

    @Column(columnDefinition = "jsonb")
    public String result;

    @Column(name = "created_at")
    public OffsetDateTime createdAt;

    @Column(name = "updated_at")
    public OffsetDateTime updatedAt;
}
