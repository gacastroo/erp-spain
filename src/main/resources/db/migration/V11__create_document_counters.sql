CREATE TABLE document_counters (
    id BIGINT NOT NULL AUTO_INCREMENT,
    document_type VARCHAR(20) NOT NULL,
    series VARCHAR(30) NOT NULL,
    document_year INT NOT NULL,
    current_value BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_document_counters_type_series_year
        UNIQUE (document_type, series, document_year),
    CONSTRAINT chk_document_counters_value CHECK (current_value >= 0)
);

-- Preserve the highest sequence already used by installations upgraded from
-- the previous document-number implementation. Malformed or demo numbers that
-- do not follow SERIES-YYYY-SEQUENCE are intentionally ignored.
INSERT INTO document_counters (
    document_type, series, document_year, current_value, created_at, updated_at
)
SELECT
    'INVOICE',
    LEFT(invoice_number, LENGTH(invoice_number) - LENGTH(SUBSTRING_INDEX(invoice_number, '-', -2)) - 1),
    CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(invoice_number, '-', -2), '-', 1) AS UNSIGNED),
    MAX(CAST(SUBSTRING_INDEX(invoice_number, '-', -1) AS UNSIGNED)),
    NOW(6),
    NOW(6)
FROM invoices
WHERE invoice_number REGEXP '^.+-[0-9]{4}-[0-9]+$'
GROUP BY
    LEFT(invoice_number, LENGTH(invoice_number) - LENGTH(SUBSTRING_INDEX(invoice_number, '-', -2)) - 1),
    CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(invoice_number, '-', -2), '-', 1) AS UNSIGNED);

INSERT INTO document_counters (
    document_type, series, document_year, current_value, created_at, updated_at
)
SELECT
    'QUOTE',
    LEFT(quote_number, LENGTH(quote_number) - LENGTH(SUBSTRING_INDEX(quote_number, '-', -2)) - 1),
    CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(quote_number, '-', -2), '-', 1) AS UNSIGNED),
    MAX(CAST(SUBSTRING_INDEX(quote_number, '-', -1) AS UNSIGNED)),
    NOW(6),
    NOW(6)
FROM quotes
WHERE quote_number REGEXP '^.+-[0-9]{4}-[0-9]+$'
GROUP BY
    LEFT(quote_number, LENGTH(quote_number) - LENGTH(SUBSTRING_INDEX(quote_number, '-', -2)) - 1),
    CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(quote_number, '-', -2), '-', 1) AS UNSIGNED);
