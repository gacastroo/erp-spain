# Correcciones de seguridad, integridad y negocio

Fecha: 20 de julio de 2026

Este paquete incorpora las correcciones derivadas de la revisión estática y de la batería de pruebas del ERP.

## Cambios principales

1. El administrador inicial queda desactivado por defecto y solo puede crearse mediante variables de entorno y una contraseña robusta.
2. Los datos demo se han retirado de la ubicación normal de Flyway y solo se cargan con el perfil `demo`.
3. La función «recordarme» queda desactivada por defecto y exige un secreto de al menos 32 caracteres cuando se activa.
4. Los scripts inline de tema se han sustituido por `/js/theme-init.js`, compatible con la CSP `script-src 'self'`.
5. Las respuestas dinámicas reciben las cabeceras `no-store` antes de que la respuesta pueda quedar comprometida.
6. El limitador de login solo registra fallos reales y limpia el contador tras una autenticación correcta.
7. La numeración usa un contador MySQL atómico por tipo, serie y año mediante `LAST_INSERT_ID(expr)`.
8. Los cobros bloquean pesimistamente la fila de factura antes de calcular el pendiente.
9. Solo se pueden facturar presupuestos aceptados y el presupuesto se bloquea durante la operación.
10. Una factura totalmente cobrada no puede volver manualmente a un estado pendiente ni marcarse como pagada sin registrar cobros.
11. Los cobros parciales conservan el estado comercial `ISSUED`, `SENT` u `OVERDUE`.
12. Las facturas emitidas o enviadas pasan automáticamente a vencidas al arrancar y cada día.
13. El pendiente se calcula sobre las facturas emitidas en el periodo y sus propios cobros acumulados.
14. El resultado de caja descuenta únicamente gastos marcados como pagados.
15. Se rechazan fechas de cobro futuras y `paidAt` utiliza siempre la fecha más reciente de los cobros persistidos.
16. Los gastos tienen límites de precisión, escala e IVA tanto en Bean Validation como en el servicio.
17. Una fila de presupuesto completamente vacía se ignora, pero una fila parcialmente rellenada sigue validándose.
18. La creación y edición de presupuestos rechaza clientes y productos desactivados.
19. Las ventas por producto se agrupan por la identidad del producto; las líneas libres se mantienen separadas por descripción.
20. La suite ha pasado de un test vacío a 12 archivos y 28 métodos de prueba sobre seguridad, negocio, informes, validación y arquitectura.


## Verificación realizada

- 20/20 casos dinámicos dirigidos superados.
- 6/6 casos del bootstrap seguro superados.
- 19/19 comprobaciones estructurales superadas.
- 10/10 archivos JavaScript superaron `node --check`.
- Núcleo, servicios, controladores modificados, seguridad y las 28 pruebas compilaron mediante un arnés aislado con APIs sustitutas.