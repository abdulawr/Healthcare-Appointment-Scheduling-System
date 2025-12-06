package com.basit.cz.analytics.dto;

import java.time.LocalDate;

public class WeeklyAppointmentsStatDTO {
    public LocalDate weekStart;
    public long total;
    public long completed;
    public long cancelled;
}