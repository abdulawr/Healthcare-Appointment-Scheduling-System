-- V6: Create payment_methods table
CREATE TABLE payment_methods (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    payment_type VARCHAR(30) NOT NULL,
    payment_token VARCHAR(200) UNIQUE NOT NULL,
    payment_gateway VARCHAR(50) NOT NULL,
    card_last_four VARCHAR(4),
    card_brand VARCHAR(50),
    card_expiry_month INTEGER CHECK (card_expiry_month BETWEEN 1 AND 12),
    card_expiry_year INTEGER CHECK (card_expiry_year >= 2020),
    bank_name VARCHAR(200),
    account_last_four VARCHAR(4),
    billing_address VARCHAR(500),
    billing_zip_code VARCHAR(20),
    is_default BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    nickname VARCHAR(100),
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create indexes for payment_methods table
CREATE INDEX idx_payment_method_patient ON payment_methods(patient_id);
CREATE INDEX idx_payment_method_type ON payment_methods(payment_type);
CREATE INDEX idx_payment_method_gateway ON payment_methods(payment_gateway);
CREATE INDEX idx_payment_method_is_default ON payment_methods(patient_id, is_default) WHERE is_default = true;
CREATE INDEX idx_payment_method_is_active ON payment_methods(patient_id, is_active) WHERE is_active = true;
CREATE INDEX idx_payment_method_card_brand ON payment_methods(card_brand);
CREATE INDEX idx_payment_method_last_used ON payment_methods(last_used_at);

-- Add comments
COMMENT ON TABLE payment_methods IS 'Stores tokenized payment methods for patients (PCI compliant)';
COMMENT ON COLUMN payment_methods.payment_type IS 'CREDIT_CARD, DEBIT_CARD, BANK_ACCOUNT, PAYPAL, etc.';
COMMENT ON COLUMN payment_methods.payment_token IS 'Tokenized payment data from gateway (never stores raw card numbers)';
COMMENT ON COLUMN payment_methods.card_last_four IS 'Last 4 digits of card for display purposes';
COMMENT ON COLUMN payment_methods.is_default IS 'Whether this is the default payment method for the patient';
COMMENT ON COLUMN payment_methods.is_active IS 'Whether this payment method is still active/valid';

-- Add constraint to ensure only one default per patient
CREATE UNIQUE INDEX idx_payment_method_one_default_per_patient 
    ON payment_methods(patient_id) 
    WHERE is_default = true AND is_active = true;
