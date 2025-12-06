package com.basit.cz.analytics.dto;

import java.util.UUID;
import java.math.BigDecimal;

public class DoctorPerformanceDTO {
    public UUID doctorId;
    public long totalAppointments;
    public long completedAppointments;
    public long cancelledAppointments;
    public long totalRevenueCents;
    public BigDecimal totalRevenue;
    public double completionRate;
    public double cancellationRate;
}
