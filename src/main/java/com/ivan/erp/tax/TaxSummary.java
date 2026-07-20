package com.ivan.erp.tax;

import com.ivan.erp.expense.Expense;
import com.ivan.erp.invoice.Invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TaxSummary(
        int year,
        int quarter,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal issuedBase,
        BigDecimal issuedVat,
        BigDecimal issuedTotal,
        BigDecimal deductibleBase,
        BigDecimal deductibleVat,
        BigDecimal deductibleTotal,
        BigDecimal vatResult,
        long issuedInvoiceCount,
        long expenseCount,
        List<TaxVatRateRow> outputVatByRate,
        List<TaxVatRateRow> inputVatByRate,
        List<Invoice> invoices,
        List<Expense> expenses
) {
    public boolean isVatToPay() {
        return vatResult != null && vatResult.signum() >= 0;
    }

    public BigDecimal vatResultAbs() {
        return vatResult != null ? vatResult.abs() : BigDecimal.ZERO;
    }
}
