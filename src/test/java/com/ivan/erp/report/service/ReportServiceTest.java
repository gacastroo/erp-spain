package com.ivan.erp.report.service;

import com.ivan.erp.client.Client;
import com.ivan.erp.client.ClientType;
import com.ivan.erp.expense.ExpenseRepository;
import com.ivan.erp.invoice.Invoice;
import com.ivan.erp.invoice.InvoiceLine;
import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.invoice.InvoiceStatus;
import com.ivan.erp.payment.PaymentRepository;
import com.ivan.erp.report.ReportData;
import com.ivan.erp.shared.BaseEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    private final InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
    private final ReportService service = new ReportService(invoiceRepository, paymentRepository, expenseRepository);

    @Test
    void pendingIsCalculatedFromTheInvoicesIssuedInThePeriod() throws Exception {
        Invoice invoice = invoice(10L, "100.00");
        configureCommon(List.of(invoice));
        when(invoiceRepository.sumTotalByIssueDateBetweenAndStatusNotIn(any(), any(), any())).thenReturn(new BigDecimal("100.00"));
        when(paymentRepository.sumAmountByPaymentDateBetween(any(), any())).thenReturn(new BigDecimal("80.00"));
        when(paymentRepository.sumAmountsByInvoiceIds(List.of(10L))).thenReturn(List.of());

        ReportData result = service.buildSalesReport(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        assertThat(result.pendingEstimated()).isEqualByComparingTo("100.00");
    }

    @Test
    void cashSubtractsOnlyPaidExpenses() {
        configureCommon(List.of());
        when(paymentRepository.sumAmountByPaymentDateBetween(any(), any())).thenReturn(new BigDecimal("100.00"));
        when(expenseRepository.sumTotalByExpenseDateBetween(any(), any())).thenReturn(new BigDecimal("50.00"));
        when(expenseRepository.sumPaidTotalByExpenseDateBetween(any(), any())).thenReturn(BigDecimal.ZERO);

        ReportData result = service.buildSalesReport(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        assertThat(result.expensesTotal()).isEqualByComparingTo("50.00");
        assertThat(result.paidExpensesTotal()).isZero();
        assertThat(result.cashResult()).isEqualByComparingTo("100.00");
    }

    private void configureCommon(List<Invoice> invoices) {
        when(invoiceRepository.findByIssueDateBetweenAndStatusNotInOrderByIssueDateDescIdDesc(any(), any(), any())).thenReturn(invoices);
        when(invoiceRepository.salesByClient(any(), any(), any())).thenReturn(List.of());
        when(invoiceRepository.salesByProduct(any(), any(), any())).thenReturn(List.of());
        when(invoiceRepository.sumTotalByIssueDateBetweenAndStatusNotIn(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(paymentRepository.findByPaymentDateBetweenOrderByPaymentDateDescIdDesc(any(), any())).thenReturn(List.of());
        when(paymentRepository.sumAmountByPaymentDateBetween(any(), any())).thenReturn(BigDecimal.ZERO);
        when(paymentRepository.sumAmountsByInvoiceIds(any())).thenReturn(List.of());
        when(expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(any(), any())).thenReturn(List.of());
        when(expenseRepository.sumTotalByExpenseDateBetween(any(), any())).thenReturn(BigDecimal.ZERO);
        when(expenseRepository.sumPaidTotalByExpenseDateBetween(any(), any())).thenReturn(BigDecimal.ZERO);
    }

    private Invoice invoice(Long id, String total) throws Exception {
        Client client = new Client("Cliente", "B12345678", ClientType.COMPANY);
        Invoice invoice = new Invoice("FAC-2026-000001", null, client, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), null);
        invoice.replaceLines(List.of(new InvoiceLine(null, "Servicio", BigDecimal.ONE, new BigDecimal(total), BigDecimal.ZERO)));
        invoice.changeStatus(InvoiceStatus.SENT);
        Field field = BaseEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(invoice, id);
        return invoice;
    }
}
