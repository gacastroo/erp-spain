package com.ivan.erp.importing;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ImportTemplateService {

    private static final List<String> CLIENT_HEADERS = List.of(
            "nombre_fiscal", "nombre_comercial", "nif_cif_nie", "email", "telefono",
            "direccion", "ciudad", "codigo_postal", "provincia", "pais", "tipo_cliente", "observaciones"
    );

    private static final List<String> PRODUCT_HEADERS = List.of(
            "nombre", "descripcion", "sku", "tipo", "precio", "iva"
    );

    public byte[] clientTemplate(String format) {
        return template(CLIENT_HEADERS, format, "Clientes");
    }

    public byte[] productTemplate(String format) {
        return template(PRODUCT_HEADERS, format, "Productos");
    }

    private byte[] template(List<String> headers, String format, String sheetName) {
        if ("csv".equalsIgnoreCase(format)) {
            return csv(headers);
        }
        if (format == null || format.isBlank() || "xlsx".equalsIgnoreCase(format)) {
            return xlsx(headers, sheetName);
        }
        throw new ImportFileException("Formato de plantilla no admitido.");
    }

    private byte[] csv(List<String> headers) {
        String content = "\uFEFF" + String.join(";", headers) + System.lineSeparator();
        return content.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] xlsx(List<String> headers, String sheetName) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            Row row = sheet.createRow(0);

            Font font = workbook.createFont();
            font.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setDataFormat(workbook.createDataFormat().getFormat("@"));

            for (int index = 0; index < headers.size(); index++) {
                Cell cell = row.createCell(index);
                cell.setCellValue(headers.get(index));
                cell.setCellStyle(headerStyle);
                sheet.setDefaultColumnStyle(index, textStyle);
                sheet.setColumnWidth(index, Math.min(40, Math.max(16, headers.get(index).length() + 4)) * 256);
            }

            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, headers.size() - 1));
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se ha podido generar la plantilla Excel", ex);
        }
    }
}
