# ERP Spain

Aplicación web de gestión empresarial orientada a autónomos, pequeñas empresas y equipos administrativos.

ERP Spain permite centralizar clientes, productos y servicios, presupuestos, facturas, cobros, gastos, reportes, impuestos y configuración empresarial desde una única interfaz responsive.

---

## Estado del proyecto

El proyecto se encuentra en desarrollo activo y dispone de una base funcional completa:

- Autenticación y control de acceso.
- Dashboard financiero.
- Gestión comercial.
- Facturación y cobros.
- Control de gastos.
- Informes y fiscalidad.
- Exportación de documentos.
- Diseño responsive para ordenador, tablet y móvil.
- Modos claro y oscuro persistentes.
- Base de datos versionada mediante Flyway.

---

## Últimos cambios

### Rediseño completo de la interfaz

Se ha renovado toda la interfaz visual de ERP Spain con un diseño corporativo, limpio y consistente:

- Nueva identidad visual para ERP Spain.
- Rediseño completo del login.
- Navegación lateral reorganizada por categorías.
- Menú móvil mediante panel lateral `offcanvas`.
- Modo claro y oscuro persistente.
- Eliminación de indicadores visuales innecesarios de estado online.
- Cabeceras, tarjetas, formularios y tablas unificados.
- Mejor contraste y legibilidad.
- Estados de presupuestos y facturas más claros.
- Botones de acción alineados de forma uniforme.
- Mayor separación entre botones en dispositivos móviles.
- Texto e iconos centrados horizontal y verticalmente.
- Botones con una altura mínima adecuada para pantallas táctiles.
- Botón de desactivación con borde claramente visible.
- Botones de navegación contextual situados debajo del navbar.
- Usuario autenticado y cierre de sesión disponibles en todos los módulos.
- Interfaz adaptada a móviles, tablets y ordenadores.
- Compatibilidad mejorada con proxies HTTPS y Dev Tunnels.
- Limpieza y organización de estilos CSS y comportamiento JavaScript.

---

## Funcionalidades

### Dashboard

El dashboard ofrece una visión general de la actividad:

- Clientes registrados.
- Presupuestos pendientes.
- Facturas emitidas.
- Facturas pendientes de cobro.
- Cobros registrados.
- Gastos recientes.
- Indicadores financieros.
- Accesos rápidos a los principales módulos.

### Clientes

Permite administrar la cartera de clientes:

- Crear clientes.
- Editar información fiscal y de contacto.
- Buscar por nombre, nombre comercial, NIF/CIF o correo.
- Diferenciar particulares y empresas.
- Activar o desactivar clientes.
- Eliminar clientes sin información relacionada.
- Evitar la eliminación accidental de clientes con documentos asociados.
- Consultar clientes desde tablas de escritorio o tarjetas móviles.

### Productos y servicios

Permite mantener el catálogo utilizado en presupuestos y facturas:

- Crear productos o servicios.
- Definir descripción y precio.
- Establecer el tipo de producto.
- Editar elementos existentes.
- Activar o desactivar productos.
- Utilizar los productos en líneas de documentos comerciales.

### Presupuestos

El módulo de presupuestos permite:

- Crear presupuestos asociados a clientes.
- Añadir múltiples líneas.
- Seleccionar productos o servicios.
- Modificar cantidades, precios e impuestos.
- Calcular bases imponibles, impuestos y totales.
- Consultar el detalle completo.
- Cambiar el estado del presupuesto.
- Generar documentos PDF.
- Buscar y filtrar presupuestos.

Los presupuestos pueden utilizar estados como:

- Borrador.
- Enviado.
- Aceptado.
- Rechazado.

### Facturas

El módulo de facturación permite:

- Crear facturas para clientes.
- Añadir líneas de productos o servicios.
- Gestionar numeración documental.
- Calcular importes e impuestos.
- Consultar el detalle de una factura.
- Generar documentos PDF.
- Controlar estados.
- Consultar el resumen de cobros asociados.

