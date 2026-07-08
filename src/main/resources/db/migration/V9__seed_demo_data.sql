-- Datos de ejemplo para probar dashboard, facturas, cobros, gastos, reportes, impuestos y PDFs.
-- Todos los números y NIF/CIF son ficticios y llevan referencias DEMO para identificarlos fácilmente.

SET @today = CURDATE();
SET @now = NOW(6);

INSERT IGNORE INTO companies (
    legal_name,
    commercial_name,
    tax_id,
    email,
    phone,
    address_line,
    city,
    postal_code,
    province,
    country,
    invoice_series,
    quote_series,
    bank_name,
    bank_iban,
    logo_text,
    invoice_legal_text,
    default_payment_terms_days,
    enabled,
    created_at,
    updated_at
) VALUES (
    'Demo Consulting Madrid S.L.',
    'Demo ERP',
    'B00000000',
    'administracion@demoerp.local',
    '+34 910 000 000',
    'Calle Mayor 1',
    'Madrid',
    '28013',
    'Madrid',
    'España',
    'FAC',
    'PRE',
    'Banco Demo',
    'ES9121000418450200051332',
    'DEMO ERP',
    'Documento generado con fines de prueba. Datos fiscales ficticios.',
    30,
    TRUE,
    @now,
    @now
);

UPDATE companies
SET
    legal_name = 'Demo Consulting Madrid S.L.',
    commercial_name = 'Demo ERP',
    email = 'administracion@demoerp.local',
    phone = '+34 910 000 000',
    address_line = 'Calle Mayor 1',
    city = 'Madrid',
    postal_code = '28013',
    province = 'Madrid',
    country = 'España',
    invoice_series = 'FAC',
    quote_series = 'PRE',
    bank_name = 'Banco Demo',
    bank_iban = 'ES9121000418450200051332',
    logo_text = 'DEMO ERP',
    invoice_legal_text = 'Documento generado con fines de prueba. Datos fiscales ficticios.',
    default_payment_terms_days = 30,
    enabled = TRUE,
    updated_at = @now
WHERE tax_id = 'B00000000';

INSERT IGNORE INTO clients (
    legal_name,
    commercial_name,
    tax_id,
    email,
    phone,
    address_line,
    city,
    postal_code,
    province,
    country,
    client_type,
    notes,
    enabled,
    created_at,
    updated_at
) VALUES
('Acme Retail España S.L.', 'Acme Retail', 'B86543210', 'compras@acmeretail.demo', '+34 911 111 111', 'Avenida de Europa 25', 'Madrid', '28224', 'Madrid', 'España', 'COMPANY', 'Cliente demo con varias facturas.', TRUE, @now, @now),
('Restaurante La Plaza S.L.', 'La Plaza', 'B74258136', 'gerencia@laplaza.demo', '+34 932 222 222', 'Plaza Mayor 8', 'Barcelona', '08002', 'Barcelona', 'España', 'COMPANY', 'Cliente demo de hostelería.', TRUE, @now, @now),
('Clínica Norte S.L.P.', 'Clínica Norte', 'B19384756', 'admin@clinicanorte.demo', '+34 948 333 333', 'Calle Sancho el Fuerte 12', 'Pamplona', '31007', 'Navarra', 'España', 'COMPANY', 'Cliente demo sanitario.', TRUE, @now, @now),
('María López García', 'López Diseño', '12345678Z', 'maria@lopezdiseno.demo', '+34 600 444 444', 'Calle Colón 20', 'Valencia', '46004', 'Valencia', 'España', 'SELF_EMPLOYED', 'Autónoma demo.', TRUE, @now, @now),
('Ayuntamiento Demo', 'Ayuntamiento Demo', 'P2800000A', 'contratacion@aytodemo.local', '+34 915 555 555', 'Plaza Consistorial 1', 'Madrid', '28005', 'Madrid', 'España', 'PUBLIC_ENTITY', 'Administración pública demo.', TRUE, @now, @now),
('Talleres Vega S.L.', 'Talleres Vega', 'B50987654', 'info@talleresvega.demo', '+34 976 666 666', 'Polígono Industrial Norte 14', 'Zaragoza', '50014', 'Zaragoza', 'España', 'COMPANY', 'Cliente demo industrial.', TRUE, @now, @now);

