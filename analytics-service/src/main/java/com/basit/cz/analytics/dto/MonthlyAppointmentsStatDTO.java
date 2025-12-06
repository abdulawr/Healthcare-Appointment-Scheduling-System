package com.basit.cz.analytics.dto;

import java.time.LocalDate;

public class MonthlyAppointmentsStatDTO {
    public LocalDate monthStart;
    public long total;
    public long completed;
    public long cancelled;
}