Entre los estados disponibles se incluyen:

- Borrador.
- Emitida.
- Enviada.
- Vencida.
- Cobrada.
- Cancelada.

### Cobros

Permite registrar los ingresos recibidos:

- Asociar cobros a facturas.
- Seleccionar el método de pago.
- Registrar fecha e importe.
- Añadir referencias y observaciones.
- Consultar el historial de cobros.
- Mostrar resúmenes por factura.
- Actualizar automáticamente el estado económico de una factura.

### Gastos

Permite controlar los gastos de la empresa:

- Crear y editar gastos.
- Registrar proveedor o concepto.
- Asignar fecha.
- Definir base imponible e impuestos.
- Clasificar gastos.
- Consultar el detalle.
- Buscar y filtrar registros.

### Reportes

El módulo de reportes permite analizar la actividad:

- Ventas por cliente.
- Ventas por producto.
- Resúmenes de ingresos.
- Información de cobros.
- Comparación de periodos.
- Datos agregados para gestión.
- Exportación de reportes.

### Impuestos

Permite consultar información fiscal calculada a partir de facturas y gastos:

- IVA repercutido.
- IVA soportado.
- Resultado fiscal del periodo.
- Desglose por tipo impositivo.
- Consulta por fechas.
- Exportación de información fiscal.

### Configuración de empresa

Permite configurar los datos utilizados en documentos y procesos internos:

- Razón social.
- NIF/CIF.
- Dirección.
- Información de contacto.
- Series y numeración documental.
- Datos mostrados en presupuestos y facturas.

---

## Flujo de funcionamiento

El flujo principal de ERP Spain es:

```text
Inicio de sesión
       │
       ▼
Dashboard
       │
       ├── Clientes
       │      │
       │      └── Presupuestos
       │               │
       │               └── Facturas
       │                        │
       │                        └── Cobros
       │
       ├── Productos y servicios
       │
       ├── Gastos
       │
       ├── Reportes
       │
       ├── Impuestos
       │
       └── Configuración de empresa
```

---

## Arquitectura

La aplicación utiliza una arquitectura por capas:

```text
Controlador
    ↓
Servicio
    ↓
Repositorio
    ↓
Base de datos MySQL
```

Las vistas se renderizan en el servidor mediante **Thymeleaf**.

---

## Tecnologías utilizadas

### Backend

- Java 21
- Spring Boot 3.5
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate
- Bean Validation
- Flyway
- Maven

### Frontend

- Thymeleaf
- Bootstrap 5.3
- HTML5
- CSS3
- JavaScript sin frameworks adicionales
- Diseño responsive
- Modo claro y oscuro

### Base de datos

- MySQL 8
- Docker Compose
- Migraciones automáticas con Flyway

---

## Seguridad

ERP Spain incluye:

- Inicio de sesión mediante Spring Security
- Contraseñas cifradas
- Protección CSRF
- Control de acceso por roles
- Limitación de intentos de inicio de sesión
- Cookies de sesión protegidas
- Cabeceras compatibles con proxies inversos
- Prevención de almacenamiento de páginas privadas en caché
- Formularios protegidos mediante tokens CSRF
- Separación entre configuración de desarrollo y producción

Los usuarios con permisos administrativos o de gestión pueden realizar operaciones sensibles como crear, editar, desactivar o eliminar registros.

---

## Requisitos

Antes de iniciar el proyecto es necesario instalar:

- Java JDK 21 o superior
- Maven 3.9 o superior
- Docker Desktop, o una instalación local de MySQL 8
- Git
- Un navegador moderno

Comprobar las instalaciones:

```powershell
java -version
mvn -version
docker --version
git --version
```

---

## Instalación

### 1. Clonar el repositorio

```powershell
git clone https://github.com/gacastroo/erp-spain.git
cd erp-spain
```

