# ERP Spain

## Descripción

**ERP Spain** consiste en una aplicación web de gestión empresarial orientada a autónomos, pequeñas empresas y equipos administrativos.

La aplicación permite centralizar desde una única interfaz la gestión de clientes, productos y servicios, presupuestos, facturas, cobros, gastos, reportes, impuestos y configuración empresarial.

El objetivo del proyecto es proporcionar una herramienta sencilla, segura y responsive que permita controlar los principales procesos comerciales, administrativos y financieros de una empresa.

ERP Spain cuenta con una interfaz adaptada a ordenadores, tablets y dispositivos móviles, además de modos claro y oscuro persistentes.

---

## Aspectos tecnológicos

| Tecnología                  | Descripción                                                         |
| --------------------------- | ------------------------------------------------------------------- |
| **Backend**                 |                                                                     |
| Java 21                     | Lenguaje principal utilizado para desarrollar la aplicación.        |
| Spring Boot 3.5             | Framework principal para la configuración y ejecución del proyecto. |
| Spring MVC                  | Gestión de controladores, rutas HTTP y renderizado de vistas.       |
| Spring Security             | Autenticación, autorización, protección CSRF y control de acceso.   |
| Spring Data JPA + Hibernate | Gestión de entidades, repositorios y persistencia de datos.         |
| Bean Validation             | Validación de formularios y datos de entrada.                       |
| Maven                       | Gestión de dependencias, compilación y ejecución del proyecto.      |
| **Frontend**                |                                                                     |
| Thymeleaf                   | Renderizado de las vistas HTML desde el servidor.                   |
| Bootstrap 5.3               | Diseño visual, componentes y adaptación responsive.                 |
| HTML5 + CSS3                | Estructura y personalización de la interfaz.                        |
| JavaScript                  | Comportamiento dinámico de formularios, navegación y componentes.   |
| **Base de datos**           |                                                                     |
| MySQL 8                     | Base de datos relacional utilizada por la aplicación.               |
| Flyway                      | Creación y actualización versionada del esquema de base de datos.   |
| Docker Compose              | Ejecución de MySQL en un contenedor para el entorno de desarrollo.  |
| **Extras**                  |                                                                     |
| OpenPDF                     | Generación de presupuestos y facturas en formato PDF.               |
| Apache POI                  | Lectura e importación de archivos XLS y XLSX.                       |
| Commons CSV                 | Procesamiento de importaciones mediante archivos CSV.               |
| JUnit + Spring Boot Test    | Pruebas unitarias y de integración.                                 |
| GitHub Actions              | Automatización de pruebas y comprobaciones del proyecto.            |

---

# Guía de instalación

## 📦 Manual de instalación

### ✅ 1. Requisitos previos

Antes de ejecutar el proyecto es necesario instalar:

* Java JDK 21 o superior.
* Maven 3.9 o superior.
* Docker Desktop o una instalación local de MySQL 8.
* Git.
* Un navegador web moderno.

Puedes comprobar las instalaciones mediante los siguientes comandos:

```powershell
java -version
mvn -version
docker --version
git --version
```

---

## 2. Procedimiento de instalación

### Clonar el repositorio

```powershell
git clone https://github.com/gacastroo/erp-spain.git
cd erp-spain
```

La carpeta principal del proyecto debe contener, entre otros elementos:

```text
erp-spain/
├── src/
├── scripts/
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 🗄️ Base de datos MySQL

### Iniciar MySQL mediante Docker

```powershell
docker compose up -d
```

Comprobar que el contenedor se ha iniciado correctamente:

```powershell
docker compose ps
```

El contenedor `erp-spain-mysql` debe aparecer en ejecución.

### Configurar la conexión

El archivo `docker-compose.yml` puede exponer MySQL mediante el puerto `3307`:

```yaml
ports:
  - "3307:3306"
```

En ese caso, configura las variables de entorno desde PowerShell:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3307/erp_spain?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Madrid&useUnicode=true&characterEncoding=utf8"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
```

Cuando MySQL utilice directamente el puerto `3306`, sustituye `3307` por `3306` en la URL de conexión.

---

## 🚀 Ejecutar la aplicación

```powershell
mvn clean spring-boot:run
```

La aplicación estará disponible en:

```text
http://localhost:8080/login
```

---

## 👤 Crear el administrador inicial

ERP Spain no incluye usuarios ni contraseñas predeterminadas.

