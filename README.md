# ERP Spain — Fase 1.1

Base inicial profesional para un ERP ligero orientado a pymes españolas.

## Incluye

- Java 21 + Spring Boot.
- Spring Security con login propio.
- Contraseñas con BCrypt.
- CSRF activo.
- Roles iniciales: `ADMIN`, `MANAGER`, `USER`.
- Usuario administrador inicial.
- Entidad `Company`.
- Dashboard responsive con Bootstrap 5.
- Menú móvil tipo offcanvas.
- HTML semántico y detalles de accesibilidad: `label`, `aria-label`, `caption`, `skip-link` y foco visible.
- Flyway para migraciones.
- JPA con `open-in-view: false`.
- Índices básicos.
- Docker Compose opcional para MySQL.

## Requisitos

- Java 21.
- Maven 3.9+.
- MySQL 8+ o Docker.

## Arranque rápido con Docker

```bash
docker compose up -d
```

Después ejecuta:

```bash
mvn spring-boot:run
```

Abre:

```text
http://localhost:8080
```

Usuario inicial:

```text
Email: admin@erp.local
Password: Admin123!
```

## Arranque con MySQL local

Crea la base de datos:

```sql
CREATE DATABASE erp_spain CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Configura usuario/contraseña en `src/main/resources/application.yml` o usando variables de entorno:

```bash
set DB_USERNAME=root
set DB_PASSWORD=tu_password
mvn spring-boot:run
```

En PowerShell:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="tu_password"
mvn spring-boot:run
```

## Próxima fase

Fase 1.2 — Módulo de clientes:

- Entidad `Client`.
- Formulario con validaciones.
- Listado paginado y buscador.
- Crear/editar cliente.
- Activar/desactivar sin borrado físico.
- Mensajes flash.
- Pantallas responsive y accesibles.
