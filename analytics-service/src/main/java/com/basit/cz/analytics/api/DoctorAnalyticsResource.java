package com.basit.cz.analytics.api;

import com.basit.cz.analytics.service.AnalyticsService;
import com.basit.cz.analytics.dto.DoctorPerformanceDTO;
import com.basit.cz.analytics.dto.DoctorUtilizationDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/api/analytics")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("analytics:read")
public class DoctorAnalyticsResource {

    @Inject
    AnalyticsService analyticsService;

    /**
     * GET /api/analytics/doctor/{id}
     * Doctor utilization metrics for a single doctor.
     */
    @GET
    @Path("/doctor/{id}")
    public DoctorUtilizationDTO getDoctorUtilization(@PathParam("id") String id) {
        UUID doctorId = UUID.fromString(id);
        return analyticsService.getDoctorUtilization(doctorId);
    }

    /**
     * GET /api/analytics/doctors/performance
     * Performance comparison across all doctors.
     */
    @GET
    @Path("/doctors/performance")
    public List<DoctorPerformanceDTO> getDoctorsPerformance() {
        return analyticsService.getDoctorsPerformance();
    }
}
