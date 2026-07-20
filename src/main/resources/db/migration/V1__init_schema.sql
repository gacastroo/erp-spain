CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(150) NOT NULL,
    email VARCHAR(180) NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_users_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_users_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE companies (
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
    invoice_series VARCHAR(20) NOT NULL,
    bank_iban VARCHAR(34),
    enabled BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_companies_tax_id UNIQUE (tax_id)
);

CREATE INDEX idx_users_roles_user ON users_roles(user_id);
CREATE INDEX idx_users_roles_role ON users_roles(role_id);
