package com.basit.cz.analytics.api;

import com.basit.cz.analytics.service.AnalyticsService;
import com.basit.cz.analytics.dto.DoctorPerformanceDTO;
import com.basit.cz.analytics.dto.DoctorUtilizationDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class DoctorAnalyticsResourceTest {

    @InjectMock
    AnalyticsService analyticsService;

    @Test
    @TestSecurity(user = "test-user", roles = {"analytics:read"})
    void getDoctorUtilization_returnsExpectedMetrics() {
        // Arrange
        UUID doctorId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        DoctorUtilizationDTO dto = new DoctorUtilizationDTO();
        dto.doctorId = doctorId;
        dto.totalAppointments = 10;
        dto.completedAppointments = 8;
        dto.cancelledAppointments = 2;
        dto.totalScheduledMinutes = 300;
        dto.completionRate = 0.8;
        dto.cancellationRate = 0.2;

        when(analyticsService.getDoctorUtilization(doctorId)).thenReturn(dto);

        // Act + Assert
        given()
                .when()
                .get("/api/analytics/doctor/" + doctorId)
                .then()
                .statusCode(200)
                .body("doctorId", is(doctorId.toString()))
                .body("totalAppointments", is(10))
                .body("completedAppointments", is(8))
                .body("cancelledAppointments", is(2))
                .body("totalScheduledMinutes", is(300.0f))
                .body("completionRate", is(0.8f))
                .body("cancellationRate", is(0.2f));
    }

    @Test
    @TestSecurity(user = "test-user", roles = {"analytics:read"})
    void getDoctorsPerformance_returnsListOfDoctors() {
        // Arrange
        DoctorPerformanceDTO d1 = new DoctorPerformanceDTO();
        d1.doctorId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        d1.totalAppointments = 20;
        d1.completedAppointments = 18;
        d1.cancelledAppointments = 2;
        d1.totalRevenueCents = 500_00;
        d1.totalRevenue = BigDecimal.valueOf(500);
        d1.completionRate = 0.9;
        d1.cancellationRate = 0.1;

        DoctorPerformanceDTO d2 = new DoctorPerformanceDTO();
        d2.doctorId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        d2.totalAppointments = 15;
        d2.completedAppointments = 12;
        d2.cancelledAppointments = 3;
        d2.totalRevenueCents = 300_00;
        d2.totalRevenue = BigDecimal.valueOf(300.0f);
        d2.completionRate = 0.8;
        d2.cancellationRate = 0.2;

        when(analyticsService.getDoctorsPerformance()).thenReturn(List.of(d1, d2));

        // Act + Assert
        given()
                .when()
                .get("/api/analytics/doctors/performance")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].doctorId", is(d1.doctorId.toString()))
                .body("[0].totalAppointments", is(20))
                .body("[0].totalRevenueCents", is(50000))
                .body("[0].totalRevenue", is(500))
                .body("[1].doctorId", is(d2.doctorId.toString()))
                .body("[1].totalAppointments", is(15))
                .body("[1].totalRevenueCents", is(30000))
                .body("[1].totalRevenue", is(300.0f));
    }

    @Test
    @TestSecurity(user = "test-user", roles = {"analytics:read"})
    void getDoctorsPerformance_returnsEmptyListWhenNoData() {
        // Arrange
        when(analyticsService.getDoctorsPerformance()).thenReturn(List.of());

        // Act + Assert
        given()
                .when()
                .get("/api/analytics/doctors/performance")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }
}
