-- Índices de rendimiento para dashboard, reportes, IVA trimestral y listados principales.
-- Se añaden como migración independiente para no tocar migraciones ya aplicadas.

CREATE INDEX idx_quotes_issue_status ON quotes(issue_date, status);
CREATE INDEX idx_quotes_client_issue ON quotes(client_id, issue_date);

CREATE INDEX idx_invoices_issue_status ON invoices(issue_date, status);
CREATE INDEX idx_invoices_status_due ON invoices(status, due_date);
CREATE INDEX idx_invoices_client_issue ON invoices(client_id, issue_date);
CREATE INDEX idx_invoice_lines_vat_rate ON invoice_lines(vat_rate);

CREATE INDEX idx_payments_date_invoice ON payments(payment_date, invoice_id);

CREATE INDEX idx_expenses_date_paid ON expenses(expense_date, paid);
CREATE INDEX idx_expenses_date_category ON expenses(expense_date, category);
CREATE INDEX idx_expenses_date_vat_rate ON expenses(expense_date, vat_rate);
