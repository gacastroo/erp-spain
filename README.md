# ERP Spain — Fase 1.9 Fiscalidad e IVA trimestral

Esta versión añade una pantalla de control fiscal para revisar el IVA trimestral de forma sencilla a partir de los datos reales del ERP.

## Incluye

- Nuevo módulo de impuestos en `/taxes`.
- Resumen trimestral de IVA por año y trimestre.
- Cálculo de IVA repercutido desde facturas emitidas no canceladas.
- Cálculo de IVA soportado desde gastos registrados.
- Resultado estimado: IVA a ingresar o IVA a compensar.
- Desglose de IVA por tipo: 0%, 4%, 10%, 21% u otros tipos usados en facturas/gastos.
- Detalle de facturas y gastos incluidos en el trimestre.
- Exportación del resumen de IVA a Excel y PDF.
- Enlace a Impuestos desde el menú principal y desde el dashboard.
- Dashboard actualizado con acceso rápido a reportes e impuestos.

## Arranque

```powershell
cd C:\Users\guill\Desktop\ERP-SPAIN\erp-spain

Remove-Item -Recurse -Force .\target -ErrorAction SilentlyContinue

$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"

C:\tools\apache-maven-3.9.16\bin\mvn.cmd clean spring-boot:run
```

## URLs

```text
http://localhost:8080/dashboard
http://localhost:8080/taxes
http://localhost:8080/reports
http://localhost:8080/expenses
```

## Usuario inicial

```text
admin@erp.local
Admin123!
```

## Nota fiscal

El cálculo de IVA es orientativo y sirve para control interno. Antes de presentar impuestos reales conviene revisar facturas, gastos deducibles, fechas fiscales y criterios aplicables con una asesoría.