INSERT IGNORE INTO products (
    name,
    description,
    sku,
    product_type,
    unit_price,
    vat_rate,
    enabled,
    created_at,
    updated_at
) VALUES
('Consultoría ERP', 'Hora de consultoría funcional y técnica para implantación ERP.', 'DEMO-SERV-ERP-CONSULTORIA', 'SERVICE', 75.00, 21.00, TRUE, @now, @now),
('Implantación básica ERP', 'Configuración inicial, módulos base, usuarios y primera formación.', 'DEMO-SERV-ERP-IMPLANTACION', 'SERVICE', 1200.00, 21.00, TRUE, @now, @now),
('Mantenimiento mensual', 'Soporte evolutivo y correctivo mensual.', 'DEMO-SERV-MANTENIMIENTO', 'SERVICE', 350.00, 21.00, TRUE, @now, @now),
('Formación usuarios', 'Sesión de formación para usuarios finales.', 'DEMO-SERV-FORMACION', 'SERVICE', 280.00, 21.00, TRUE, @now, @now),
('Licencia software anual', 'Licencia anual de software empresarial.', 'DEMO-PROD-LICENCIA-ANUAL', 'PRODUCT', 600.00, 21.00, TRUE, @now, @now),
('Diseño dashboard', 'Diseño y puesta en marcha de panel de control personalizado.', 'DEMO-SERV-DASHBOARD', 'SERVICE', 850.00, 21.00, TRUE, @now, @now),
('Hosting gestionado', 'Alojamiento gestionado mensual para aplicación empresarial.', 'DEMO-SERV-HOSTING', 'SERVICE', 45.00, 21.00, TRUE, @now, @now),
('Soporte remoto', 'Hora de soporte remoto para incidencias y ajustes menores.', 'DEMO-SERV-SOPORTE', 'SERVICE', 60.00, 21.00, TRUE, @now, @now);

SELECT @client_acme := id FROM clients WHERE tax_id = 'B86543210';
SELECT @client_restaurante := id FROM clients WHERE tax_id = 'B74258136';
SELECT @client_clinica := id FROM clients WHERE tax_id = 'B19384756';
SELECT @client_maria := id FROM clients WHERE tax_id = '12345678Z';
SELECT @client_ayto := id FROM clients WHERE tax_id = 'P2800000A';
SELECT @client_talleres := id FROM clients WHERE tax_id = 'B50987654';

SELECT @prod_consultoria := id FROM products WHERE sku = 'DEMO-SERV-ERP-CONSULTORIA';
SELECT @prod_implantacion := id FROM products WHERE sku = 'DEMO-SERV-ERP-IMPLANTACION';
SELECT @prod_mantenimiento := id FROM products WHERE sku = 'DEMO-SERV-MANTENIMIENTO';
SELECT @prod_formacion := id FROM products WHERE sku = 'DEMO-SERV-FORMACION';
SELECT @prod_licencia := id FROM products WHERE sku = 'DEMO-PROD-LICENCIA-ANUAL';
SELECT @prod_dashboard := id FROM products WHERE sku = 'DEMO-SERV-DASHBOARD';
SELECT @prod_hosting := id FROM products WHERE sku = 'DEMO-SERV-HOSTING';
SELECT @prod_soporte := id FROM products WHERE sku = 'DEMO-SERV-SOPORTE';

