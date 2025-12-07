package com.basit.constant;

/**
 * Represents the lifecycle status of an invoice in the billing system.
 */
public enum InvoiceStatus {
    /**
     * Invoice has been created but not yet sent to the patient
     */
    DRAFT,

    /**
     * Invoice has been issued and sent to the patient
     */
    ISSUED,

    /**
     * Invoice payment is overdue
     */
    OVERDUE,

    /**
     * Partial payment has been received
     */
    PARTIALLY_PAID,

    /**
     * Full payment has been received
     */
    PAID,

    /**
     * Invoice has been cancelled
     */
    CANCELLED,

    /**
     * Invoice has been refunded
     */
    REFUNDED,

    /**
     * Invoice is under dispute
     */
    DISPUTED
}
