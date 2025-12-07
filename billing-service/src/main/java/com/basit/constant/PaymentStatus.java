package com.basit.constant;

/**
 * Represents the status of a payment transaction.
 */
public enum PaymentStatus {
    /**
     * Payment is being processed
     */
    PENDING,

    /**
     * Payment has been successfully processed
     */
    COMPLETED,

    /**
     * Payment processing failed
     */
    FAILED,

    /**
     * Payment has been refunded
     */
    REFUNDED,

    /**
     * Payment has been partially refunded
     */
    PARTIALLY_REFUNDED,

    /**
     * Payment is on hold (fraud detection, verification needed)
     */
    ON_HOLD,

    /**
     * Payment was cancelled before completion
     */
    CANCELLED,

    /**
     * Payment is being processed by the gateway
     */
    PROCESSING
}
