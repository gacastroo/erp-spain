# ERP Spain

ERP ligero para pymes españolas desarrollado con Spring Boot, Thymeleaf, Bootstrap 5, MySQL y Flyway.

## Requisitos

- Java 21
- Maven 3.9 o superior
- Docker Desktop, o una instalación local de MySQL 8

## Estructura principal

```text
src/main/java/com/ivan/erp/       Código Java organizado por dominio
src/main/resources/templates/     Vistas Thymeleaf
src/main/resources/static/css/    Estilos globales
src/main/resources/static/js/     JavaScript global
src/main/resources/static/js/pages/ Scripts específicos de cada pantalla
src/main/resources/db/migration/  Migraciones Flyway
scripts/database/                 Utilidades SQL manuales
```

## Arranque en desarrollo

```powershell
# Base de datos
docker compose up -d

# Aplicación
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
mvn clean spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`.

## Perfil de producción

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="cambia-esta-clave"
$env:REMEMBER_ME_KEY="cambia-esta-clave-larga-en-produccion"
$env:SESSION_COOKIE_SECURE="true"
mvn clean spring-boot:run
```

## Variables principales

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

## Usuario inicial de demostración

```text
admin@erp.local
Admin123!
```

Cambia estas credenciales antes de utilizar el proyecto fuera de un entorno local.
