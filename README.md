# ERP Spain - Fase 1.5 UX y trazabilidad presupuesto/factura

Esta versión corrige la relación entre presupuestos y facturas:

- Un presupuesto vinculado a una factura ya no se puede eliminar ni editar.
- El sistema evita errores de integridad si se intenta borrar un presupuesto facturado.
- Al crear una factura desde un presupuesto, el presupuesto pasa automáticamente a `Aceptado`.
- El estado del presupuesto facturado queda protegido y se gestiona desde la factura.
- En presupuestos y facturas se sustituyen los selectores de estado por botones de estado más claros.
- En el detalle de factura se muestra de forma visible el presupuesto vinculado.
- En el detalle de presupuesto facturado se muestra la factura vinculada y el bloqueo de trazabilidad.

## Arranque

```powershell
cd C:\Users\guill\Desktop\ERP-SPAIN\erp-spain
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
C:\tools\apache-maven-3.9.16\bin\mvn.cmd spring-boot:run
```

Si usas Docker:

```powershell
docker compose up -d
```

## Usuario inicial

```text
admin@erp.local
Admin123!
```

## Commit recomendado

```powershell
git add .
git commit -m "fix: proteger presupuestos facturados y simplificar estados"
```
