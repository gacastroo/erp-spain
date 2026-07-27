package com.ivan.erp.importing;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class ImportErrorExportService {

    public byte[] toCsv(ImportResult result) {
        StringBuilder csv = new StringBuilder("\uFEFFfila;registro;motivo\r\n");
        for (ImportRowError error : result.errors()) {
            csv.append(error.rowNumber()).append(';')
                    .append(escape(error.identifier())).append(';')
                    .append(escape(error.message())).append("\r\n");
        }
        if (result.omittedErrors() > 0) {
            csv.append(";;")
                    .append(escape("Hay " + result.omittedErrors() + " errores adicionales no incluidos en este archivo."))
                    .append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        String safe = value == null ? "" : value;
        if (!safe.isEmpty() && "=+-@".indexOf(safe.charAt(0)) >= 0) {
            safe = "'" + safe;
        }
        return '"' + safe.replace("\"", "\"\"") + '"';
    }
}
