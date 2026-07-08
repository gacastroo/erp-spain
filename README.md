# ERP Spain — Fase 1.10 Configuración de empresa, series y PDF

Esta versión añade la configuración básica de empresa y mejora la generación de documentos para que presupuestos y facturas empiecen a comportarse como documentos reales de negocio.

## Incluye

- Nueva pantalla de configuración en `/settings/company`, accesible desde el menú principal.
- Datos fiscales de empresa: nombre fiscal, NIF/CIF, dirección, email y teléfono.
- Configuración de series de numeración para presupuestos y facturas.
- Vencimiento automático de facturas según los días configurados.
- Datos bancarios para mostrar en PDF de factura.
- Texto legal o pie de documento configurable.
- Texto de logo sencillo para documentos PDF.
- Botón de descarga PDF en detalle de presupuesto.
- Botón de descarga PDF en detalle de factura.
- PDFs profesionales de presupuestos y facturas con datos de empresa, cliente, líneas, base imponible, IVA, total, notas y pie legal.
- Exportaciones PDF y Excel de reportes con datos de empresa en el encabezado.
- Exportaciones PDF y Excel de impuestos/IVA con datos de empresa en el encabezado.
- Migración `V8__company_document_settings.sql`.

## Arranque

```powershell
cd C:\Users\guill\Desktop\ERP-SPAIN\erp-spain

Remove-Item -Recurse -Force .\target -ErrorAction SilentlyContinue

$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"

C:\tools\apache-maven-3.9.16\bin\mvn.cmd clean spring-boot:run
```

## URLs principales

```text
http://localhost:8080/dashboard
http://localhost:8080/settings/company
http://localhost:8080/quotes
http://localhost:8080/invoices
```

## Usuario inicial

```text
admin@erp.local
Admin123!
```

## Nota fiscal

La generación de PDF y el cálculo de impuestos son funcionales para gestión interna. Antes de usar documentos reales con validez fiscal conviene revisar textos legales, numeración, criterios de emisión, facturas rectificativas y obligaciones VeriFactu/factura electrónica con una asesoría.
