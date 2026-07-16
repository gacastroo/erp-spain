-- Script opcional para eliminar los datos de ejemplo.
-- Ejecutar manualmente en MySQL solo si quieres limpiar la base de datos demo.

DELETE FROM payments WHERE reference LIKE 'DEMO-COBRO-%';
DELETE FROM invoice_lines WHERE invoice_id IN (SELECT id FROM invoices WHERE invoice_number LIKE 'DEMO-FAC-%');
DELETE FROM invoices WHERE invoice_number LIKE 'DEMO-FAC-%';
DELETE FROM quote_lines WHERE quote_id IN (SELECT id FROM quotes WHERE quote_number LIKE 'DEMO-PRE-%');
DELETE FROM quotes WHERE quote_number LIKE 'DEMO-PRE-%';
DELETE FROM expenses WHERE invoice_number LIKE 'DEMO-G-%';
DELETE FROM products WHERE sku LIKE 'DEMO-%';
DELETE FROM clients WHERE tax_id IN ('B86543210', 'B74258136', 'B19384756', '12345678Z', 'P2800000A', 'B50987654');
