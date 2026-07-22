package com.ivan.erp.invoice.service;

import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.invoice.InvoiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
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
@ConditionalOnProperty(
        prefix = "app.invoices.overdue-update",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class InvoiceOverdueService {

    private static final List<InvoiceStatus> ELIGIBLE_STATUSES = List.of(
            InvoiceStatus.ISSUED,
            InvoiceStatus.SENT
    );

    private final InvoiceRepository invoiceRepository;
    private final Clock clock;

    /**
     * Constructor utilizado por Spring en la aplicación.
     */
    @Autowired
    public InvoiceOverdueService(InvoiceRepository invoiceRepository) {
        this(
                invoiceRepository,
                Clock.system(ZoneId.of("Europe/Madrid"))
        );
    }

    /**
     * Constructor interno utilizado por las pruebas para controlar la fecha.
     */
    InvoiceOverdueService(
            InvoiceRepository invoiceRepository,
            Clock clock
    ) {
        if (invoiceRepository == null) {
            throw new IllegalArgumentException(
                    "InvoiceRepository no puede ser null"
            );
        }

        if (clock == null) {
            throw new IllegalArgumentException(
                    "Clock no puede ser null"
            );
        }

        this.invoiceRepository = invoiceRepository;
        this.clock = clock;
    }

    /**
     * Actualiza las facturas vencidas cuando la aplicación termina de arrancar.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        markOverdueInvoices();
    }

    /**
     * Ejecuta diariamente la actualización de facturas vencidas.
     */
    @Scheduled(
            cron = "${app.invoices.overdue-update.cron:0 5 0 * * *}",
            zone = "Europe/Madrid"
    )
    @Transactional
    public int markOverdueInvoices() {
        LocalDate today = LocalDate.now(clock);

        return invoiceRepository.markOverdueInvoices(
                today,
                ELIGIBLE_STATUSES
        );
    }
}