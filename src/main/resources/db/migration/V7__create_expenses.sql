CREATE TABLE expenses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    expense_date DATE NOT NULL,
    supplier_name VARCHAR(180) NOT NULL,
    supplier_tax_id VARCHAR(20),
    invoice_number VARCHAR(80),
    category VARCHAR(40) NOT NULL,
    description VARCHAR(500),
    base_amount DECIMAL(12, 2) NOT NULL,
    vat_rate DECIMAL(5, 2) NOT NULL,
    vat_amount DECIMAL(12, 2) NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    paid BOOLEAN NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    notes VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE INDEX idx_expenses_date ON expenses(expense_date);
CREATE INDEX idx_expenses_supplier ON expenses(supplier_name);
CREATE INDEX idx_expenses_category ON expenses(category);
CREATE INDEX idx_expenses_paid ON expenses(paid);