INSERT IGNORE INTO quotes (
    quote_number,
    client_id,
    status,
    issue_date,
    valid_until,
    notes,
    subtotal,
    vat_total,
    total,
    created_at,
    updated_at
) VALUES
('DEMO-PRE-001', @client_acme, 'ACCEPTED', DATE_SUB(@today, INTERVAL 12 DAY), DATE_ADD(@today, INTERVAL 18 DAY), 'Presupuesto demo aceptado y convertido en factura.', 1200.00, 252.00, 1452.00, @now, @now),
('DEMO-PRE-002', @client_restaurante, 'SENT', DATE_SUB(@today, INTERVAL 4 DAY), DATE_ADD(@today, INTERVAL 26 DAY), 'Presupuesto demo enviado y pendiente de respuesta.', 850.00, 178.50, 1028.50, @now, @now),
('DEMO-PRE-003', @client_clinica, 'DRAFT', @today, DATE_ADD(@today, INTERVAL 30 DAY), 'Presupuesto demo en borrador.', 560.00, 117.60, 677.60, @now, @now),
('DEMO-PRE-004', @client_maria, 'REJECTED', DATE_SUB(@today, INTERVAL 20 DAY), DATE_ADD(@today, INTERVAL 10 DAY), 'Presupuesto demo rechazado.', 350.00, 73.50, 423.50, @now, @now),
('DEMO-PRE-005', @client_ayto, 'ACCEPTED', DATE_SUB(@today, INTERVAL 8 DAY), DATE_ADD(@today, INTERVAL 22 DAY), 'Presupuesto demo aceptado para administración pública.', 600.00, 126.00, 726.00, @now, @now);

SELECT @quote_001 := id FROM quotes WHERE quote_number = 'DEMO-PRE-001';
SELECT @quote_002 := id FROM quotes WHERE quote_number = 'DEMO-PRE-002';
SELECT @quote_003 := id FROM quotes WHERE quote_number = 'DEMO-PRE-003';
SELECT @quote_004 := id FROM quotes WHERE quote_number = 'DEMO-PRE-004';
SELECT @quote_005 := id FROM quotes WHERE quote_number = 'DEMO-PRE-005';

INSERT INTO quote_lines (
    quote_id,
    product_id,
    description,
    quantity,
    unit_price,
    vat_rate,
    line_subtotal,
    line_vat,
    line_total,
    sort_order,
    created_at,
    updated_at
) VALUES
(@quote_001, @prod_implantacion, 'Implantación básica ERP', 1.00, 1200.00, 21.00, 1200.00, 252.00, 1452.00, 1, @now, @now),
(@quote_002, @prod_dashboard, 'Diseño dashboard', 1.00, 850.00, 21.00, 850.00, 178.50, 1028.50, 1, @now, @now),
(@quote_003, @prod_formacion, 'Formación usuarios', 2.00, 280.00, 21.00, 560.00, 117.60, 677.60, 1, @now, @now),
(@quote_004, @prod_mantenimiento, 'Mantenimiento mensual', 1.00, 350.00, 21.00, 350.00, 73.50, 423.50, 1, @now, @now),
(@quote_005, @prod_licencia, 'Licencia software anual', 1.00, 600.00, 21.00, 600.00, 126.00, 726.00, 1, @now, @now);

