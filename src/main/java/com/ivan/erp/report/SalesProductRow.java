package com.ivan.erp.report;

import java.math.BigDecimal;

public record SalesProductRow(String description, BigDecimal quantity, BigDecimal total) {
}
