# ERP Spain - Fase 1.6 Cobros

Esta fase añade el módulo de cobros sobre la Fase 1.5.

## Incluye

- Módulo `/payments`.
- Registro de cobros parciales o totales.
- Métodos de cobro: transferencia, tarjeta, efectivo, Bizum, domiciliación y otros.
- Cobros vinculados a facturas.
- Cálculo automático de cobrado y pendiente.
- Actualización automática del estado de la factura:
  - Si el total cobrado cubre la factura, pasa a **Cobrada**.
  - Si se elimina un cobro, la factura vuelve a **Enviada** o **Vencida** según fecha.
- La factura ya no se marca manualmente como cobrada desde estados; se hace registrando cobros.
- Sección de cobros dentro del detalle de factura.
- Listado de cobros responsive.
- Migración `V6__create_payments.sql`.
- Dashboard ajustado a **Cobrado este mes**.

## Arranque

```powershell
cd C:\Users\guill\Desktop\ERP-SPAIN\erp-spain
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
C:\tools\apache-maven-3.9.16\bin\mvn.cmd spring-boot:run
```

## Rutas

- Facturas: http://localhost:8080/invoices
- Cobros: http://localhost:8080/payments

## Usuario inicial

- Email: `admin@erp.local`
- Contraseña: `Admin123!`