INSERT IGNORE INTO invoices (
    invoice_number,
    quote_id,
    client_id,
    status,
    issue_date,
    due_date,
    paid_at,
    notes,
    subtotal,
    vat_total,
    total,
    created_at,
    updated_at
) VALUES
('DEMO-FAC-001', @quote_001, @client_acme, 'PAID', DATE_SUB(@today, INTERVAL 6 DAY), DATE_ADD(DATE_SUB(@today, INTERVAL 6 DAY), INTERVAL 30 DAY), DATE_SUB(@today, INTERVAL 2 DAY), 'Factura demo cobrada completa.', 1200.00, 252.00, 1452.00, @now, @now),
('DEMO-FAC-002', @quote_005, @client_ayto, 'SENT', DATE_SUB(@today, INTERVAL 3 DAY), DATE_ADD(DATE_SUB(@today, INTERVAL 3 DAY), INTERVAL 30 DAY), NULL, 'Factura demo enviada con cobro parcial.', 600.00, 126.00, 726.00, @now, @now),
('DEMO-FAC-003', NULL, @client_restaurante, 'OVERDUE', DATE_SUB(@today, INTERVAL 45 DAY), DATE_SUB(@today, INTERVAL 15 DAY), NULL, 'Factura demo vencida.', 350.00, 73.50, 423.50, @now, @now),
('DEMO-FAC-004', NULL, @client_clinica, 'ISSUED', DATE_SUB(@today, INTERVAL 1 DAY), DATE_ADD(DATE_SUB(@today, INTERVAL 1 DAY), INTERVAL 30 DAY), NULL, 'Factura demo emitida y pendiente de envío.', 720.00, 151.20, 871.20, @now, @now),
('DEMO-FAC-005', NULL, @client_maria, 'PAID', DATE_SUB(@today, INTERVAL 18 DAY), DATE_ADD(DATE_SUB(@today, INTERVAL 18 DAY), INTERVAL 30 DAY), DATE_SUB(@today, INTERVAL 14 DAY), 'Factura demo cobrada por Bizum.', 180.00, 37.80, 217.80, @now, @now),
('DEMO-FAC-006', NULL, @client_talleres, 'SENT', DATE_SUB(@today, INTERVAL 70 DAY), DATE_SUB(@today, INTERVAL 40 DAY), NULL, 'Factura demo enviada de otro periodo.', 600.00, 126.00, 726.00, @now, @now),
('DEMO-FAC-007', NULL, @client_restaurante, 'CANCELLED', DATE_SUB(@today, INTERVAL 5 DAY), DATE_ADD(DATE_SUB(@today, INTERVAL 5 DAY), INTERVAL 30 DAY), NULL, 'Factura demo cancelada y excluida de reportes.', 100.00, 21.00, 121.00, @now, @now),
('DEMO-FAC-008', NULL, @client_acme, 'DRAFT', @today, DATE_ADD(@today, INTERVAL 30 DAY), NULL, 'Factura demo en borrador y excluida de reportes.', 45.00, 9.45, 54.45, @now, @now);

SELECT @invoice_001 := id FROM invoices WHERE invoice_number = 'DEMO-FAC-001';
SELECT @invoice_002 := id FROM invoices WHERE invoice_number = 'DEMO-FAC-002';
SELECT @invoice_003 := id FROM invoices WHERE invoice_number = 'DEMO-FAC-003';
SELECT @invoice_004 := id FROM invoices WHERE invoice_number = 'DEMO-FAC-004';
SELECT @invoice_005 := id FROM invoices WHERE invoice_number = 'DEMO-FAC-005';
SELECT @invoice_006 := id FROM invoices WHERE invoice_number = 'DEMO-FAC-006';
SELECT @invoice_007 := id FROM invoices WHERE invoice_number = 'DEMO-FAC-007';
SELECT @invoice_008 := id FROM invoices WHERE invoice_number = 'DEMO-FAC-008';

INSERT INTO invoice_lines (
    invoice_id,
    product_id,
    description,
    quantity,
    unit_price,
    vat_rate,
    line_subtotal,
    line_vat,
    line_total,
    sort_order,
    created_at,
    updated_at
) VALUES
(@invoice_001, @prod_implantacion, 'Implantación básica ERP', 1.00, 1200.00, 21.00, 1200.00, 252.00, 1452.00, 1, @now, @now),
(@invoice_002, @prod_licencia, 'Licencia software anual', 1.00, 600.00, 21.00, 600.00, 126.00, 726.00, 1, @now, @now),
(@invoice_003, @prod_mantenimiento, 'Mantenimiento mensual', 1.00, 350.00, 21.00, 350.00, 73.50, 423.50, 1, @now, @now),
(@invoice_004, @prod_consultoria, 'Consultoría ERP', 8.00, 75.00, 21.00, 600.00, 126.00, 726.00, 1, @now, @now),
(@invoice_004, @prod_soporte, 'Soporte remoto', 2.00, 60.00, 21.00, 120.00, 25.20, 145.20, 2, @now, @now),
(@invoice_005, @prod_soporte, 'Soporte remoto', 3.00, 60.00, 21.00, 180.00, 37.80, 217.80, 1, @now, @now),
(@invoice_006, @prod_licencia, 'Licencia software anual', 1.00, 600.00, 21.00, 600.00, 126.00, 726.00, 1, @now, @now),
(@invoice_007, @prod_hosting, 'Hosting gestionado', 2.00, 50.00, 21.00, 100.00, 21.00, 121.00, 1, @now, @now),
(@invoice_008, @prod_hosting, 'Hosting gestionado', 1.00, 45.00, 21.00, 45.00, 9.45, 54.45, 1, @now, @now);

