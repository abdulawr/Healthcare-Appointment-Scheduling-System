-- V3: Create payments table
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    payment_gateway VARCHAR(50) NOT NULL,
    gateway_response TEXT,
    payment_method_id BIGINT,
    idempotency_key VARCHAR(100) UNIQUE NOT NULL,
    processed_at TIMESTAMP,
    failed_reason VARCHAR(500),
    notes VARCHAR(1000),
    refunded_amount DECIMAL(10, 2) NOT NULL DEFAULT 0 CHECK (refunded_amount >= 0),
    is_refundable BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create indexes for payments table
CREATE INDEX idx_payment_invoice ON payments(invoice_id);
CREATE INDEX idx_payment_patient ON payments(patient_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_transaction_id ON payments(transaction_id);
CREATE INDEX idx_payment_processed_at ON payments(processed_at);
CREATE INDEX idx_payment_gateway ON payments(payment_gateway);
CREATE INDEX idx_payment_method_id ON payments(payment_method_id);

-- Add comments
COMMENT ON TABLE payments IS 'Stores payment transactions for invoices';
COMMENT ON COLUMN payments.status IS 'PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, PARTIALLY_REFUNDED, ON_HOLD, CANCELLED';
COMMENT ON COLUMN payments.payment_method IS 'CREDIT_CARD, DEBIT_CARD, BANK_ACCOUNT, PAYPAL, INSURANCE, CASH, CHECK, WIRE_TRANSFER, OTHER';
COMMENT ON COLUMN payments.idempotency_key IS 'Unique key to prevent duplicate payments';
COMMENT ON COLUMN payments.gateway_response IS 'Raw response from payment gateway for audit trail';
