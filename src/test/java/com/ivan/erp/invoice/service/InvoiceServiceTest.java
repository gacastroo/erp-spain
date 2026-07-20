package com.ivan.erp.invoice.service;

import com.ivan.erp.client.Client;
import com.ivan.erp.client.ClientType;
import com.ivan.erp.document.service.DocumentNumberService;
import com.ivan.erp.invoice.Invoice;
import com.ivan.erp.invoice.InvoiceLine;
import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.invoice.InvoiceStatus;
import com.ivan.erp.payment.PaymentRepository;
import com.ivan.erp.quote.Quote;
import com.ivan.erp.quote.QuoteLine;
import com.ivan.erp.quote.QuoteRepository;
import com.ivan.erp.quote.QuoteStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvoiceServiceTest {

    private final InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
    private final QuoteRepository quoteRepository = mock(QuoteRepository.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final DocumentNumberService numberService = mock(DocumentNumberService.class);
    private final InvoiceService service = new InvoiceService(invoiceRepository, quoteRepository, paymentRepository, numberService);

    @Test
    void rejectedQuoteCannotBeInvoiced() {
        Quote quote = quote(QuoteStatus.REJECTED);
        when(quoteRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(quote));

        assertThatThrownBy(() -> service.createFromQuote(7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("aceptados");
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void acceptedQuoteCanBeInvoicedOnce() {
        Quote quote = quote(QuoteStatus.ACCEPTED);
        when(quoteRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(quote));
        when(invoiceRepository.findByQuote_Id(7L)).thenReturn(Optional.empty());
        when(quoteRepository.findByIdWithClientAndLines(7L)).thenReturn(Optional.of(quote));
        when(numberService.nextInvoiceNumber(any())).thenReturn("FAC-2026-000001");
        when(numberService.defaultInvoiceDueDate(any())).thenReturn(LocalDate.now().plusDays(30));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice invoice = service.createFromQuote(7L);

        assertThat(invoice.getQuote()).isSameAs(quote);
        assertThat(invoice.getTotal()).isEqualByComparingTo(quote.getTotal());
    }

    @Test
    void fullyPaidInvoiceCannotReturnToPendingState() {
        Invoice invoice = invoice(InvoiceStatus.PAID);
        invoice.markPaid(LocalDate.now().minusDays(1));
        when(invoiceRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.sumAmountByInvoiceId(8L)).thenReturn(invoice.getTotal());

        assertThatThrownBy(() -> service.changeStatus(8L, InvoiceStatus.SENT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("completamente cobrada");
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getPaidAt()).isNotNull();
    }

    @Test
    void paidStateCannotBeSelectedManually() {
        assertThatThrownBy(() -> service.changeStatus(8L, InvoiceStatus.PAID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("registra un cobro");
        verify(invoiceRepository, never()).findByIdForUpdate(any());
    }

    private Quote quote(QuoteStatus status) {
        Client client = new Client("Cliente", "B12345678", ClientType.COMPANY);
        Quote quote = new Quote("PRE-2026-000001", client, LocalDate.now(), LocalDate.now().plusDays(30), null);
        quote.replaceLines(List.of(new QuoteLine(null, "Servicio", BigDecimal.ONE, new BigDecimal("100"), BigDecimal.ZERO)));
        quote.changeStatus(status);
        return quote;
    }

    private Invoice invoice(InvoiceStatus status) {
        Client client = new Client("Cliente", "B12345678", ClientType.COMPANY);
        Invoice invoice = new Invoice("FAC-2026-000001", null, client, LocalDate.now(), LocalDate.now().plusDays(30), null);
        invoice.replaceLines(List.of(new InvoiceLine(null, "Servicio", BigDecimal.ONE, new BigDecimal("100"), BigDecimal.ZERO)));
        invoice.changeStatus(status);
        return invoice;
    }
}
