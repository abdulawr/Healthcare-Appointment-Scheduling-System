package com.basit.cz.analytics.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "fact_appointment")
public class FactAppointment extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "appointment_id", nullable = false)
    public UUID appointmentId;

    @Column(name = "doctor_id", nullable = false)
    public UUID doctorId;

    @Column(name = "patient_id", nullable = false)
    public UUID patientId;

    @Column(nullable = false)
    public String status;  // BOOKED, COMPLETED, CANCELLED

    @Column(name = "start_time", nullable = false)
    public OffsetDateTime startTime;

    @Column(name = "end_time")
    public OffsetDateTime endTime;

    @Column(name = "booking_time", nullable = false)
    public OffsetDateTime bookingTime;

    @Column(name = "cancellation_time")
    public OffsetDateTime cancellationTime;

    @Column(name = "cancellation_reason")
    public String cancellationReason;

    @Column(name = "price_cents")
    public Long priceCents;

    @Column(name = "source_service")
    public String sourceService;

    @Column(name = "event_type", nullable = false)
    public String eventType;  // CREATED, UPDATED, CANCELLED, COMPLETED

    @Column(name = "event_time", nullable = false)
    public OffsetDateTime eventTime;

    @Column(name = "created_at")
    public OffsetDateTime createdAt;
}
