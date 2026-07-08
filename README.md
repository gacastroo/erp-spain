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

## Commit recomendado

```powershell
git add .
git commit -m "feat: añadir reportes y exportación"
```


## Corrección incluida

Este ZIP corrige el fallo de arranque del módulo de reportes:

```text
NoClassDefFoundError: PdfPTable
ClassNotFoundException: com.lowagie.text.Element
```

La causa era que el código de exportación PDF usa imports `com.lowagie.*`, pero el `pom.xml` tenía OpenPDF 2.x.
Se ha fijado OpenPDF a `1.3.43`, que mantiene esos paquetes.

Para aplicar la corrección en local, limpia y arranca de nuevo:

```powershell
C:\tools\apache-maven-3.9.16\bin\mvn.cmd clean
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
C:\tools\apache-maven-3.9.16\bin\mvn.cmd spring-boot:run
```
