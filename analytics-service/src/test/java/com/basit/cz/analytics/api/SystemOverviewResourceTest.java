package com.basit.cz.analytics.api;

import com.basit.cz.analytics.service.AnalyticsService;
import com.basit.cz.analytics.dto.SystemOverviewDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class SystemOverviewResourceTest {

    @InjectMock
    AnalyticsService analyticsService;

    @Test
    @TestSecurity(user = "test-user", roles = {"analytics:read"})
    void getSystemOverview_returnsExpectedSummary() {
        // Arrange
        SystemOverviewDTO dto = new SystemOverviewDTO();
        dto.totalAppointments = 100;
        dto.totalDoctors = 5;
        dto.totalPatients = 80;
        dto.totalRevenueCents = 123_456;
        dto.totalRevenue = BigDecimal.valueOf(1234.56);
        dto.firstAppointment = Instant.parse("2025-01-01T09:00:00Z");
        dto.lastAppointment = Instant.parse("2025-01-31T17:30:00Z");
        dto.appointmentsLast24h = 12;

        when(analyticsService.getSystemOverview()).thenReturn(dto);

        // Act + Assert
        given()
                .when()
                .get("/api/analytics/system/overview")
                .then()
                .statusCode(200)
                .body("totalAppointments", is(100))
                .body("totalDoctors", is(5))
                .body("totalPatients", is(80))
                .body("totalRevenueCents", is(123456))
                .body("totalRevenue", is(1234.56f))
                .body("appointmentsLast24h", is(12))
                // Dates are serialized as ISO strings; we just check they exist and are non-empty
                .body("firstAppointment", notNullValue())
                .body("lastAppointment", notNullValue());
    }

    @Test
    @TestSecurity(user = "test-user", roles = {"analytics:read"})
    void getSystemOverview_handlesEmptySystem() {
        // Arrange: everything zero / null
        SystemOverviewDTO dto = new SystemOverviewDTO();
        dto.totalAppointments = 0;
        dto.totalDoctors = 0;
        dto.totalPatients = 0;
        dto.totalRevenueCents = 0;
        dto.totalRevenue = BigDecimal.ZERO;
        dto.firstAppointment = null;
        dto.lastAppointment = null;
        dto.appointmentsLast24h = 0;

        when(analyticsService.getSystemOverview()).thenReturn(dto);

        // Act + Assert
        given()
                .when()
                .get("/api/analytics/system/overview")
                .then()
                .statusCode(200)
                .body("totalAppointments", is(0))
                .body("totalDoctors", is(0))
                .body("totalPatients", is(0))
                .body("totalRevenueCents", is(0))
                .body("totalRevenue", is(0))
                .body("appointmentsLast24h", is(0))
                .body("firstAppointment", nullValue())
                .body("lastAppointment", nullValue());
    }
}
