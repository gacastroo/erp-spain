package com.ivan.erp.invoice.service;

import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.invoice.InvoiceStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@ConditionalOnProperty(prefix = "app.invoices.overdue-update", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InvoiceOverdueService {

    private static final List<InvoiceStatus> ELIGIBLE_STATUSES = List.of(
            InvoiceStatus.ISSUED,
            InvoiceStatus.SENT
    );

    private final InvoiceRepository invoiceRepository;
    private final Clock clock;

    public InvoiceOverdueService(InvoiceRepository invoiceRepository) {
        this(invoiceRepository, Clock.system(ZoneId.of("Europe/Madrid")));
    }

    InvoiceOverdueService(InvoiceRepository invoiceRepository, Clock clock) {
        this.invoiceRepository = invoiceRepository;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        markOverdueInvoices();
    }

    @Scheduled(cron = "${app.invoices.overdue-update.cron:0 5 0 * * *}", zone = "Europe/Madrid")
    @Transactional
    public int markOverdueInvoices() {
        return invoiceRepository.markOverdueInvoices(LocalDate.now(clock), ELIGIBLE_STATUSES);
    }
}
