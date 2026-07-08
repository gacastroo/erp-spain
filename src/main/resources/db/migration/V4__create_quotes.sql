CREATE TABLE quotes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    quote_number VARCHAR(40) NOT NULL,
    client_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    issue_date DATE NOT NULL,
    valid_until DATE,
    notes VARCHAR(1000),
    subtotal DECIMAL(12, 2) NOT NULL,
    vat_total DECIMAL(12, 2) NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_quotes_number UNIQUE (quote_number),
    CONSTRAINT fk_quotes_client FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE TABLE quote_lines (
    id BIGINT NOT NULL AUTO_INCREMENT,
    quote_id BIGINT NOT NULL,
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
    CONSTRAINT fk_quote_lines_quote FOREIGN KEY (quote_id) REFERENCES quotes(id) ON DELETE CASCADE,
    CONSTRAINT fk_quote_lines_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_quotes_number ON quotes(quote_number);
CREATE INDEX idx_quotes_client ON quotes(client_id);
CREATE INDEX idx_quotes_status ON quotes(status);
CREATE INDEX idx_quotes_issue_date ON quotes(issue_date);
CREATE INDEX idx_quote_lines_quote ON quote_lines(quote_id);
CREATE INDEX idx_quote_lines_product ON quote_lines(product_id);
