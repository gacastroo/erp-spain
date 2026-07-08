# ERP Spain — Fase 1.11 Rendimiento, seguridad y PageSpeed

Esta versión optimiza el proyecto para mejorar rendimiento percibido, seguridad HTTP, estabilidad del dashboard y preparación para producción.

## Incluye

- Recursos estáticos servidos en local mediante WebJars, sin depender de CDN externos para Bootstrap.
- Caché de recursos estáticos y cadena de recursos preparada para versionado por contenido.
- Compresión HTTP para HTML, CSS, JavaScript, JSON y SVG.
- Perfil `prod` con caché de Thymeleaf activada, cookies seguras y caché larga para estáticos.
- Cabeceras de seguridad reforzadas: CSP, HSTS, Referrer-Policy y Permissions-Policy.
- Clave de remember-me configurable mediante variable de entorno.
- Protección básica contra fuerza bruta en login por IP.
- Configuración de errores para no exponer stacktraces ni detalles internos.
- Ajustes HikariCP para conexiones MySQL más estables.
- Ajustes Hibernate para batching e inserciones/actualizaciones ordenadas.
- Dashboard live menos agresivo: evita petición inmediata extra en carga y refresca de forma más eficiente.
- Nuevos índices de base de datos para dashboard, reportes, IVA, facturas, cobros y gastos.
- Metadescripciones básicas en pantallas para mejorar auditorías generales.

## Arranque en desarrollo

```powershell
cd C:\Users\guill\Desktop\ERP-SPAIN\erp-spain

Remove-Item -Recurse -Force .\target -ErrorAction SilentlyContinue

$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"

C:\tools\apache-maven-3.9.16\bin\mvn.cmd clean spring-boot:run
```

## Arranque recomendado para probar rendimiento

Para medir rendimiento de forma más realista, arranca con perfil de producción:

```powershell
cd C:\Users\guill\Desktop\ERP-SPAIN\erp-spain

Remove-Item -Recurse -Force .\target -ErrorAction SilentlyContinue

$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
$env:SPRING_PROFILES_ACTIVE="prod"
$env:THYMELEAF_CACHE="true"
$env:STATIC_CACHE_MAX_AGE="365d"
$env:REMEMBER_ME_KEY="cambia-esta-clave-larga-en-produccion"

C:\tools\apache-maven-3.9.16\bin\mvn.cmd clean spring-boot:run
```

## Variables útiles

```text
DB_URL
DB_USERNAME
DB_PASSWORD
SPRING_PROFILES_ACTIVE
THYMELEAF_CACHE
STATIC_CACHE_MAX_AGE
SESSION_COOKIE_SECURE
REMEMBER_ME_KEY
LOGIN_RATE_LIMIT_ENABLED
LOGIN_RATE_LIMIT_MAX_ATTEMPTS
LOGIN_RATE_LIMIT_WINDOW_MINUTES
DB_POOL_MAX_SIZE
DB_POOL_MIN_IDLE
```

## URLs principales

```text
http://localhost:8080/dashboard
http://localhost:8080/clients
http://localhost:8080/products
http://localhost:8080/quotes
http://localhost:8080/invoices
http://localhost:8080/payments
http://localhost:8080/expenses
http://localhost:8080/reports
http://localhost:8080/taxes
http://localhost:8080/settings/company
```

## Usuario inicial

```text
admin@erp.local
Admin123!
```