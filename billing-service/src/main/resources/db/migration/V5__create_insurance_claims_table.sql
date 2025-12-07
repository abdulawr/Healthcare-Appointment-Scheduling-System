-- V5: Create insurance_claims table
CREATE TABLE insurance_claims (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    claim_number VARCHAR(50) UNIQUE NOT NULL,
    insurance_provider VARCHAR(200) NOT NULL,
    policy_number VARCHAR(100) NOT NULL,
    group_number VARCHAR(100),
    claim_amount DECIMAL(10, 2) NOT NULL CHECK (claim_amount > 0),
    approved_amount DECIMAL(10, 2) CHECK (approved_amount >= 0),
    paid_amount DECIMAL(10, 2) NOT NULL DEFAULT 0 CHECK (paid_amount >= 0),
    patient_responsibility DECIMAL(10, 2) CHECK (patient_responsibility >= 0),
    status VARCHAR(30) NOT NULL,
    submission_date DATE NOT NULL,
    processed_date DATE,
    paid_date DATE,
    denial_reason VARCHAR(1000),
    diagnosis_codes VARCHAR(500),
    procedure_codes VARCHAR(500),
    provider_npi VARCHAR(20),
    facility_code VARCHAR(50),
    notes VARCHAR(2000),
    external_claim_id VARCHAR(100),
    appeal_count INTEGER NOT NULL DEFAULT 0,
    last_appeal_date DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create indexes for insurance_claims table
CREATE INDEX idx_claim_invoice ON insurance_claims(invoice_id);
CREATE INDEX idx_claim_patient ON insurance_claims(patient_id);
CREATE INDEX idx_claim_status ON insurance_claims(status);
CREATE INDEX idx_claim_provider ON insurance_claims(insurance_provider);
CREATE INDEX idx_claim_policy_number ON insurance_claims(policy_number);
CREATE INDEX idx_claim_submission_date ON insurance_claims(submission_date);
CREATE INDEX idx_claim_processed_date ON insurance_claims(processed_date);
CREATE INDEX idx_claim_provider_npi ON insurance_claims(provider_npi);
CREATE INDEX idx_claim_external_id ON insurance_claims(external_claim_id);

-- Add comments
COMMENT ON TABLE insurance_claims IS 'Stores insurance claims for invoices';
COMMENT ON COLUMN insurance_claims.status IS 'DRAFT, SUBMITTED, IN_REVIEW, INFO_REQUESTED, APPROVED, PARTIALLY_APPROVED, DENIED, PAID, APPEALED, CANCELLED';
COMMENT ON COLUMN insurance_claims.claim_number IS 'Internal claim tracking number';
COMMENT ON COLUMN insurance_claims.external_claim_id IS 'Claim ID from insurance company system';
COMMENT ON COLUMN insurance_claims.patient_responsibility IS 'Amount patient owes after insurance (claim_amount - approved_amount)';
COMMENT ON COLUMN insurance_claims.diagnosis_codes IS 'ICD-10 diagnosis codes';
COMMENT ON COLUMN insurance_claims.procedure_codes IS 'CPT procedure codes';
COMMENT ON COLUMN insurance_claims.provider_npi IS 'National Provider Identifier';