La carpeta abierta debe contener:

```text
pom.xml
docker-compose.yml
src
```

### 2. Iniciar MySQL con Docker

```powershell
docker compose up -d
```

Comprobar el estado:

```powershell
docker compose ps
```

El contenedor `erp-spain-mysql` debe aparecer como iniciado.

### 3. Configurar la conexión

Revisa el puerto configurado en `docker-compose.yml`.

Ejemplo habitual:

```yaml
ports:
  - "3307:3306"
```

En este caso, configura las variables desde PowerShell:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3307/erp_spain?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Madrid&useUnicode=true&characterEncoding=utf8"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
```

Cuando Docker utilice directamente el puerto `3306`, sustituye `3307` por `3306`.

### 4. Ejecutar la aplicación

```powershell
mvn clean spring-boot:run
```

La aplicación estará disponible en:

```text
http://localhost:8080/login
```

### 5. Crear el administrador inicial de forma segura

La aplicación no contiene credenciales predeterminadas y no crea usuarios automáticamente. Para el primer arranque, define temporalmente estas variables con una contraseña única y robusta:

```powershell
$env:INITIAL_ADMIN_ENABLED="true"
$env:INITIAL_ADMIN_EMAIL="tu-admin@empresa.es"
$env:INITIAL_ADMIN_PASSWORD="UnaClaveUnica-2026!"
$env:INITIAL_ADMIN_FIRST_NAME="Administrador"
$env:INITIAL_ADMIN_LAST_NAME="ERP"
```

La contraseña debe tener al menos 12 caracteres e incluir mayúsculas, minúsculas, números y símbolos. Tras comprobar que puedes iniciar sesión, desactiva el bootstrap y elimina el secreto del entorno:

```powershell
$env:INITIAL_ADMIN_ENABLED="false"
Remove-Item Env:INITIAL_ADMIN_PASSWORD
```

Si el usuario indicado ya existe, el inicializador no modifica su contraseña ni sus permisos.

La opción «recordarme» está desactivada por defecto. Para activarla, utiliza un secreto aleatorio de al menos 32 caracteres y nunca lo guardes en el repositorio:

```powershell
$env:REMEMBER_ME_ENABLED="true"
$env:REMEMBER_ME_KEY="un-secreto-aleatorio-de-al-menos-32-caracteres"
```

---

## Acceso desde otro dispositivo

Cuando el ordenador y el dispositivo están en la misma red:

```text
http://IP_DEL_ORDENADOR:8080/login
```

Para comprobar la dirección IP en Windows:

```powershell
ipconfig
```

La aplicación escucha en el puerto `8080`.

Para pruebas desde una red externa puede utilizarse un túnel HTTPS, por ejemplo mediante el reenvío de puertos de Visual Studio Code.

No deben compartirse públicamente las direcciones de túneles que permitan acceder a la aplicación.

---

## Base de datos y migraciones

Flyway crea y actualiza automáticamente el esquema al iniciar la aplicación.

Migraciones incluidas:

```text
V1  - Esquema inicial
V2  - Clientes
V3  - Productos
V4  - Presupuestos
V5  - Facturas
V6  - Cobros
V7  - Gastos
V8  - Configuración empresarial y documental
V10 - Índices de rendimiento
V11 - Contadores documentales atómicos
```

No deben modificarse migraciones que ya hayan sido aplicadas en una base de datos compartida. Los cambios posteriores deben añadirse mediante una nueva migración.

Los datos de ejemplo están aislados de las migraciones normales. Solo se cargan al activar explícitamente el perfil `demo`:

```powershell
$env:SPRING_PROFILES_ACTIVE="demo"
mvn spring-boot:run
```

La migración de ejemplo se encuentra en `src/main/resources/db/demo/V9__seed_demo_data.sql`; el perfil normal y el perfil `prod` no incluyen esa ubicación. El script `scripts/database/clean-demo-data.sql` permite limpiar esos datos durante el desarrollo.

---

## Configuración de producción

El proyecto incluye:

```text
src/main/resources/application-prod.yml
```

Para activar el perfil de producción:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
```