INSERT INTO payments (
    invoice_id,
    payment_date,
    amount,
    method,
    reference,
    notes,
    created_at,
    updated_at
) VALUES
(@invoice_001, DATE_SUB(@today, INTERVAL 2 DAY), 1452.00, 'TRANSFER', 'DEMO-COBRO-001', 'Cobro completo de factura demo.', @now, @now),
(@invoice_002, DATE_SUB(@today, INTERVAL 1 DAY), 250.00, 'TRANSFER', 'DEMO-COBRO-002', 'Cobro parcial de factura demo.', @now, @now),
(@invoice_005, DATE_SUB(@today, INTERVAL 14 DAY), 217.80, 'BIZUM', 'DEMO-COBRO-003', 'Cobro por Bizum.', @now, @now),
(@invoice_006, DATE_SUB(@today, INTERVAL 38 DAY), 300.00, 'CARD', 'DEMO-COBRO-004', 'Cobro parcial de periodo anterior.', @now, @now);

INSERT INTO expenses (
    expense_date,
    supplier_name,
    supplier_tax_id,
    invoice_number,
    category,
    description,
    base_amount,
    vat_rate,
    vat_amount,
    total,
    paid,
    payment_method,
    notes,
    created_at,
    updated_at
) VALUES
(DATE_SUB(@today, INTERVAL 2 DAY), 'Proveedor Hosting Demo S.L.', 'B11111111', 'DEMO-G-001', 'SOFTWARE', 'Servidor VPS y copias de seguridad.', 120.00, 21.00, 25.20, 145.20, TRUE, 'TRANSFER', 'Gasto demo pagado.', @now, @now),
(DATE_SUB(@today, INTERVAL 5 DAY), 'Asesoría Fiscal Demo S.L.', 'B22222222', 'DEMO-G-002', 'PROFESSIONAL_SERVICES', 'Asesoría fiscal mensual.', 180.00, 21.00, 37.80, 217.80, TRUE, 'TRANSFER', 'Gasto demo de asesoría.', @now, @now),
(DATE_SUB(@today, INTERVAL 7 DAY), 'Coworking Centro Demo S.L.', 'B33333333', 'DEMO-G-003', 'RENT', 'Puesto flexible de trabajo.', 250.00, 21.00, 52.50, 302.50, FALSE, 'TRANSFER', 'Gasto demo pendiente.', @now, @now),
(DATE_SUB(@today, INTERVAL 9 DAY), 'Telefonía Demo S.A.', 'A44444444', 'DEMO-G-004', 'UTILITIES', 'Telefonía e internet.', 65.00, 21.00, 13.65, 78.65, TRUE, 'CARD', 'Gasto demo de suministros.', @now, @now),
(DATE_SUB(@today, INTERVAL 16 DAY), 'Campañas Demo Ads S.L.', 'B55555555', 'DEMO-G-005', 'MARKETING', 'Campaña publicitaria mensual.', 300.00, 21.00, 63.00, 363.00, FALSE, 'CARD', 'Gasto demo pendiente.', @now, @now),
(DATE_SUB(@today, INTERVAL 35 DAY), 'Material Oficina Demo S.L.', 'B66666666', 'DEMO-G-006', 'PURCHASES', 'Material de oficina.', 90.00, 21.00, 18.90, 108.90, TRUE, 'CASH', 'Gasto demo del periodo anterior.', @now, @now),
(DATE_SUB(@today, INTERVAL 42 DAY), 'Renfe Demo Viajes', 'A77777777', 'DEMO-G-007', 'TRANSPORT', 'Desplazamiento a cliente.', 70.00, 10.00, 7.00, 77.00, TRUE, 'CARD', 'Gasto demo con IVA reducido.', @now, @now),
(@today, 'Software Productividad Demo', 'B88888888', 'DEMO-G-008', 'SOFTWARE', 'Suscripción mensual de productividad.', 39.00, 21.00, 8.19, 47.19, TRUE, 'CARD', 'Gasto demo reciente.', @now, @now);