Para crear el primer usuario administrador, configura temporalmente las siguientes variables de entorno:

```powershell
$env:INITIAL_ADMIN_ENABLED="true"
$env:INITIAL_ADMIN_EMAIL="tu-admin@empresa.es"
$env:INITIAL_ADMIN_PASSWORD="UnaClaveUnica-2026!"
$env:INITIAL_ADMIN_FIRST_NAME="Administrador"
$env:INITIAL_ADMIN_LAST_NAME="ERP"
```

La contraseña debe contener al menos 12 caracteres e incluir:

* Letras mayúsculas.
* Letras minúsculas.
* Números.
* Símbolos.

Después de iniciar sesión correctamente, desactiva la creación automática del administrador y elimina la contraseña del entorno:

```powershell
$env:INITIAL_ADMIN_ENABLED="false"
Remove-Item Env:INITIAL_ADMIN_PASSWORD
```

Si el usuario indicado ya existe, el inicializador no modificará su contraseña ni sus permisos.

---

## 🔐 Función «Recordarme»

La opción «Recordarme» está desactivada de forma predeterminada.

Para activarla, configura un secreto aleatorio de al menos 32 caracteres:

```powershell
$env:REMEMBER_ME_ENABLED="true"
$env:REMEMBER_ME_KEY="un-secreto-aleatorio-de-al-menos-32-caracteres"
```

Este secreto no debe almacenarse dentro del repositorio.

---

## 💻 Acceso a la aplicación

### Acceso local

```text
http://localhost:8080/login
```

### Acceso desde otro dispositivo de la misma red

```text
http://IP_DEL_ORDENADOR:8080/login
```

En Windows puedes consultar la dirección IP mediante:

```powershell
ipconfig
```

La aplicación escucha de manera predeterminada en el puerto `8080`.

---

## 🌍 Sin instalación local — Producción

El repositorio incluye una configuración específica para producción:

```text
src/main/resources/application-prod.yml
```

Para activar el perfil de producción:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
```

Actualmente no se especifica en el repositorio una URL pública de producción.

Cuando la aplicación se despliegue, podrá añadirse aquí:

```text
🔹 Aplicación: [URL pública pendiente]
```

---

# 📁 Estructura del proyecto

```text
erp-spain/
├── .github/
│   └── workflows/                 # Integración continua
├── scripts/
│   └── database/                  # Scripts auxiliares de base de datos
├── src/
│   ├── main/
│   │   ├── java/com/ivan/erp/
│   │   │   ├── auth/             # Autenticación
│   │   │   ├── client/           # Clientes
│   │   │   ├── company/          # Configuración empresarial
│   │   │   ├── dashboard/        # Panel principal
│   │   │   ├── expense/          # Gastos
│   │   │   ├── invoice/          # Facturas
│   │   │   ├── payment/          # Cobros
│   │   │   ├── product/          # Productos y servicios
│   │   │   ├── quote/            # Presupuestos
│   │   │   ├── report/           # Reportes
│   │   │   ├── security/         # Configuración de seguridad
│   │   │   ├── shared/           # Componentes compartidos
│   │   │   ├── tax/              # Impuestos
│   │   │   └── user/             # Usuarios y permisos
│   │   └── resources/
│   │       ├── db/migration/      # Migraciones de Flyway
│   │       ├── static/css/        # Estilos
│   │       ├── static/js/         # JavaScript
│   │       ├── templates/         # Plantillas Thymeleaf
│   │       ├── application.yml
│   │       └── application-prod.yml
│   └── test/                      # Pruebas automatizadas
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

# Características clave

* 📊 Dashboard con indicadores comerciales y financieros.
* 👥 Gestión de clientes particulares y empresas.
* 📦 Gestión de productos y servicios.
* 📄 Creación y seguimiento de presupuestos.
* 🧾 Creación y gestión de facturas.
* 💳 Registro y seguimiento de cobros.
* 💸 Control de gastos empresariales.
* 📈 Generación de reportes.
* 🧮 Cálculo de IVA repercutido y soportado.
* 🏢 Configuración de información empresarial.
* 📥 Importación masiva de clientes y productos.
* 📤 Exportación de documentos y reportes.
* 📑 Generación de presupuestos y facturas en PDF.
* 🔐 Autenticación y control de acceso mediante Spring Security.
* 🛡️ Protección CSRF y limitación de intentos de acceso.
* 🌓 Modos claro y oscuro persistentes.
* 📱 Interfaz responsive para ordenador, tablet y móvil.
* 🗃️ Migraciones versionadas mediante Flyway.

