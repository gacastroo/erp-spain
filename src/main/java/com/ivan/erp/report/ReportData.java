package com.ivan.erp.report;

import com.ivan.erp.expense.Expense;
import com.ivan.erp.invoice.Invoice;
import com.ivan.erp.payment.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportData(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal invoicedTotal,
        BigDecimal collectedTotal,
        BigDecimal expensesTotal,
        BigDecimal paidExpensesTotal,
        BigDecimal cashResult,
        BigDecimal pendingEstimated,
        long invoiceCount,
        long paymentCount,
        long expenseCount,
        List<Invoice> invoices,
        List<Payment> payments,
        List<Expense> expenses,
        List<SalesClientRow> salesByClient,
        List<SalesProductRow> salesByProduct
) {
}
