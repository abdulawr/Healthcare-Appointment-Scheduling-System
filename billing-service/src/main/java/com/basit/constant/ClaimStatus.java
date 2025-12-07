package com.basit.constant;

/**
 * Represents the status of an insurance claim.
 */
public enum ClaimStatus {
    /**
     * Claim has been created but not yet submitted
     */
    DRAFT,

    /**
     * Claim has been submitted to insurance company
     */
    SUBMITTED,

    /**
     * Claim is under review by insurance company
     */
    IN_REVIEW,

    /**
     * Additional information requested by insurance
     */
    INFO_REQUESTED,

    /**
     * Claim has been approved
     */
    APPROVED,

    /**
     * Claim has been partially approved
     */
    PARTIALLY_APPROVED,

    /**
     * Claim has been denied
     */
    DENIED,

    /**
     * Claim payment has been received
     */
    PAID,

    /**
     * Claim has been appealed after denial
     */
    APPEALED,

    /**
     * Claim has been cancelled
     */
    CANCELLED
}
