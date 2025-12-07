package com.basit.rest;

import com.basit.dto.request.AddInvoiceItemRequest;
import com.basit.dto.request.CreateInvoiceRequest;
import com.basit.repository.InvoiceRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InvoiceResourceTest {

    @Inject
    InvoiceRepository invoiceRepository;

    private static Long createdInvoiceId;

    @BeforeEach
    @Transactional
    void setUp() {
        invoiceRepository.deleteAll();
    }

    @Test
    @Order(1)
    void testCreateInvoice() {
        CreateInvoiceRequest request = createInvoiceRequest();

        createdInvoiceId = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/invoices")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("invoiceNumber", equalTo(request.invoiceNumber))
                .body("status", equalTo("DRAFT"))
                .body("patientId", equalTo(request.patientId.intValue()))
                .extract()
                .path("id");
    }

    @Test
    @Order(2)
    void testCreateInvoice_InvalidRequest() {
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        // Missing required fields

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/billing/invoices")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(3)
    void testGetInvoiceById() {
        // First create an invoice
        CreateInvoiceRequest request = createInvoiceRequest();
        Integer idInt = given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/billing/invoices")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
        Long id = idInt.longValue();


        // Then get it
        given()
                .when()
                .get("/api/billing/invoices/" + id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id.intValue()))
                .body("invoiceNumber", equalTo(request.invoiceNumber));
    }

    @Test
    @Order(4)
    void testGetInvoiceById_NotFound() {
        given()
                .when()
                .get("/api/billing/invoices/99999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(5)
    void testGetInvoicesByPatient() {
        // Create invoices for patient
        Long patientId = 1L;
        CreateInvoiceRequest request1 = createInvoiceRequest();
        request1.patientId = patientId;

        given()
                .contentType(ContentType.JSON)
                .body(request1)
                .post("/api/billing/invoices");

        CreateInvoiceRequest request2 = createInvoiceRequest();
        request2.patientId = patientId;
        request2.invoiceNumber = "INV-" + System.currentTimeMillis();

        given()
                .contentType(ContentType.JSON)
                .body(request2)
                .post("/api/billing/invoices");

        // Get invoices for patient
        given()
                .when()
                .get("/api/billing/invoices/patient/" + patientId)
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @Order(6)
    void testAddItemToInvoice() {
        // Create invoice
        CreateInvoiceRequest request = createInvoiceRequest();
        Long invoiceId = given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/billing/invoices")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Add item
        AddInvoiceItemRequest itemRequest = new AddInvoiceItemRequest();
        itemRequest.description = "Lab Test";
        itemRequest.serviceCode = "LAB-001";
        itemRequest.quantity = 1;
        itemRequest.unitPrice = new BigDecimal("75.00");

        given()
                .contentType(ContentType.JSON)
                .body(itemRequest)
                .when()
                .post("/api/billing/invoices/" + invoiceId + "/items")
                .then()
                .statusCode(200)
                .body("items.size()", greaterThan(0));
    }

    @Test
    @Order(7)
    void testIssueInvoice() {
        // Create invoice with items
        CreateInvoiceRequest request = createInvoiceRequest();
        CreateInvoiceRequest.InvoiceItemRequest item = new CreateInvoiceRequest.InvoiceItemRequest();
        item.description = "Service";
        item.quantity = 1;
        item.unitPrice = new BigDecimal("100.00");
        request.items = new ArrayList<>();
        request.items.add(item);

        Long invoiceId = given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/billing/invoices")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Issue invoice
        given()
                .when()
                .post("/api/billing/invoices/" + invoiceId + "/issue")
                .then()
                .statusCode(200)
                .body("status", equalTo("ISSUED"));
    }

    @Test
    @Order(8)
    void testCancelInvoice() {
        // Create invoice
        CreateInvoiceRequest request = createInvoiceRequest();
        Long invoiceId = given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/billing/invoices")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Cancel invoice
        given()
                .queryParam("reason", "Customer request")
                .when()
                .post("/api/billing/invoices/" + invoiceId + "/cancel")
                .then()
                .statusCode(200)
                .body("status", equalTo("CANCELLED"));
    }

    @Test
    @Order(9)
    void testDeleteInvoice() {
        // Create invoice
        CreateInvoiceRequest request = createInvoiceRequest();
        Long invoiceId = given()
                .contentType(ContentType.JSON)
                .body(request)
                .post("/api/billing/invoices")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Delete invoice
        given()
                .when()
                .delete("/api/billing/invoices/" + invoiceId)
                .then()
                .statusCode(204);

        // Verify deleted
        given()
                .when()
                .get("/api/billing/invoices/" + invoiceId)
                .then()
                .statusCode(404);
    }

    @Test
    @Order(10)
    void testGetOutstandingAmount() {
        Long patientId = 1L;

        given()
                .when()
                .get("/api/billing/invoices/patient/" + patientId + "/outstanding")
                .then()
                .statusCode(200)
                .body("outstandingAmount", notNullValue());
    }

    private CreateInvoiceRequest createInvoiceRequest() {
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.appointmentId = 1L;
        request.patientId = 1L;
        request.doctorId = 1L;  // ✅ Add this if missing
        request.invoiceNumber = "INV-" + System.currentTimeMillis();
        request.issueDate = LocalDate.now();
        request.dueDate = LocalDate.now().plusDays(30);
        request.taxAmount = new BigDecimal("10.00");
        request.discountAmount = BigDecimal.ZERO;
        request.notes = "Test invoice";  // ✅ Add if needed
        return request;
    }
}

