package com.basit.cz.analytics.api;

import com.basit.cz.analytics.service.AnalyticsService;
import com.basit.cz.analytics.dto.SystemOverviewDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/analytics/system")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("analytics:read")
public class SystemOverviewResource {

    @Inject
    AnalyticsService analyticsService;

    /**
     * GET /api/analytics/system/overview
     * Overall system health / usage summary.
     */
    @GET
    @Path("/overview")
    public SystemOverviewDTO getSystemOverview() {
        return analyticsService.getSystemOverview();
    }
}
