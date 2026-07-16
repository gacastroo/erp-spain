ALTER TABLE companies
    ADD COLUMN quote_series VARCHAR(20) NOT NULL DEFAULT 'PRE' AFTER invoice_series,
    ADD COLUMN bank_name VARCHAR(120) NULL AFTER quote_series,
    ADD COLUMN logo_text VARCHAR(80) NULL AFTER bank_iban,
    ADD COLUMN invoice_legal_text VARCHAR(1200) NULL AFTER logo_text,
    ADD COLUMN default_payment_terms_days INT NOT NULL DEFAULT 30 AFTER invoice_legal_text;

UPDATE companies
SET invoice_series = 'FAC'
WHERE invoice_series = 'A';

UPDATE companies
SET logo_text = 'ERP'
WHERE logo_text IS NULL;
