CREATE TABLE invoices (
    id BIGINT NOT NULL AUTO_INCREMENT,
    invoice_number VARCHAR(40) NOT NULL,
    quote_id BIGINT,
    client_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE,
    paid_at DATE,
    notes VARCHAR(1000),
    subtotal DECIMAL(12, 2) NOT NULL,
    vat_total DECIMAL(12, 2) NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_invoices_number UNIQUE (invoice_number),
    CONSTRAINT uk_invoices_quote UNIQUE (quote_id),
    CONSTRAINT fk_invoices_quote FOREIGN KEY (quote_id) REFERENCES quotes(id),
    CONSTRAINT fk_invoices_client FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE TABLE invoice_lines (
    id BIGINT NOT NULL AUTO_INCREMENT,
    invoice_id BIGINT NOT NULL,
    product_id BIGINT,
    description VARCHAR(500) NOT NULL,
    quantity DECIMAL(12, 2) NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    vat_rate DECIMAL(5, 2) NOT NULL,
    line_subtotal DECIMAL(12, 2) NOT NULL,
    line_vat DECIMAL(12, 2) NOT NULL,
    line_total DECIMAL(12, 2) NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_invoice_lines_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_lines_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_invoices_number ON invoices(invoice_number);
CREATE INDEX idx_invoices_client ON invoices(client_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_issue_date ON invoices(issue_date);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
CREATE INDEX idx_invoices_quote ON invoices(quote_id);
CREATE INDEX idx_invoice_lines_invoice ON invoice_lines(invoice_id);
CREATE INDEX idx_invoice_lines_product ON invoice_lines(product_id);
