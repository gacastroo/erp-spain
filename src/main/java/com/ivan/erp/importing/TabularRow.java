package com.ivan.erp.importing;

import java.util.List;

public record TabularRow(int rowNumber, List<String> cells) {
    public TabularRow {
        cells = List.copyOf(cells);
    }
}
