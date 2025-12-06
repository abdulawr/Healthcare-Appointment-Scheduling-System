package com.basit.cz.analytics.api;

import com.basit.cz.analytics.service.AnalyticsService;
import com.basit.cz.analytics.dto.PeakHoursStatsDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class PeakHoursAnalyticsResourceTest {

    @InjectMock
    AnalyticsService analyticsService;

    @Test
    @TestSecurity(user = "test-user", roles = {"analytics:read"})
    void getPeakHours_returnsHourBuckets() {
        // Arrange
        PeakHoursStatsDTO h9 = new PeakHoursStatsDTO();
        h9.hourOfDay = 9;
        h9.totalAppointments = 5;

        PeakHoursStatsDTO h10 = new PeakHoursStatsDTO();
        h10.hourOfDay = 10;
        h10.totalAppointments = 8;

        PeakHoursStatsDTO h11 = new PeakHoursStatsDTO();
        h11.hourOfDay = 11;
        h11.totalAppointments = 3;

        when(analyticsService.getPeakHours()).thenReturn(List.of(h9, h10, h11));

        // Act + Assert
        given()
                .when()
                .get("/api/analytics/peak-hours")
                .then()
                .statusCode(200)
                .body("$", hasSize(3))
                .body("[0].hourOfDay", is(9))
                .body("[0].totalAppointments", is(5))
                .body("[1].hourOfDay", is(10))
                .body("[1].totalAppointments", is(8))
                .body("[2].hourOfDay", is(11))
                .body("[2].totalAppointments", is(3));
    }

    @Test
    @TestSecurity(user = "test-user", roles = {"analytics:read"})
    void getPeakHours_returnsEmptyListWhenNoData() {
        // Arrange
        when(analyticsService.getPeakHours()).thenReturn(List.of());

        // Act + Assert
        given()
                .when()
                .get("/api/analytics/peak-hours")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }
}
