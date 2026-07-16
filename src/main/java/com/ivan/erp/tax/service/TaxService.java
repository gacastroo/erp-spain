package com.ivan.erp.tax.service;

import com.ivan.erp.expense.Expense;
import com.ivan.erp.expense.ExpenseRepository;
import com.ivan.erp.invoice.Invoice;
import com.ivan.erp.invoice.InvoiceLine;
import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.invoice.InvoiceStatus;
import com.ivan.erp.tax.TaxSummary;
import com.ivan.erp.tax.TaxVatRateRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class TaxService {

    private final InvoiceRepository invoiceRepository;
    private final ExpenseRepository expenseRepository;

    public TaxService(InvoiceRepository invoiceRepository, ExpenseRepository expenseRepository) {
        this.invoiceRepository = invoiceRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public TaxSummary buildQuarterSummary(int year, int quarter) {
        int safeQuarter = Math.max(1, Math.min(4, quarter));
        LocalDate start = LocalDate.of(year, ((safeQuarter - 1) * 3) + 1, 1);
        LocalDate end = start.plusMonths(3).minusDays(1);

        List<InvoiceStatus> excludedStatuses = List.of(InvoiceStatus.DRAFT, InvoiceStatus.CANCELLED);
        List<Invoice> invoices = invoiceRepository.findFiscalInvoicesWithLines(start, end, excludedStatuses);
        List<Expense> expenses = expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(start, end);

        BigDecimal issuedBase = BigDecimal.ZERO;
        BigDecimal issuedVat = BigDecimal.ZERO;
        BigDecimal issuedTotal = BigDecimal.ZERO;
        BigDecimal deductibleBase = BigDecimal.ZERO;
        BigDecimal deductibleVat = BigDecimal.ZERO;
        BigDecimal deductibleTotal = BigDecimal.ZERO;

        Map<BigDecimal, MutableVatRow> outputRates = new TreeMap<>(Comparator.naturalOrder());
        Map<BigDecimal, MutableVatRow> inputRates = new TreeMap<>(Comparator.naturalOrder());

        for (Invoice invoice : invoices) {
            issuedBase = issuedBase.add(safe(invoice.getSubtotal()));
            issuedVat = issuedVat.add(safe(invoice.getVatTotal()));
            issuedTotal = issuedTotal.add(safe(invoice.getTotal()));

            for (InvoiceLine line : invoice.getLines()) {
                BigDecimal rate = normalizeRate(line.getVatRate());
                MutableVatRow row = outputRates.computeIfAbsent(rate, ignored -> new MutableVatRow(rate));
                row.add(line.getLineSubtotal(), line.getLineVat(), line.getLineTotal());
            }
        }

        for (Expense expense : expenses) {
            deductibleBase = deductibleBase.add(safe(expense.getBaseAmount()));
            deductibleVat = deductibleVat.add(safe(expense.getVatAmount()));
            deductibleTotal = deductibleTotal.add(safe(expense.getTotal()));

            BigDecimal rate = normalizeRate(expense.getVatRate());
            MutableVatRow row = inputRates.computeIfAbsent(rate, ignored -> new MutableVatRow(rate));
            row.add(expense.getBaseAmount(), expense.getVatAmount(), expense.getTotal());
        }

        issuedBase = money(issuedBase);
        issuedVat = money(issuedVat);
        issuedTotal = money(issuedTotal);
        deductibleBase = money(deductibleBase);
        deductibleVat = money(deductibleVat);
        deductibleTotal = money(deductibleTotal);
        BigDecimal vatResult = money(issuedVat.subtract(deductibleVat));

        return new TaxSummary(
                year,
                safeQuarter,
                start,
                end,
                issuedBase,
                issuedVat,
                issuedTotal,
                deductibleBase,
                deductibleVat,
                deductibleTotal,
                vatResult,
                invoices.size(),
                expenses.size(),
                toRows(outputRates),
                toRows(inputRates),
                invoices,
                expenses
        );
    }

    private List<TaxVatRateRow> toRows(Map<BigDecimal, MutableVatRow> rows) {
        List<TaxVatRateRow> result = new ArrayList<>();
        for (MutableVatRow row : rows.values()) {
            result.add(row.toRow());
        }
        return result;
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal money(BigDecimal value) {
        return safe(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeRate(BigDecimal value) {
        return safe(value).setScale(2, RoundingMode.HALF_UP);
    }

    private final class MutableVatRow {
        private final BigDecimal rate;
        private BigDecimal base = BigDecimal.ZERO;
        private BigDecimal vat = BigDecimal.ZERO;
        private BigDecimal total = BigDecimal.ZERO;

        private MutableVatRow(BigDecimal rate) {
            this.rate = rate;
        }

        private void add(BigDecimal base, BigDecimal vat, BigDecimal total) {
            this.base = this.base.add(safe(base));
            this.vat = this.vat.add(safe(vat));
            this.total = this.total.add(safe(total));
        }

        private TaxVatRateRow toRow() {
            return new TaxVatRateRow(rate, money(base), money(vat), money(total));
        }
    }
}
