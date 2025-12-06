package com.basit.cz.analytics.api;

import com.basit.cz.analytics.service.AnalyticsService;
import com.basit.cz.analytics.dto.PeakHoursStatsDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/analytics/peak-hours")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("analytics:read")
public class PeakHoursAnalyticsResource {

    @Inject
    AnalyticsService analyticsService;

    /**
     * GET /api/analytics/peak-hours
     * Peak appointment hours analysis.
     */
    @GET
    public List<PeakHoursStatsDTO> getPeakHours() {
        return analyticsService.getPeakHours();
    }
}
