package com.ivan.erp.invoice.service;

import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.invoice.InvoiceStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvoiceOverdueServiceTest {

    @Test
    void marksIssuedAndSentInvoicesUsingMadridDate() {
        InvoiceRepository repository = mock(InvoiceRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-07-19T22:30:00Z"), ZoneId.of("Europe/Madrid"));
        when(repository.markOverdueInvoices(any(), any())).thenReturn(4);

        int updated = new InvoiceOverdueService(repository, clock).markOverdueInvoices();

        ArgumentCaptor<LocalDate> date = ArgumentCaptor.forClass(LocalDate.class);
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<Collection> statuses = ArgumentCaptor.forClass(Collection.class);
        verify(repository).markOverdueInvoices(date.capture(), statuses.capture());
        assertThat(updated).isEqualTo(4);
        assertThat(date.getValue()).isEqualTo(LocalDate.of(2026, 7, 20));
        assertThat(statuses.getValue()).containsExactlyInAnyOrder(InvoiceStatus.ISSUED, InvoiceStatus.SENT);
    }
}
