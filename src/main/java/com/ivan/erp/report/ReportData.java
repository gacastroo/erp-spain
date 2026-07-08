package com.ivan.erp.report;

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
        BigDecimal pendingEstimated,
        long invoiceCount,
        long paymentCount,
        List<Invoice> invoices,
        List<Payment> payments,
        List<SalesClientRow> salesByClient,
        List<SalesProductRow> salesByProduct
) {
}