---

# 📊 Módulos principales

## Dashboard

Muestra una visión general de la actividad empresarial:

* Clientes registrados.
* Presupuestos pendientes.
* Facturas emitidas.
* Facturas pendientes de cobro.
* Cobros registrados.
* Gastos recientes.
* Indicadores financieros.
* Accesos rápidos a los módulos principales.

## Clientes

Permite crear, editar, buscar, activar y desactivar clientes.

También permite:

* Diferenciar particulares y empresas.
* Gestionar información fiscal y de contacto.
* Impedir la eliminación accidental de clientes con documentos asociados.
* Importar clientes desde archivos CSV, XLSX o XLS.

## Productos y servicios

Permite administrar el catálogo utilizado en presupuestos y facturas:

* Nombre y descripción.
* Precio.
* Tipo de producto o servicio.
* Estado activo o inactivo.
* Importación masiva desde CSV, XLSX o XLS.
* IVA predeterminado del 21 %.

## Presupuestos

Permite:

* Crear presupuestos asociados a clientes.
* Añadir múltiples líneas.
* Seleccionar productos o servicios.
* Modificar cantidades, precios e impuestos.
* Calcular bases imponibles, impuestos y totales.
* Cambiar el estado del presupuesto.
* Buscar y filtrar documentos.
* Generar presupuestos en PDF.

Estados disponibles:

* Borrador.
* Enviado.
* Aceptado.
* Rechazado.

## Facturas

Permite:

* Crear facturas para clientes.
* Gestionar la numeración documental.
* Añadir líneas de productos o servicios.
* Calcular importes e impuestos.
* Controlar el estado de las facturas.
* Consultar cobros asociados.
* Generar facturas en PDF.

Estados disponibles:

* Borrador.
* Emitida.
* Enviada.
* Vencida.
* Cobrada.
* Cancelada.

## Cobros

Permite registrar ingresos asociados a facturas:

* Fecha e importe.
* Método de pago.
* Referencias.
* Observaciones.
* Historial de cobros.
* Resúmenes por factura.
* Actualización automática del estado económico.

## Gastos

Permite registrar y consultar gastos:

* Proveedor o concepto.
* Fecha.
* Base imponible.
* Impuestos.
* Categoría.
* Búsqueda y filtrado.

## Reportes

Incluye:

* Ventas por cliente.
* Ventas por producto.
* Resúmenes de ingresos.
* Información de cobros.
* Comparación entre periodos.
* Exportación de reportes.

## Impuestos

Permite consultar:

* IVA repercutido.
* IVA soportado.
* Resultado fiscal del periodo.
* Desglose por tipo impositivo.
* Información filtrada por fechas.
* Exportación de información fiscal.

## Configuración empresarial

Permite establecer:

* Razón social.
* NIF o CIF.
* Dirección.
* Información de contacto.
* Series documentales.
* Numeración de presupuestos y facturas.
* Datos mostrados en los documentos PDF.

---

# 🏗️ Arquitectura

ERP Spain utiliza una arquitectura por capas:

```text
Controlador
    ↓
Servicio
    ↓
Repositorio
    ↓
Base de datos MySQL
```

Las vistas se renderizan desde el servidor mediante Thymeleaf.

La aplicación no utiliza un frontend React separado ni una API backend desplegada de forma independiente. Spring Boot se encarga de la lógica de negocio, la seguridad, el acceso a datos y el renderizado de la interfaz.

---

# 🔐 Seguridad

ERP Spain incorpora las siguientes medidas:

* Inicio de sesión mediante Spring Security.
* Contraseñas almacenadas de forma cifrada.
* Protección CSRF.
* Control de acceso mediante roles.
* Limitación de intentos de inicio de sesión.
* Cookies de sesión protegidas.
* Prevención del almacenamiento en caché de páginas privadas.
* Cabeceras compatibles con proxies inversos.
* Separación entre configuración de desarrollo y producción.
* Creación segura del administrador inicial.
* Ausencia de credenciales predeterminadas en el repositorio.

Los usuarios con permisos administrativos o de gestión pueden realizar operaciones sensibles como crear, editar, desactivar o eliminar registros.

---

# 🗃️ Base de datos y migraciones

Flyway crea y actualiza automáticamente el esquema de la base de datos al iniciar la aplicación.

