package com.basit.constant;

/**
 * Represents different types of payment methods supported by the system.
 */
public enum PaymentMethodType {
    /**
     * Credit card payment
     */
    CREDIT_CARD,

    /**
     * Debit card payment
     */
    DEBIT_CARD,

    /**
     * Bank account / ACH transfer
     */
    BANK_ACCOUNT,

    /**
     * PayPal payment
     */
    PAYPAL,

    /**
     * Insurance payment
     */
    INSURANCE,

    /**
     * Cash payment
     */
    CASH,

    /**
     * Check payment
     */
    CHECK,

    /**
     * Wire transfer
     */
    WIRE_TRANSFER,

    /**
     * Other payment method
     */
    OTHER
}
