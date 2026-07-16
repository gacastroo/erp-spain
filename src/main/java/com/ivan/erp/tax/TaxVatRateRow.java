package com.ivan.erp.tax;

import java.math.BigDecimal;

public record TaxVatRateRow(
        BigDecimal vatRate,
        BigDecimal taxableBase,
        BigDecimal vatAmount,
        BigDecimal total
) {
}
