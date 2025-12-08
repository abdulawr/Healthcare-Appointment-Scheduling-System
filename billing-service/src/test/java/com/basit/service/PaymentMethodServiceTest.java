package com.basit.service;

import com.basit.dto.request.PaymentMethodRequest;
import com.basit.dto.response.PaymentMethodResponse;
import com.basit.constant.PaymentMethodType;
import com.basit.repository.PaymentMethodRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class PaymentMethodServiceTest {

    @Inject
    PaymentMethodService paymentMethodService;

    @Inject
    PaymentMethodRepository paymentMethodRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        paymentMethodRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testSavePaymentMethod() {
        PaymentMethodRequest request = new PaymentMethodRequest();
        request.patientId = 1L;
        request.type = PaymentMethodType.CREDIT_CARD;
        request.cardholderName = "John Doe";
        request.lastFourDigits = "4242";
        request.cardBrand = "VISA";
        request.expiryMonth = 12;
        request.expiryYear = 2025;
        request.token = "tok_visa_4242";
        request.isDefault = true;

        PaymentMethodResponse response = paymentMethodService.savePaymentMethod(request);

        assertNotNull(response);
        assertNotNull(response.id);
        assertEquals(PaymentMethodType.CREDIT_CARD, response.type);
        assertEquals("John Doe", response.cardholderName);
        assertEquals("4242", response.lastFourDigits);
        assertEquals("VISA", response.cardBrand);
        assertEquals(12, response.expiryMonth);
        assertEquals(2025, response.expiryYear);
        assertTrue(response.isDefault);
        assertTrue(response.isActive);
        assertNotNull(response.createdAt);
    }

    @Test
    @Transactional
    public void testSaveMultiplePaymentMethodsOnlyOneDefault() {
        // Save first payment method as default
        PaymentMethodRequest request1 = createTestRequest(1L, true);
        PaymentMethodResponse pm1 = paymentMethodService.savePaymentMethod(request1);

        // Save second payment method as default (should unset first)
        PaymentMethodRequest request2 = createTestRequest(1L, true);
        request2.lastFourDigits = "5555";
        PaymentMethodResponse pm2 = paymentMethodService.savePaymentMethod(request2);

        // Verify only second is default
        assertTrue(pm2.isDefault);

        PaymentMethodResponse firstMethod = paymentMethodService.getPaymentMethod(pm1.id);
        assertFalse(firstMethod.isDefault);
    }

    @Test
    @Transactional
    public void testUpdatePaymentMethod() {
        PaymentMethodRequest createRequest = createTestRequest(1L, false);
        PaymentMethodResponse created = paymentMethodService.savePaymentMethod(createRequest);

        // Update
        PaymentMethodRequest updateRequest = new PaymentMethodRequest();
        updateRequest.patientId = 1L;
        updateRequest.type = PaymentMethodType.CREDIT_CARD;
        updateRequest.cardholderName = "Jane Doe"; // Updated
        updateRequest.lastFourDigits = "4242";
        updateRequest.cardBrand = "MASTERCARD"; // Updated
        updateRequest.expiryMonth = 6; // Updated
        updateRequest.expiryYear = 2026; // Updated
        updateRequest.isDefault = true; // Updated

        PaymentMethodResponse updated = paymentMethodService.updatePaymentMethod(
                created.id, updateRequest);

        assertEquals("Jane Doe", updated.cardholderName);
        assertEquals("MASTERCARD", updated.cardBrand);
        assertEquals(6, updated.expiryMonth);
        assertEquals(2026, updated.expiryYear);
        assertTrue(updated.isDefault);
        assertNotNull(updated.updatedAt);
    }

    @Test
    @Transactional
    public void testDeletePaymentMethod() {
        PaymentMethodRequest request = createTestRequest(1L, false);
        PaymentMethodResponse created = paymentMethodService.savePaymentMethod(request);

        // Delete (soft delete)
        paymentMethodService.deletePaymentMethod(created.id);

        // Verify it's marked as inactive
        PaymentMethodResponse deleted = paymentMethodService.getPaymentMethod(created.id);
        assertFalse(deleted.isActive);
    }

    @Test
    @Transactional
    public void testSetAsDefault() {
        // Create two payment methods
        PaymentMethodResponse pm1 = paymentMethodService.savePaymentMethod(
                createTestRequest(1L, true));
        PaymentMethodResponse pm2 = paymentMethodService.savePaymentMethod(
                createTestRequest(1L, false));

        // Set second as default
        PaymentMethodResponse updated = paymentMethodService.setAsDefault(pm2.id, 1L);

        assertTrue(updated.isDefault);

        // Verify first is no longer default
        PaymentMethodResponse first = paymentMethodService.getPaymentMethod(pm1.id);
        assertFalse(first.isDefault);
    }

    @Test
    @Transactional
    public void testSetAsDefaultWrongPatient() {
        PaymentMethodResponse pm = paymentMethodService.savePaymentMethod(
                createTestRequest(1L, false));

        assertThrows(IllegalArgumentException.class, () -> {
            paymentMethodService.setAsDefault(pm.id, 999L);
        }, "Payment method does not belong to patient");
    }

    @Test
    @Transactional
    public void testGetPaymentMethod() {
        PaymentMethodRequest request = createTestRequest(1L, false);
        PaymentMethodResponse created = paymentMethodService.savePaymentMethod(request);

        PaymentMethodResponse retrieved = paymentMethodService.getPaymentMethod(created.id);

        assertNotNull(retrieved);
        assertEquals(created.id, retrieved.id);
        assertEquals(created.cardholderName, retrieved.cardholderName);
    }

    @Test
    @Transactional
    public void testGetPatientPaymentMethods() {
        // Create multiple payment methods for patient 1
        paymentMethodService.savePaymentMethod(createTestRequest(1L, true));
        paymentMethodService.savePaymentMethod(createTestRequest(1L, false));

        // Create one for patient 2
        paymentMethodService.savePaymentMethod(createTestRequest(2L, false));

        List<PaymentMethodResponse> patient1Methods =
                paymentMethodService.getPatientPaymentMethods(1L);

        assertEquals(2, patient1Methods.size());
        assertTrue(patient1Methods.stream().allMatch(pm -> pm.patientId.equals(1L)));
        assertTrue(patient1Methods.stream().allMatch(pm -> pm.isActive));
    }

    @Test
    @Transactional
    public void testGetDefaultPaymentMethod() {
        // Create non-default method
        paymentMethodService.savePaymentMethod(createTestRequest(1L, false));

        // Create default method
        PaymentMethodResponse defaultPm = paymentMethodService.savePaymentMethod(
                createTestRequest(1L, true));

        PaymentMethodResponse retrieved =
                paymentMethodService.getDefaultPaymentMethod(1L);

        assertNotNull(retrieved);
        assertEquals(defaultPm.id, retrieved.id);
        assertTrue(retrieved.isDefault);
    }

    @Test
    @Transactional
    public void testGetDefaultPaymentMethodNotFound() {
        assertThrows(jakarta.ws.rs.NotFoundException.class, () -> {
            paymentMethodService.getDefaultPaymentMethod(999L);
        });
    }

    @Test
    @Transactional
    public void testGetPaymentMethodByToken() {
        PaymentMethodRequest request = createTestRequest(1L, false);
        request.token = "unique_token_123";
        PaymentMethodResponse created = paymentMethodService.savePaymentMethod(request);

        PaymentMethodResponse retrieved =
                paymentMethodService.getPaymentMethodByToken("unique_token_123");

        assertNotNull(retrieved);
        assertEquals(created.id, retrieved.id);
        assertEquals("unique_token_123", retrieved.cardholderName); // Note: token not exposed in response
    }

    @Test
    @Transactional
    public void testSavePaymentMethodWithDifferentTypes() {
        // Credit card
        PaymentMethodRequest ccRequest = createTestRequest(1L, false);
        ccRequest.type = PaymentMethodType.CREDIT_CARD;
        PaymentMethodResponse cc = paymentMethodService.savePaymentMethod(ccRequest);

        // Debit card
        PaymentMethodRequest dcRequest = createTestRequest(1L, false);
        dcRequest.type = PaymentMethodType.DEBIT_CARD;
        dcRequest.lastFourDigits = "5555";
        PaymentMethodResponse dc = paymentMethodService.savePaymentMethod(dcRequest);

        // PayPal
        PaymentMethodRequest ppRequest = createTestRequest(1L, false);
        ppRequest.type = PaymentMethodType.PAYPAL;
        ppRequest.lastFourDigits = "6666";
        PaymentMethodResponse pp = paymentMethodService.savePaymentMethod(ppRequest);

        List<PaymentMethodResponse> methods =
                paymentMethodService.getPatientPaymentMethods(1L);

        assertEquals(3, methods.size());
        assertEquals(1, methods.stream()
                .filter(m -> m.type == PaymentMethodType.CREDIT_CARD).count());
        assertEquals(1, methods.stream()
                .filter(m -> m.type == PaymentMethodType.DEBIT_CARD).count());
        assertEquals(1, methods.stream()
                .filter(m -> m.type == PaymentMethodType.PAYPAL).count());
    }

    @Test
    @Transactional
    public void testDeletedMethodsNotReturnedInPatientList() {
        PaymentMethodResponse pm1 = paymentMethodService.savePaymentMethod(
                createTestRequest(1L, false));
        PaymentMethodResponse pm2 = paymentMethodService.savePaymentMethod(
                createTestRequest(1L, false));

        // Delete first method
        paymentMethodService.deletePaymentMethod(pm1.id);

        List<PaymentMethodResponse> activeMethods =
                paymentMethodService.getPatientPaymentMethods(1L);

        // Should only return active methods
        assertEquals(1, activeMethods.size());
        assertEquals(pm2.id, activeMethods.get(0).id);
    }

    private PaymentMethodRequest createTestRequest(Long patientId, Boolean isDefault) {
        PaymentMethodRequest request = new PaymentMethodRequest();
        request.patientId = patientId;
        request.type = PaymentMethodType.CREDIT_CARD;
        request.cardholderName = "Test User";
        request.lastFourDigits = "4242";
        request.cardBrand = "VISA";
        request.expiryMonth = 12;
        request.expiryYear = 2025;
        request.token = "tok_test_" + System.currentTimeMillis();
        request.isDefault = isDefault;
        return request;
    }
}
