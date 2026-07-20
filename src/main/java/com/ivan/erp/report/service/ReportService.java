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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                normalizedStart, normalizedEnd, excludedStatuses
        );
        List<Payment> payments = paymentRepository.findByPaymentDateBetweenOrderByPaymentDateDescIdDesc(
                normalizedStart, normalizedEnd
        );
        List<Expense> expenses = expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(
                normalizedStart, normalizedEnd
        );

        BigDecimal invoicedTotal = safe(invoiceRepository.sumTotalByIssueDateBetweenAndStatusNotIn(
                normalizedStart, normalizedEnd, excludedStatuses
        ));
        BigDecimal collectedTotal = safe(paymentRepository.sumAmountByPaymentDateBetween(normalizedStart, normalizedEnd));
        BigDecimal expensesTotal = safe(expenseRepository.sumTotalByExpenseDateBetween(normalizedStart, normalizedEnd));
        BigDecimal paidExpensesTotal = safe(expenseRepository.sumPaidTotalByExpenseDateBetween(normalizedStart, normalizedEnd));
        BigDecimal cashResult = collectedTotal.subtract(paidExpensesTotal);
        BigDecimal pendingEstimated = calculateOutstandingForInvoiceCohort(invoices);

        List<SalesClientRow> salesByClient = invoiceRepository.salesByClient(
                        normalizedStart, normalizedEnd, excludedStatuses
                ).stream()
                .map(row -> new SalesClientRow((String) row[0], ((Number) row[1]).longValue(), (BigDecimal) row[2]))
                .toList();

        List<SalesProductRow> salesByProduct = invoiceRepository.salesByProduct(
                        normalizedStart, normalizedEnd, excludedStatuses
                ).stream()
                .map(row -> new SalesProductRow((String) row[0], (BigDecimal) row[1], (BigDecimal) row[2]))
                .toList();

        return new ReportData(
                normalizedStart,
                normalizedEnd,
                invoicedTotal,
                collectedTotal,
                expensesTotal,
                paidExpensesTotal,
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

    private BigDecimal calculateOutstandingForInvoiceCohort(List<Invoice> invoices) {
        if (invoices.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<Long> invoiceIds = invoices.stream().map(Invoice::getId).toList();
        Map<Long, BigDecimal> paidByInvoice = new HashMap<>();
        for (Object[] row : paymentRepository.sumAmountsByInvoiceIds(invoiceIds)) {
            paidByInvoice.put(((Number) row[0]).longValue(), safe((BigDecimal) row[1]));
        }

        return invoices.stream()
                .map(invoice -> {
                    BigDecimal paid = paidByInvoice.getOrDefault(invoice.getId(), BigDecimal.ZERO);
                    BigDecimal outstanding = invoice.getTotal().subtract(paid);
                    return outstanding.signum() > 0 ? outstanding : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LocalDate normalizeStart(LocalDate startDate) {
        return startDate != null ? startDate : YearMonth.now().atDay(1);
    }

    private LocalDate normalizeEnd(LocalDate endDate, LocalDate startDate) {
        return endDate != null && !endDate.isBefore(startDate)
                ? endDate
                : YearMonth.from(startDate).atEndOfMonth();
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
