# Importación CSV y Excel

La aplicación permite importar clientes y productos/servicios desde las pantallas de listado.

## Formatos admitidos

- CSV codificado en UTF-8 o Windows-1252.
- Excel XLSX.
- Excel XLS.
- Máximo 5 MB, 5.000 filas de datos y 100 columnas.
- Se usa la primera hoja del archivo Excel.
- La primera fila no vacía debe contener los encabezados.

## Clientes

Columnas obligatorias:

- `nombre_fiscal`
- `nif_cif_nie`

Columnas opcionales:

- `nombre_comercial`
- `email`
- `telefono`
- `direccion`
- `ciudad`
- `codigo_postal`
- `provincia`
- `pais`
- `tipo_cliente`
- `observaciones`

Valores de `tipo_cliente`: `Empresa`, `Autónomo`, `Particular` o `Administración pública`.
Si se omite, se usa `Empresa`. Si el país está vacío, se usa `España`.

## Productos y servicios

Columnas obligatorias:

- `nombre`
- `precio`

Columnas opcionales:

- `descripcion`
- `sku`
- `tipo`
- `iva`

Valores de `tipo`: `Producto` o `Servicio`. Si se omite, se usa `Producto`.
Si el IVA está vacío, se usa `21`.

Los importes admiten coma o punto decimal y separadores de miles habituales, por ejemplo:

- `49,90`
- `49.90`
- `1.234,56`
- `1,234.56`

## Comportamiento

Cada fila válida se crea mediante los mismos servicios usados por los formularios manuales. Se aplican las mismas validaciones y comprobaciones de duplicados.

La importación es parcial: las filas correctas se guardan y las filas incorrectas se omiten. Al terminar se muestra el número de filas procesadas, importadas y rechazadas, junto con el motivo de cada error.

Las plantillas vacías se pueden descargar en CSV o XLSX desde las pantallas de importación.
