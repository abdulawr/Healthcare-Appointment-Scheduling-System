package com.basit.cz.analytics.dto;

import java.util.UUID;

public class DoctorUtilizationDTO {
    public UUID doctorId;
    public long totalAppointments;
    public long completedAppointments;
    public long cancelledAppointments;
    public double totalScheduledMinutes;
    public double completionRate;
    public double cancellationRate;
}
