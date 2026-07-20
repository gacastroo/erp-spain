package com.ivan.erp.payment.service;

import com.ivan.erp.client.Client;
import com.ivan.erp.client.ClientType;
import com.ivan.erp.invoice.Invoice;
import com.ivan.erp.invoice.InvoiceLine;
import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.invoice.InvoiceStatus;
import com.ivan.erp.payment.Payment;
import com.ivan.erp.payment.PaymentMethod;
import com.ivan.erp.payment.PaymentRepository;
import com.ivan.erp.payment.web.PaymentForm;
import com.ivan.erp.shared.BaseEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
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

class PaymentServiceTest {

    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
    private final PaymentService service = new PaymentService(paymentRepository, invoiceRepository);

    @Test
    void rejectsFuturePaymentDatesBeforeWriting() {
        PaymentForm form = form(1L, "10.00", LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> service.create(form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("futuro");
        verify(invoiceRepository, never()).findByIdForUpdate(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void partialPaymentPreservesIssuedStatus() throws Exception {
        Invoice invoice = invoice(1L, "100.00", InvoiceStatus.ISSUED);
        when(invoiceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.sumAmountByInvoiceId(1L))
                .thenReturn(BigDecimal.ZERO, new BigDecimal("20.00"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(form(1L, "20.00", LocalDate.now()));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(invoice.getPaidAt()).isNull();
        verify(invoiceRepository).findByIdForUpdate(1L);
    }

    @Test
    void fullPaymentUsesTheLatestPersistedPaymentDate() throws Exception {
        Invoice invoice = invoice(2L, "100.00", InvoiceStatus.SENT);
        LocalDate latestDate = LocalDate.now().minusDays(1);
        when(invoiceRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.sumAmountByInvoiceId(2L))
                .thenReturn(new BigDecimal("60.00"), new BigDecimal("100.00"));
        when(paymentRepository.findLatestPaymentDateByInvoiceId(2L)).thenReturn(latestDate);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(form(2L, "40.00", LocalDate.now().minusDays(5)));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getPaidAt()).isEqualTo(latestDate);
    }

    @Test
    void rejectsAnAmountAboveTheLockedOutstandingBalance() throws Exception {
        Invoice invoice = invoice(3L, "100.00", InvoiceStatus.ISSUED);
        when(invoiceRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.sumAmountByInvoiceId(3L)).thenReturn(new BigDecimal("80.00"));

        assertThatThrownBy(() -> service.create(form(3L, "30.00", LocalDate.now())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("supera el pendiente");
        verify(paymentRepository, never()).save(any());
    }

    private PaymentForm form(Long invoiceId, String amount, LocalDate date) {
        PaymentForm form = new PaymentForm();
        form.setInvoiceId(invoiceId);
        form.setAmount(new BigDecimal(amount));
        form.setPaymentDate(date);
        form.setMethod(PaymentMethod.TRANSFER);
        return form;
    }

    private Invoice invoice(Long id, String total, InvoiceStatus status) throws Exception {
        Client client = new Client("Cliente", "B12345678", ClientType.COMPANY);
        Invoice invoice = new Invoice("FAC-2026-000001", null, client, LocalDate.now(), LocalDate.now().plusDays(30), null);
        invoice.replaceLines(List.of(new InvoiceLine(null, "Servicio", BigDecimal.ONE, new BigDecimal(total), BigDecimal.ZERO)));
        invoice.changeStatus(status);
        Field field = BaseEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(invoice, id);
        return invoice;
    }
}
