package com.ivan.erp.importing;

import java.util.List;

public record ImportPreview(
        String filename,
        int totalRows,
        List<String> headers,
        List<String> recognizedHeaders,
        List<String> ignoredHeaders,
        List<TabularRow> sampleRows
) {
    public ImportPreview {
        headers = List.copyOf(headers);
        recognizedHeaders = List.copyOf(recognizedHeaders);
        ignoredHeaders = List.copyOf(ignoredHeaders);
        sampleRows = List.copyOf(sampleRows);
    }

    public boolean hasIgnoredHeaders() {
        return !ignoredHeaders.isEmpty();
    }
}
