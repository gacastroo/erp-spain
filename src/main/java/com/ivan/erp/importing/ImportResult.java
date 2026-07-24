package com.ivan.erp.importing;

import java.util.List;

public record ImportResult(
        int processedRows,
        int importedRows,
        int failedRows,
        List<ImportRowError> errors,
        int omittedErrors
) {
    public ImportResult {
        errors = List.copyOf(errors);
    }

    public boolean hasErrors() {
        return failedRows > 0;
    }

    public boolean hasImportedRows() {
        return importedRows > 0;
    }
}
