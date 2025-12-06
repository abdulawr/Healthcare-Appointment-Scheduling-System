package com.basit.cz.analytics.dto;

import java.time.LocalDate;

public class DailyAppointmentsStatDTO {
    public LocalDate date;
    public long total;
    public long completed;
    public long cancelled;
}
