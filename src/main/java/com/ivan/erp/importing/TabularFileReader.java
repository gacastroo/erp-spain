package com.ivan.erp.importing;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class TabularFileReader {

    static final int MAX_FILE_BYTES = 5 * 1024 * 1024;
    static final int MAX_DATA_ROWS = 5_000;
    private static final int MAX_COLUMNS = 100;

    public TabularData read(MultipartFile file) {
        validateFile(file);

        String filename = file.getOriginalFilename() == null
                ? ""
                : file.getOriginalFilename().toLowerCase(Locale.ROOT).trim();

        try {
            byte[] content = file.getBytes();
            if (filename.endsWith(".csv")) {
                return readCsv(content);
            }
            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                return readExcel(content);
            }
        } catch (ImportFileException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ImportFileException("No se ha podido leer el archivo. Comprueba que no esté dañado o protegido.", ex);
        }

        throw new ImportFileException("Formato no admitido. Usa un archivo CSV, XLSX o XLS.");
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImportFileException("Selecciona un archivo para importar.");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new ImportFileException("El archivo supera el límite de 5 MB.");
        }
    }

    private TabularData readExcel(byte[] content) {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new ImportFileException("El archivo Excel no contiene ninguna hoja.");
            }

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(new Locale("es", "ES"));
            List<String> headers = null;
            List<TabularRow> rows = new ArrayList<>();

            for (Row row : sheet) {
                List<String> cells = excelCells(row, formatter);
                if (isBlankRow(cells)) {
                    continue;
                }

                if (headers == null) {
                    headers = cells;
                    validateColumnCount(headers.size());
                    continue;
                }

                if (rows.size() >= MAX_DATA_ROWS) {
                    throw new ImportFileException("El archivo contiene más de 5.000 filas de datos.");
                }
                rows.add(new TabularRow(row.getRowNum() + 1, pad(cells, headers.size())));
            }

            return finish(headers, rows);
        } catch (ImportFileException ex) {
            throw ex;
        } catch (IOException | RuntimeException ex) {
            throw new ImportFileException("No se ha podido abrir el archivo Excel. Comprueba que sea válido y no esté protegido.", ex);
        }
    }

    private List<String> excelCells(Row row, DataFormatter formatter) {
        int lastCell = Math.max(row.getLastCellNum(), 0);
        validateColumnCount(lastCell);
        List<String> cells = new ArrayList<>(lastCell);
        for (int index = 0; index < lastCell; index++) {
            Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            cells.add(cellValue(cell, formatter));
        }
        return cells;
    }

    private String cellValue(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() != CellType.FORMULA) {
            return formatter.formatCellValue(cell).trim();
        }

        return switch (cell.getCachedFormulaResultType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> formatter.formatRawCellContents(
                    cell.getNumericCellValue(),
                    cell.getCellStyle().getDataFormat(),
                    cell.getCellStyle().getDataFormatString()
            ).trim();
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case ERROR, BLANK, _NONE, FORMULA -> "";
        };
    }

    private TabularData readCsv(byte[] content) {
        String text = decodeCsv(content);
        char delimiter = detectDelimiter(text);
        List<CsvRecord> records = parseCsv(text, delimiter);

        List<String> headers = null;
        List<TabularRow> rows = new ArrayList<>();
        for (CsvRecord record : records) {
            if (isBlankRow(record.cells())) {
                continue;
            }
            if (headers == null) {
                headers = record.cells();
                validateColumnCount(headers.size());
                continue;
            }
            if (rows.size() >= MAX_DATA_ROWS) {
                throw new ImportFileException("El archivo contiene más de 5.000 filas de datos.");
            }
            validateColumnCount(record.cells().size());
            rows.add(new TabularRow(record.startLine(), pad(record.cells(), headers.size())));
        }

        return finish(headers, rows);
    }

    private String decodeCsv(byte[] content) {
        CharsetDecoder utf8 = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            String text = utf8.decode(ByteBuffer.wrap(content)).toString();
            return stripBom(text);
        } catch (CharacterCodingException ignored) {
            return stripBom(Charset.forName("windows-1252").decode(ByteBuffer.wrap(content)).toString());
        }
    }

    private String stripBom(String text) {
        return text.startsWith("\uFEFF") ? text.substring(1) : text;
    }

    private char detectDelimiter(String text) {
        String firstLine = text.lines().filter(line -> !line.isBlank()).findFirst().orElse("");
        int semicolons = countOutsideQuotes(firstLine, ';');
        int commas = countOutsideQuotes(firstLine, ',');
        int tabs = countOutsideQuotes(firstLine, '\t');

        if (tabs > semicolons && tabs > commas) {
            return '\t';
        }
        return semicolons >= commas ? ';' : ',';
    }

    private int countOutsideQuotes(String value, char target) {
        int count = 0;
        boolean quoted = false;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current == '"') {
                if (quoted && i + 1 < value.length() && value.charAt(i + 1) == '"') {
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (!quoted && current == target) {
                count++;
            }
        }
        return count;
    }

    private List<CsvRecord> parseCsv(String text, char delimiter) {
        List<CsvRecord> records = new ArrayList<>();
        List<String> cells = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        int line = 1;
        int recordStartLine = 1;

        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == '"') {
                if (quoted && i + 1 < text.length() && text.charAt(i + 1) == '"') {
                    cell.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
                continue;
            }

            if (!quoted && current == delimiter) {
                cells.add(cell.toString().trim());
                cell.setLength(0);
                continue;
            }

            if (current == '\r' || current == '\n') {
                boolean crlf = current == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n';
                if (quoted) {
                    cell.append('\n');
                } else {
                    cells.add(cell.toString().trim());
                    cell.setLength(0);
                    records.add(new CsvRecord(recordStartLine, List.copyOf(cells)));
                    cells.clear();
                    recordStartLine = line + 1;
                }
                if (crlf) {
                    i++;
                }
                line++;
                continue;
            }

            cell.append(current);
        }

        if (quoted) {
            throw new ImportFileException("El CSV tiene una comilla sin cerrar cerca de la línea " + recordStartLine + ".");
        }
        if (cell.length() > 0 || !cells.isEmpty()) {
            cells.add(cell.toString().trim());
            records.add(new CsvRecord(recordStartLine, List.copyOf(cells)));
        }
        return records;
    }

    private TabularData finish(List<String> headers, List<TabularRow> rows) {
        if (headers == null || isBlankRow(headers)) {
            throw new ImportFileException("El archivo está vacío o no contiene una fila de encabezados.");
        }
        if (rows.isEmpty()) {
            throw new ImportFileException("El archivo no contiene filas de datos para importar.");
        }
        return new TabularData(headers, rows);
    }

    private List<String> pad(List<String> cells, int size) {
        List<String> result = new ArrayList<>(Math.max(cells.size(), size));
        result.addAll(cells);
        while (result.size() < size) {
            result.add("");
        }
        return result;
    }

    private boolean isBlankRow(List<String> cells) {
        return cells.stream().allMatch(value -> value == null || value.isBlank());
    }

    private void validateColumnCount(int count) {
        if (count > MAX_COLUMNS) {
            throw new ImportFileException("El archivo supera el límite de 100 columnas.");
        }
    }

    private record CsvRecord(int startLine, List<String> cells) {
    }
}
