-- V1: Create invoices table
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal > 0),
    tax_amount DECIMAL(10, 2) NOT NULL CHECK (tax_amount >= 0),
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount > 0),
    amount_paid DECIMAL(10, 2) NOT NULL DEFAULT 0 CHECK (amount_paid >= 0),
    amount_due DECIMAL(10, 2) NOT NULL CHECK (amount_due >= 0),
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(1000),
    insurance_claim_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create indexes for invoices table
CREATE INDEX idx_invoice_patient ON invoices(patient_id);
CREATE INDEX idx_invoice_appointment ON invoices(appointment_id);
CREATE INDEX idx_invoice_doctor ON invoices(doctor_id);
CREATE INDEX idx_invoice_status ON invoices(status);
CREATE INDEX idx_invoice_due_date ON invoices(due_date);
CREATE INDEX idx_invoice_issue_date ON invoices(issue_date);
CREATE INDEX idx_invoice_insurance_claim ON invoices(insurance_claim_id);

-- Add comments
COMMENT ON TABLE invoices IS 'Stores invoices for healthcare services';
COMMENT ON COLUMN invoices.invoice_number IS 'Unique invoice identifier';
COMMENT ON COLUMN invoices.status IS 'DRAFT, ISSUED, OVERDUE, PARTIALLY_PAID, PAID, CANCELLED, REFUNDED, DISPUTED';