Las credenciales y secretos deben configurarse mediante variables de entorno:

```powershell
$env:DB_URL="jdbc:mysql://servidor:3306/erp_spain"
$env:DB_USERNAME="usuario_seguro"
$env:DB_PASSWORD="contraseña_segura"
$env:SESSION_COOKIE_SECURE="true"
```

En producción se recomienda:

- Utilizar HTTPS
- Cambiar todas las credenciales iniciales
- No almacenar contraseñas en el repositorio
- Utilizar una base de datos con copias de seguridad
- Ejecutar la aplicación detrás de un proxy inverso
- Mantener Java, Maven, Docker y MySQL actualizados
- Revisar los permisos asignados a cada usuario

---

## Comandos útiles

### Iniciar la base de datos

```powershell
docker compose up -d
```

### Detener los contenedores

```powershell
docker compose down
```

### Ver los registros de MySQL

```powershell
docker compose logs mysql
```

### Eliminar la compilación anterior

```powershell
Remove-Item -Recurse -Force .\target -ErrorAction SilentlyContinue
```

### Ejecutar la aplicación

```powershell
mvn spring-boot:run
```

### Ejecutar las pruebas

```powershell
mvn test
```

La suite cubre el bootstrap seguro, numeración documental, cobros, estados de factura, vencimientos, informes, validaciones, limitación de acceso, CSP y aislamiento de datos demo.

### Generar el paquete

```powershell
mvn clean package
```

### Ejecutar el JAR generado

```powershell
java -jar target/erp-spain-0.0.1-SNAPSHOT.jar
```

---

## Estructura principal

```text
erp-spain/
├── .github/
│   └── workflows/
├── scripts/
│   └── database/
├── src/
│   ├── main/
│   │   ├── java/com/ivan/erp/
│   │   │   ├── auth/
│   │   │   ├── client/
│   │   │   ├── company/
│   │   │   ├── dashboard/
│   │   │   ├── expense/
│   │   │   ├── invoice/
│   │   │   ├── payment/
│   │   │   ├── product/
│   │   │   ├── quote/
│   │   │   ├── report/
│   │   │   ├── security/
│   │   │   ├── shared/
│   │   │   ├── tax/
│   │   │   └── user/
│   │   └── resources/
│   │       ├── db/migration/
│   │       ├── static/css/
│   │       ├── static/js/
│   │       ├── templates/
│   │       ├── application.yml
│   │       └── application-prod.yml
│   └── test/
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## Autor

**Guillermo Castro Abarca**

- Usuario de GitHub: `gacastroo`
- Proyecto: `ERP Spain`

---

## Licencia y propiedad intelectual

Copyright © 2026 Guillermo Castro Abarca. Todos los derechos reservados.

ERP Spain es software propietario y no se distribuye bajo una licencia de código abierto.

No se concede ninguna licencia, expresa o implícita, para utilizar, copiar, reproducir, modificar, adaptar, traducir, publicar, distribuir, sublicenciar, vender, desplegar comercialmente o crear trabajos derivados de este proyecto, total o parcialmente, sin autorización previa y por escrito de Guillermo Castro Abarca.

La publicación del código fuente en un repositorio no implica autorización para su reutilización fuera de los permisos mínimos concedidos por la plataforma que aloja el repositorio.

Las marcas, nombres comerciales, diseños, documentación, código fuente, estructura de base de datos y recursos originales incluidos en ERP Spain pertenecen a su autor.

Las librerías y dependencias de terceros mantienen sus respectivas licencias y derechos de autor.

Para solicitar autorización de uso, distribución o colaboración deberá contactarse directamente con el autor.

**No se autoriza el uso comercial ni la redistribución de este proyecto.**