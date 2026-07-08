# ERP Spain - Fase 1.4 responsive actions fix

Corrección sobre la fase 1.4:

- Botones de acciones alineados en responsive para Clientes, Productos y Presupuestos.
- En móvil se muestran en columna, todos con el mismo alto y ancho.
- En tablet se muestran en 3 columnas uniformes.
- Se mantiene la tabla en escritorio amplio.

## Arranque

```powershell
cd C:\Users\manana\Downloads\erp-spain-phase-1-4-responsive-actions-fix\erp-spain-phase-1-4-responsive-actions-fix
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
C:\tools\apache-maven-3.9.16\bin\mvn.cmd spring-boot:run
```
