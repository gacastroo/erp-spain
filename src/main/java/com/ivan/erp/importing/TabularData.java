package com.ivan.erp.importing;

import java.util.List;

public record TabularData(List<String> headers, List<TabularRow> rows) {
    public TabularData {
        headers = List.copyOf(headers);
        rows = List.copyOf(rows);
    }
}
