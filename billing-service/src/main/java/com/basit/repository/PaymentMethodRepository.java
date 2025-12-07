package com.basit.repository;

import com.basit.constant.PaymentMethodType;
import com.basit.entity.PaymentMethod;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PaymentMethod entity with custom query methods
 */
@ApplicationScoped
public class PaymentMethodRepository implements PanacheRepository<PaymentMethod> {

    /**
     * Find payment method by token
     */
    public Optional<PaymentMethod> findByPaymentToken(String paymentToken) {
        return find("paymentToken", paymentToken).firstResultOptional();
    }

    /**
     * Find all payment methods for a patient
     */
    public List<PaymentMethod> findByPatientId(Long patientId) {
        return find("patientId", Sort.descending("createdAt"), patientId).list();
    }

    /**
     * Find active payment methods for a patient
     */
    public List<PaymentMethod> findActiveByPatientId(Long patientId) {
        return find("patientId = ?1 and isActive = true",
                Sort.descending("createdAt"), patientId)
                .list();
    }

    /**
     * Find default payment method for a patient
     */
    public Optional<PaymentMethod> findDefaultByPatientId(Long patientId) {
        return find("patientId = ?1 and isDefault = true and isActive = true", patientId)
                .firstResultOptional();
    }

    /**
     * Find payment methods by type
     */
    public List<PaymentMethod> findByPaymentType(PaymentMethodType paymentType) {
        return find("paymentType", Sort.descending("createdAt"), paymentType).list();
    }

    /**
     * Find payment methods by type for a patient
     */
    public List<PaymentMethod> findByPatientIdAndType(Long patientId, PaymentMethodType paymentType) {
        return find("patientId = ?1 and paymentType = ?2",
                Sort.descending("createdAt"), patientId, paymentType)
                .list();
    }

    /**
     * Find payment methods by payment gateway
     */
    public List<PaymentMethod> findByPaymentGateway(String gateway) {
        return find("paymentGateway", Sort.descending("createdAt"), gateway).list();
    }

    /**
     * Find expired card payment methods
     */
    public List<PaymentMethod> findExpiredCards() {
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        return find("paymentType in (?1, ?2) and " +
                        "(cardExpiryYear < ?3 or " +
                        "(cardExpiryYear = ?3 and cardExpiryMonth < ?4))",
                PaymentMethodType.CREDIT_CARD, PaymentMethodType.DEBIT_CARD,
                currentYear, currentMonth)
                .list();
    }

    /**
     * Find cards expiring soon (within N months)
     */
    public List<PaymentMethod> findCardsExpiringSoon(int months) {
        LocalDateTime futureDate = LocalDateTime.now().plusMonths(months);
        int futureYear = futureDate.getYear();
        int futureMonth = futureDate.getMonthValue();

        return find("paymentType in (?1, ?2) and isActive = true and " +
                        "(cardExpiryYear < ?3 or " +
                        "(cardExpiryYear = ?3 and cardExpiryMonth <= ?4))",
                PaymentMethodType.CREDIT_CARD, PaymentMethodType.DEBIT_CARD,
                futureYear, futureMonth)
                .list();
    }

    /**
     * Find recently used payment methods (last N days)
     */
    public List<PaymentMethod> findRecentlyUsed(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return find("lastUsedAt >= ?1", Sort.descending("lastUsedAt"), since).list();
    }

    /**
     * Find inactive payment methods
     */
    public List<PaymentMethod> findInactive() {
        return find("isActive = false", Sort.descending("updatedAt")).list();
    }

    /**
     * Find payment methods by card brand
     */
    public List<PaymentMethod> findByCardBrand(String cardBrand) {
        return find("cardBrand", Sort.descending("createdAt"), cardBrand).list();
    }

    /**
     * Find payment methods by last four digits
     */
    public List<PaymentMethod> findByCardLastFour(String lastFour) {
        return find("cardLastFour", Sort.descending("createdAt"), lastFour).list();
    }

    /**
     * Count payment methods for a patient
     */
    public long countByPatientId(Long patientId) {
        return count("patientId", patientId);
    }

    /**
     * Count active payment methods for a patient
     */
    public long countActiveByPatientId(Long patientId) {
        return count("patientId = ?1 and isActive = true", patientId);
    }

    /**
     * Check if patient has any active payment methods
     */
    public boolean hasActivePaymentMethods(Long patientId) {
        return count("patientId = ?1 and isActive = true", patientId) > 0;
    }

    /**
     * Unset default for all patient's payment methods
     */
    public void unsetDefaultForPatient(Long patientId) {
        update("isDefault = false where patientId = ?1", patientId);
    }

    /**
     * Deactivate payment method
     */
    public void deactivatePaymentMethod(Long paymentMethodId) {
        update("isActive = false where id = ?1", paymentMethodId);
    }

    /**
     * Find payment methods not used in N days
     */
    public List<PaymentMethod> findNotUsedSince(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return find("(lastUsedAt is null or lastUsedAt < ?1) and isActive = true",
                Sort.ascending("createdAt"), cutoff)
                .list();
    }

    /**
     * Find payment methods created recently (last N days)
     */
    public List<PaymentMethod> findRecentlyCreated(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return find("createdAt >= ?1", Sort.descending("createdAt"), since).list();
    }
}
