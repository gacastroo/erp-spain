# ERP Spain - Fase 1.3

Proyecto base de un ERP ligero para pymes españolas.

Esta versión incluye:

- Login seguro con Spring Security.
- Roles iniciales.
- Dashboard responsive.
- Módulo de clientes.
- Activar/desactivar clientes.
- Eliminar clientes con modal de confirmación.
- Módulo de productos y servicios.
- Activar/desactivar productos.
- Eliminar productos con modal de confirmación.
- Buscador y paginación.
- Validaciones backend.
- Migraciones Flyway.
- MySQL.
- Diseño responsive y accesible.

## Arranque

Desde la carpeta del proyecto:

```powershell
cd C:\Users\manana\Downloads\erp-spain-phase-1-3-products\erp-spain-phase-1-3-products
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
C:\tools\apache-maven-3.9.16\bin\mvn.cmd spring-boot:run
```

Abre:

```text
http://localhost:8080
```

Usuario inicial:

```text
admin@erp.local
Admin123!
```

## URLs principales

```text
/dashboard
/clients
/products
```

## Migraciones incluidas

```text
V1__init_schema.sql
V2__create_clients.sql
V3__create_products.sql
```

## Commit recomendado

```powershell
git add .
git commit -m "feat: añadir módulo de productos y servicios"
```


## Ajuste responsive de listados

Esta versión añade un patrón de interfaz más profesional para clientes y productos:

- En escritorio se muestran tablas sin scroll horizontal, con texto largo recortado mediante puntos suspensivos (`...`).
- En móvil se ocultan las tablas y se muestran tarjetas verticales para evitar barras horizontales.
- Los modales de eliminación funcionan tanto desde tabla como desde tarjeta móvil.
- Se eliminan las esquinas redondeadas superiores de las filas en clientes y productos.


## Fase 1.3 responsive fix

Esta versión corrige la visibilidad de la columna de acciones en escritorio:

- La columna de acciones tiene ancho fijo de 300px.
- Los botones no se recortan por `overflow: hidden`.
- En móvil y tablet se usan cards para evitar scroll horizontal.
- En escritorio amplio se muestra la tabla con textos largos en `...`.
