package com.basit.cz.analytics.dto;

import java.math.BigDecimal;

public class SystemOverviewDTO {
    public long totalAppointments;
    public long totalDoctors;
    public long totalPatients;

    public long totalRevenueCents;
    public BigDecimal totalRevenue;

    public java.time.Instant firstAppointment;
    public java.time.Instant lastAppointment;

    public long appointmentsLast24h;
}
