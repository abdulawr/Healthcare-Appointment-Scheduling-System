package com.basit.rest;

import com.basit.dto.request.InvoiceCreateRequest;
import com.basit.repository.InvoiceRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class InvoiceResourceTest {

    @Inject
    InvoiceRepository invoiceRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        invoiceRepository.deleteAll();
    }

    @Test
    public void testCreateInvoice() {
        InvoiceCreateRequest request = createTestRequest();

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/invoices")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("appointmentId", equalTo(1))
                .body("total", notNullValue());
    }

    @Test
    public void testGetInvoice() {
        InvoiceCreateRequest request = createTestRequest();

        Long invoiceId = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/invoices")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .pathParam("id", invoiceId)
                .when()
                .get("/api/billing/invoices/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(invoiceId.intValue()));
    }

    @Test
    public void testGetNonExistentInvoice() {
        given()
                .pathParam("id", 99999)
                .when()
                .get("/api/billing/invoices/{id}")
                .then()
                .statusCode(404);
    }

    private InvoiceCreateRequest createTestRequest() {
        InvoiceCreateRequest request = new InvoiceCreateRequest();
        request.appointmentId = 1L;
        request.patientId = 1L;

        List<InvoiceCreateRequest.InvoiceItemRequest> items = new ArrayList<>();
        InvoiceCreateRequest.InvoiceItemRequest item =
                new InvoiceCreateRequest.InvoiceItemRequest();
        item.description = "Consultation";
        item.quantity = 1;
        item.unitPrice = new BigDecimal("100.00");
        items.add(item);

        request.items = items;
        return request;
    }
}
