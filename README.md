# ERP Spain — Fase 1.7 Reportes

Esta versión añade el módulo de reportes al ERP ligero.

## Incluye

- Reporte de ventas y cobros por periodo.
- Filtro por fecha desde/hasta.
- KPIs: facturado, cobrado, pendiente estimado, facturas y cobros.
- Ventas por cliente.
- Productos/servicios vendidos.
- Facturas incluidas en el periodo.
- Exportación a Excel `.xlsx`.
- Exportación a PDF.
- Menú lateral y móvil actualizado con Reportes.

## Arranque

```powershell
cd C:\Users\guill\Desktop\ERP-SPAIN\erp-spain
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
C:\tools\apache-maven-3.9.16\bin\mvn.cmd spring-boot:run
```

## URL

```text
http://localhost:8080/reports
```

## Usuario inicial

```text
admin@erp.local
Admin123!
```
```powershell
C:\tools\apache-maven-3.9.16\bin\mvn.cmd clean
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
C:\tools\apache-maven-3.9.16\bin\mvn.cmd spring-boot:run
```
