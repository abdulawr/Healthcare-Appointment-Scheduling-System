-- V4: Create refunds table
CREATE TABLE refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    invoice_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    refund_amount DECIMAL(10, 2) NOT NULL CHECK (refund_amount > 0),
    status VARCHAR(30) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    refund_transaction_id VARCHAR(100) UNIQUE,
    payment_gateway VARCHAR(50),
    gateway_response TEXT,
    processed_at TIMESTAMP,
    failed_reason VARCHAR(500),
    requested_by VARCHAR(100),
    approved_by VARCHAR(100),
    approved_at TIMESTAMP,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create indexes for refunds table
CREATE INDEX idx_refund_payment ON refunds(payment_id);
CREATE INDEX idx_refund_invoice ON refunds(invoice_id);
CREATE INDEX idx_refund_patient ON refunds(patient_id);
CREATE INDEX idx_refund_status ON refunds(status);
CREATE INDEX idx_refund_transaction_id ON refunds(refund_transaction_id);
CREATE INDEX idx_refund_requested_by ON refunds(requested_by);
CREATE INDEX idx_refund_approved_by ON refunds(approved_by);

-- Add comments
COMMENT ON TABLE refunds IS 'Stores refund transactions for payments';
COMMENT ON COLUMN refunds.status IS 'PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REQUIRES_REVIEW';
COMMENT ON COLUMN refunds.reason IS 'Reason for the refund request';
COMMENT ON COLUMN refunds.approved_by IS 'User who approved the refund (for large amounts)';
