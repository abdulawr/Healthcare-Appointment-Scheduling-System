-- V2: Create invoice_items table
CREATE TABLE invoice_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    description VARCHAR(500) NOT NULL,
    service_code VARCHAR(50),
    quantity INTEGER NOT NULL CHECK (quantity >= 1),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price > 0),
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price > 0),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_invoice_items_invoice 
        FOREIGN KEY (invoice_id) 
        REFERENCES invoices(id) 
        ON DELETE CASCADE
);

-- Create indexes for invoice_items table
CREATE INDEX idx_invoice_item_invoice ON invoice_items(invoice_id);
CREATE INDEX idx_invoice_item_service_code ON invoice_items(service_code);

-- Add comments
COMMENT ON TABLE invoice_items IS 'Stores line items for each invoice';
COMMENT ON COLUMN invoice_items.service_code IS 'Medical service/procedure code (CPT, ICD-10, etc.)';
COMMENT ON COLUMN invoice_items.total_price IS 'Calculated as quantity * unit_price';
