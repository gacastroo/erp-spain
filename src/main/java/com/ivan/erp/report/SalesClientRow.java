package com.ivan.erp.report;

import java.math.BigDecimal;

public record SalesClientRow(String clientName, long invoiceCount, BigDecimal total) {
}
