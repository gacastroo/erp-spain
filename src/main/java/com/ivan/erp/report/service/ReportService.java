package com.ivan.erp.report.service;

import com.ivan.erp.expense.Expense;
import com.ivan.erp.expense.ExpenseRepository;
import com.ivan.erp.invoice.Invoice;
import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.invoice.InvoiceStatus;
import com.ivan.erp.payment.Payment;
import com.ivan.erp.payment.PaymentRepository;
import com.ivan.erp.report.ReportData;
import com.ivan.erp.report.SalesClientRow;
import com.ivan.erp.report.SalesProductRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ReportService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;

    public ReportService(
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            ExpenseRepository expenseRepository
    ) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public ReportData buildSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDate normalizedStart = normalizeStart(startDate);
        LocalDate normalizedEnd = normalizeEnd(endDate, normalizedStart);

        List<InvoiceStatus> excludedStatuses = List.of(InvoiceStatus.DRAFT, InvoiceStatus.CANCELLED);
        List<Invoice> invoices = invoiceRepository.findByIssueDateBetweenAndStatusNotInOrderByIssueDateDescIdDesc(
                normalizedStart,
                normalizedEnd,
                excludedStatuses
        );
        List<Payment> payments = paymentRepository.findByPaymentDateBetweenOrderByPaymentDateDescIdDesc(
                normalizedStart,
                normalizedEnd
        );
        List<Expense> expenses = expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(
                normalizedStart,
                normalizedEnd
        );

        BigDecimal invoicedTotal = safe(invoiceRepository.sumTotalByIssueDateBetweenAndStatusNotIn(
                normalizedStart,
                normalizedEnd,
                excludedStatuses
        ));
        BigDecimal collectedTotal = safe(paymentRepository.sumAmountByPaymentDateBetween(normalizedStart, normalizedEnd));
        BigDecimal expensesTotal = safe(expenseRepository.sumTotalByExpenseDateBetween(normalizedStart, normalizedEnd));
        BigDecimal cashResult = collectedTotal.subtract(expensesTotal);
        BigDecimal pendingEstimated = invoicedTotal.subtract(collectedTotal);
        if (pendingEstimated.signum() < 0) {
            pendingEstimated = BigDecimal.ZERO;
        }

        List<SalesClientRow> salesByClient = invoiceRepository.salesByClient(
                        normalizedStart,
                        normalizedEnd,
                        excludedStatuses
                )
                .stream()
                .map(row -> new SalesClientRow(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        (BigDecimal) row[2]
                ))
                .toList();

        List<SalesProductRow> salesByProduct = invoiceRepository.salesByProduct(
                        normalizedStart,
                        normalizedEnd,
                        excludedStatuses
                )
                .stream()
                .map(row -> new SalesProductRow(
                        (String) row[0],
                        (BigDecimal) row[1],
                        (BigDecimal) row[2]
                ))
                .toList();

        return new ReportData(
                normalizedStart,
                normalizedEnd,
                invoicedTotal,
                collectedTotal,
                expensesTotal,
                cashResult,
                pendingEstimated,
                invoices.size(),
                payments.size(),
                expenses.size(),
                invoices,
                payments,
                expenses,
                salesByClient,
                salesByProduct
        );
    }

    private LocalDate normalizeStart(LocalDate startDate) {
        if (startDate != null) {
            return startDate;
        }

        return YearMonth.now().atDay(1);
    }

    private LocalDate normalizeEnd(LocalDate endDate, LocalDate startDate) {
        if (endDate != null && !endDate.isBefore(startDate)) {
            return endDate;
        }

        return YearMonth.from(startDate).atEndOfMonth();
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
