package com.ivan.erp.importing;

import com.ivan.erp.client.ClientType;
import com.ivan.erp.client.service.ClientService;
import com.ivan.erp.client.web.ClientForm;
import com.ivan.erp.product.ProductType;
import com.ivan.erp.product.service.ProductService;
import com.ivan.erp.product.web.ProductForm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataImportService {

    private static final int MAX_REPORTED_ERRORS = 100;

    private static final String LEGAL_NAME = "legalName";
    private static final String COMMERCIAL_NAME = "commercialName";
    private static final String TAX_ID = "taxId";
    private static final String EMAIL = "email";
    private static final String PHONE = "phone";
    private static final String ADDRESS = "addressLine";
    private static final String CITY = "city";
    private static final String POSTAL_CODE = "postalCode";
    private static final String PROVINCE = "province";
    private static final String COUNTRY = "country";
    private static final String CLIENT_TYPE = "clientType";
    private static final String NOTES = "notes";

    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String SKU = "sku";
    private static final String PRODUCT_TYPE = "productType";
    private static final String UNIT_PRICE = "unitPrice";
    private static final String VAT_RATE = "vatRate";

    private static final Map<String, String> CLIENT_HEADER_ALIASES = aliases(
            alias(LEGAL_NAME, "nombre_fiscal", "razon_social", "nombre", "legal_name"),
            alias(COMMERCIAL_NAME, "nombre_comercial", "commercial_name"),
            alias(TAX_ID, "nif", "cif", "nie", "nif_cif", "nif_cif_nie", "tax_id", "identificacion_fiscal"),
            alias(EMAIL, "email", "correo", "correo_electronico"),
            alias(PHONE, "telefono", "movil", "phone"),
            alias(ADDRESS, "direccion", "domicilio", "address", "address_line"),
            alias(CITY, "ciudad", "localidad", "city"),
            alias(POSTAL_CODE, "codigo_postal", "cp", "postal_code"),
            alias(PROVINCE, "provincia", "province"),
            alias(COUNTRY, "pais", "country"),
            alias(CLIENT_TYPE, "tipo", "tipo_cliente", "client_type"),
            alias(NOTES, "observaciones", "notas", "notes")
    );

    private static final Map<String, String> PRODUCT_HEADER_ALIASES = aliases(
            alias(NAME, "nombre", "name", "producto", "servicio", "nombre_producto", "nombre_servicio"),
            alias(DESCRIPTION, "descripcion", "description"),
            alias(SKU, "sku", "referencia", "codigo", "codigo_producto"),
            alias(PRODUCT_TYPE, "tipo", "tipo_producto", "product_type"),
            alias(UNIT_PRICE, "precio", "precio_base", "precio_unitario", "unit_price"),
            alias(VAT_RATE, "iva", "vat", "tasa_iva", "vat_rate", "porcentaje_iva")
    );

    private final TabularFileReader fileReader;
    private final ClientService clientService;
    private final ProductService productService;
    private final Validator validator;

    public DataImportService(
            TabularFileReader fileReader,
            ClientService clientService,
            ProductService productService,
            Validator validator
    ) {
        this.fileReader = fileReader;
        this.clientService = clientService;
        this.productService = productService;
        this.validator = validator;
    }

    public ImportResult importClients(MultipartFile file) {
        TabularData data = fileReader.read(file);
        Map<String, Integer> columns = mapColumns(data.headers(), CLIENT_HEADER_ALIASES);
        requireColumns(columns, Map.of(
                LEGAL_NAME, "nombre_fiscal",
                TAX_ID, "nif_cif_nie"
        ));

        ImportAccumulator result = new ImportAccumulator();
        for (TabularRow row : data.rows()) {
            result.processed++;
            ClientForm form;
            try {
                form = clientForm(row, columns);
            } catch (IllegalArgumentException ex) {
                result.fail(row.rowNumber(), value(row, columns, TAX_ID), ex.getMessage());
                continue;
            }
            String identifier = firstNotBlank(form.getTaxId(), form.getLegalName());

            List<String> validationErrors = validate(form);
            if (!validationErrors.isEmpty()) {
                result.fail(row.rowNumber(), identifier, String.join("; ", validationErrors));
                continue;
            }

            try {
                clientService.create(form);
                result.imported++;
            } catch (DataIntegrityViolationException ex) {
                result.fail(row.rowNumber(), identifier, duplicateMessage(ex, "Ya existe un cliente con ese NIF/CIF/NIE"));
            } catch (RuntimeException ex) {
                result.fail(row.rowNumber(), identifier, "No se ha podido crear el cliente");
            }
        }
        return result.finish();
    }

    public ImportResult importProducts(MultipartFile file) {
        TabularData data = fileReader.read(file);
        Map<String, Integer> columns = mapColumns(data.headers(), PRODUCT_HEADER_ALIASES);
        requireColumns(columns, Map.of(
                NAME, "nombre",
                UNIT_PRICE, "precio"
        ));

        ImportAccumulator result = new ImportAccumulator();
        for (TabularRow row : data.rows()) {
            result.processed++;
            ProductForm form;
            try {
                form = productForm(row, columns);
            } catch (IllegalArgumentException ex) {
                result.fail(row.rowNumber(), value(row, columns, SKU), ex.getMessage());
                continue;
            }

            String identifier = firstNotBlank(form.getSku(), form.getName());
            List<String> validationErrors = validate(form);
            if (!validationErrors.isEmpty()) {
                result.fail(row.rowNumber(), identifier, String.join("; ", validationErrors));
                continue;
            }

            try {
                productService.create(form);
                result.imported++;
            } catch (DataIntegrityViolationException ex) {
                result.fail(row.rowNumber(), identifier, duplicateMessage(ex, "Ya existe un producto con esa referencia"));
            } catch (RuntimeException ex) {
                result.fail(row.rowNumber(), identifier, "No se ha podido crear el producto o servicio");
            }
        }
        return result.finish();
    }

    private ClientForm clientForm(TabularRow row, Map<String, Integer> columns) {
        ClientForm form = new ClientForm();
        form.setLegalName(value(row, columns, LEGAL_NAME));
        form.setCommercialName(value(row, columns, COMMERCIAL_NAME));
        form.setTaxId(value(row, columns, TAX_ID));
        form.setEmail(value(row, columns, EMAIL));
        form.setPhone(value(row, columns, PHONE));
        form.setAddressLine(value(row, columns, ADDRESS));
        form.setCity(value(row, columns, CITY));
        form.setPostalCode(value(row, columns, POSTAL_CODE));
        form.setProvince(value(row, columns, PROVINCE));
        form.setCountry(defaultIfBlank(value(row, columns, COUNTRY), "España"));
        form.setClientType(parseClientType(value(row, columns, CLIENT_TYPE)));
        form.setNotes(value(row, columns, NOTES));
        return form;
    }

    private ProductForm productForm(TabularRow row, Map<String, Integer> columns) {
        ProductForm form = new ProductForm();
        form.setName(value(row, columns, NAME));
        form.setDescription(value(row, columns, DESCRIPTION));
        form.setSku(value(row, columns, SKU));
        form.setProductType(parseProductType(value(row, columns, PRODUCT_TYPE)));
        form.setUnitPrice(parseDecimal(value(row, columns, UNIT_PRICE), "precio"));

        String vat = value(row, columns, VAT_RATE);
        form.setVatRate(vat.isBlank() ? new BigDecimal("21.00") : parseDecimal(vat, "IVA"));
        return form;
    }

    private Map<String, Integer> mapColumns(List<String> headers, Map<String, String> aliases) {
        Map<String, Integer> columns = new LinkedHashMap<>();
        for (int index = 0; index < headers.size(); index++) {
            String normalized = normalize(headers.get(index));
            if (normalized.isBlank()) {
                continue;
            }
            String canonical = aliases.get(normalized);
            if (canonical == null) {
                continue;
            }
            if (columns.putIfAbsent(canonical, index) != null) {
                throw new ImportFileException("La columna '" + headers.get(index) + "' está repetida o equivale a otra columna del archivo.");
            }
        }
        return columns;
    }

    private void requireColumns(Map<String, Integer> columns, Map<String, String> required) {
        List<String> missing = required.entrySet().stream()
                .filter(entry -> !columns.containsKey(entry.getKey()))
                .map(Map.Entry::getValue)
                .sorted()
                .toList();
        if (!missing.isEmpty()) {
            throw new ImportFileException("Faltan columnas obligatorias: " + String.join(", ", missing) + ". Descarga la plantilla para ver el formato correcto.");
        }
    }

    private String value(TabularRow row, Map<String, Integer> columns, String key) {
        Integer index = columns.get(key);
        if (index == null || index >= row.cells().size()) {
            return "";
        }
        String value = row.cells().get(index);
        return value == null ? "" : value.trim();
    }

    private ClientType parseClientType(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return ClientType.COMPANY;
        }
        return switch (normalized) {
            case "empresa", "company", "sociedad" -> ClientType.COMPANY;
            case "autonomo", "autonoma", "autonomo_a", "self_employed", "selfemployed" -> ClientType.SELF_EMPLOYED;
            case "particular", "individual", "persona" -> ClientType.INDIVIDUAL;
            case "administracion_publica", "entidad_publica", "public_entity", "publicentity" -> ClientType.PUBLIC_ENTITY;
            default -> throw new IllegalArgumentException("Tipo de cliente no reconocido: '" + value + "'. Usa Empresa, Autónomo, Particular o Administración pública");
        };
    }

    private ProductType parseProductType(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return ProductType.PRODUCT;
        }
        return switch (normalized) {
            case "producto", "product", "p" -> ProductType.PRODUCT;
            case "servicio", "service", "s" -> ProductType.SERVICE;
            default -> throw new IllegalArgumentException("Tipo no reconocido: '" + value + "'. Usa Producto o Servicio");
        };
    }

    private BigDecimal parseDecimal(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El campo " + fieldName + " es obligatorio");
        }

        String normalized = value.trim()
                .replace("€", "")
                .replace("%", "")
                .replace("\u00A0", "")
                .replace(" ", "")
                .replace("'", "");

        int lastComma = normalized.lastIndexOf(',');
        int lastDot = normalized.lastIndexOf('.');
        if (lastComma >= 0 && lastDot >= 0) {
            if (lastComma > lastDot) {
                normalized = normalized.replace(".", "").replace(',', '.');
            } else {
                normalized = normalized.replace(",", "");
            }
        } else if (lastComma >= 0) {
            normalized = normalizeRepeatedSeparator(normalized, ',').replace(',', '.');
        } else if (lastDot >= 0) {
            normalized = normalizeRepeatedSeparator(normalized, '.');
        }

        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El campo " + fieldName + " no es un número válido: '" + value + "'");
        }
    }

    private String normalizeRepeatedSeparator(String value, char separator) {
        int first = value.indexOf(separator);
        int last = value.lastIndexOf(separator);
        int digitsAfterLast = value.length() - last - 1;

        if (first == last) {
            if (digitsAfterLast == 3 && first > 0) {
                return value.substring(0, first) + value.substring(first + 1);
            }
            return value;
        }

        boolean groupedThousands = digitsAfterLast == 3;
        StringBuilder result = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current != separator || (!groupedThousands && index == last)) {
                result.append(current);
            }
        }
        return result.toString();
    }

    private <T> List<String> validate(T form) {
        return validator.validate(form).stream()
                .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
                .map(ConstraintViolation::getMessage)
                .distinct()
                .toList();
    }

    private String duplicateMessage(DataIntegrityViolationException ex, String fallback) {
        String message = ex.getMessage();
        if (message != null && message.startsWith("Ya existe")) {
            return message;
        }
        return fallback;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String firstNotBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        return second == null ? "" : second.trim();
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return withoutAccents
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    @SafeVarargs
    private static Map<String, String> aliases(Map<String, String>... groups) {
        return Arrays.stream(groups)
                .flatMap(group -> group.entrySet().stream())
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<String, String> alias(String canonical, String... values) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String value : values) {
            result.put(normalize(value), canonical);
        }
        return result;
    }

    private static final class ImportAccumulator {
        private int processed;
        private int imported;
        private int failed;
        private int omitted;
        private final List<ImportRowError> errors = new ArrayList<>();

        void fail(int rowNumber, String identifier, String message) {
            failed++;
            if (errors.size() < MAX_REPORTED_ERRORS) {
                errors.add(new ImportRowError(
                        rowNumber,
                        identifier == null || identifier.isBlank() ? "—" : identifier,
                        message
                ));
            } else {
                omitted++;
            }
        }

        ImportResult finish() {
            return new ImportResult(processed, imported, failed, errors, omitted);
        }
    }
}
