package com.basit.constant;

/**
 * Represents the status of a refund transaction.
 */
public enum RefundStatus {
    /**
     * Refund request has been created
     */
    PENDING,

    /**
     * Refund is being processed
     */
    PROCESSING,

    /**
     * Refund has been completed successfully
     */
    COMPLETED,

    /**
     * Refund processing failed
     */
    FAILED,

    /**
     * Refund has been cancelled
     */
    CANCELLED,

    /**
     * Refund requires manual review
     */
    REQUIRES_REVIEW
}