Entre las migraciones incluidas se encuentran:

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

No deben modificarse migraciones que ya hayan sido aplicadas en una base de datos compartida.

Los cambios posteriores deben añadirse mediante una nueva migración.

Los datos de ejemplo están separados de las migraciones normales y solo se cargan cuando se activa expresamente el perfil `demo`:

```powershell
$env:SPRING_PROFILES_ACTIVE="demo"
mvn spring-boot:run
```

---

# Despliegue

La aplicación puede desplegarse como un archivo JAR de Spring Boot conectado a una base de datos MySQL.

## Generar el paquete

```powershell
mvn clean package
```

## Ejecutar el JAR

```powershell
java -jar target/erp-spain-0.0.1-SNAPSHOT.jar
```

## Variables de entorno de producción

```env
SPRING_PROFILES_ACTIVE=prod

DB_URL=jdbc:mysql://servidor:3306/erp_spain
DB_USERNAME=usuario_seguro
DB_PASSWORD=contraseña_segura

SESSION_COOKIE_SECURE=true

INITIAL_ADMIN_ENABLED=false

REMEMBER_ME_ENABLED=false
REMEMBER_ME_KEY=secreto-aleatorio-de-al-menos-32-caracteres
```

En producción se recomienda:

* Utilizar HTTPS.
* No almacenar contraseñas en el repositorio.
* Cambiar todas las credenciales iniciales.
* Utilizar una base de datos con copias de seguridad.
* Ejecutar la aplicación detrás de un proxy inverso.
* Mantener Java, Maven, Docker y MySQL actualizados.
* Revisar los permisos asignados a cada usuario.

---

# 🧪 Testing

Para ejecutar las pruebas:

```powershell
mvn test
```

La suite de pruebas cubre, entre otros elementos:

* Creación segura del administrador inicial.
* Numeración documental.
* Gestión de cobros.
* Estados de factura.
* Vencimientos.
* Reportes.
* Validaciones.
* Limitación de intentos de acceso.
* Política CSP.
* Separación de los datos de demostración.

---

# 📚 Bibliografía

Spring. (s. f.). *Spring Boot Documentation*.
https://docs.spring.io/spring-boot/

Spring. (s. f.). *Spring Security Reference*.
https://docs.spring.io/spring-security/reference/

Spring. (s. f.). *Spring Data JPA*.
https://spring.io/projects/spring-data-jpa

Thymeleaf. (s. f.). *Thymeleaf Documentation*.
https://www.thymeleaf.org/documentation.html

Bootstrap. (s. f.). *Bootstrap Documentation*.
https://getbootstrap.com/docs/

MySQL. (s. f.). *MySQL 8.0 Reference Manual*.
https://dev.mysql.com/doc/refman/8.0/en/

Flyway. (s. f.). *Flyway Documentation*.
https://documentation.red-gate.com/flyway

Docker. (s. f.). *Docker Compose Documentation*.
https://docs.docker.com/compose/

Apache Maven. (s. f.). *Maven Documentation*.
https://maven.apache.org/guides/

Hibernate. (s. f.). *Hibernate ORM Documentation*.
https://hibernate.org/orm/documentation/

GitHub. (s. f.). *GitHub Actions Documentation*.
https://docs.github.com/actions

---

# Autor

**Guillermo Castro Abarca**

Usuario de GitHub: `gacastroo`

---

# Licencia y propiedad intelectual

Copyright © 2026 Guillermo Castro Abarca. Todos los derechos reservados.

ERP Spain es software propietario y no se distribuye bajo una licencia de código abierto.

No se concede ninguna licencia, expresa o implícita, para utilizar, copiar, reproducir, modificar, adaptar, traducir, publicar, distribuir, sublicenciar, vender, desplegar comercialmente o crear trabajos derivados de este proyecto, total o parcialmente, sin autorización previa y por escrito de Guillermo Castro Abarca.

La publicación del código fuente en un repositorio no implica autorización para su reutilización fuera de los permisos mínimos concedidos por la plataforma que aloja el repositorio.

Las librerías y dependencias de terceros mantienen sus respectivas licencias y derechos de autor.

**No se autoriza el uso comercial ni la redistribución de este proyecto.**

---

# Enlace del repositorio

https://github.com/gacastroo/erp-spain

# Enlace de despliegue

Actualmente no se ha indicado una URL pública de despliegue.

```text
[URL de despliegue pendiente]
```
