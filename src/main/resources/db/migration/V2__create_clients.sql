CREATE TABLE clients (
    id BIGINT NOT NULL AUTO_INCREMENT,
    legal_name VARCHAR(180) NOT NULL,
    commercial_name VARCHAR(180),
    tax_id VARCHAR(20) NOT NULL,
    email VARCHAR(180),
    phone VARCHAR(30),
    address_line VARCHAR(255),
    city VARCHAR(120),
    postal_code VARCHAR(20),
    province VARCHAR(120),
    country VARCHAR(80),
    client_type VARCHAR(40) NOT NULL,
    notes TEXT,
    enabled BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_clients_tax_id UNIQUE (tax_id)
);

CREATE INDEX idx_clients_legal_name ON clients(legal_name);
CREATE INDEX idx_clients_email ON clients(email);
CREATE INDEX idx_clients_enabled ON clients(enabled);
