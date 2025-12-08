package com.basit.cz.analytics.api;

import com.basit.cz.analytics.dto.DailyAppointmentsStatDTO;
import com.basit.cz.analytics.dto.MonthlyAppointmentsStatDTO;
import com.basit.cz.analytics.dto.WeeklyAppointmentsStatDTO;
import com.basit.cz.analytics.service.AnalyticsService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/analytics/appointments")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("analytics:read")
public class AppointmentsAnalyticsResource {

    @Inject
    AnalyticsService analyticsService;

    @GET
    @Path("/daily")
    public List<DailyAppointmentsStatDTO> daily() {
        return analyticsService.getDailyAppointments();
    }

    @GET
    @Path("/weekly")
    public List<WeeklyAppointmentsStatDTO> weekly() {
        return analyticsService.getWeeklyAppointments();
    }

    @GET
    @Path("/monthly")
    public List<MonthlyAppointmentsStatDTO> monthly() {
        return analyticsService.getMonthlyAppointments();
    }
}
