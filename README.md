# ERP Spain — Fase 1.8 Gastos y dashboard operativo

Esta versión añade el módulo de gastos y convierte el dashboard en una pantalla útil de gestión diaria.

## Incluye

- Módulo de gastos en `/expenses`.
- Alta, edición, búsqueda y eliminación de gastos.
- Categorías de gasto: compras, alquiler, suministros, servicios profesionales, personal, impuestos, transporte, marketing, software y otros.
- Cálculo automático de IVA soportado y total del gasto.
- Estado del gasto: pagado o pendiente.
- Método de pago reutilizando los métodos del ERP.
- Migración `V7__create_expenses.sql`.
- Dashboard funcional:
  - Botones reales para nueva factura, nuevo gasto, registrar cobro y reportes.
  - Facturado este mes.
  - Cobrado este mes.
  - Gastos este mes.
  - Resultado de caja.
  - Facturas pendientes y vencidas.
  - Gastos pendientes de pago.
  - Últimas facturas, últimos cobros y últimos gastos.
- Reportes actualizados con gastos y resultado de caja.
- Exportación Excel/PDF incluyendo gastos.

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
http://localhost:8080/expenses
http://localhost:8080/reports
```

## Usuario inicial

```text
admin@erp.local
Admin123!
```
