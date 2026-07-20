package com.ivan.erp.invoice.service;

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
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {

    private static final int PAGE_SIZE = 10;

    private final InvoiceRepository invoiceRepository;
    private final QuoteRepository quoteRepository;
    private final PaymentRepository paymentRepository;
    private final DocumentNumberService documentNumberService;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            QuoteRepository quoteRepository,
            PaymentRepository paymentRepository,
            DocumentNumberService documentNumberService
    ) {
        this.invoiceRepository = invoiceRepository;
        this.quoteRepository = quoteRepository;
        this.paymentRepository = paymentRepository;
        this.documentNumberService = documentNumberService;
    }

    @Transactional(readOnly = true)
    public Page<Invoice> search(String query, int page) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "issueDate").and(Sort.by(Sort.Direction.DESC, "id"))
        );
        return invoiceRepository.search(normalizeQuery(query), pageable);
    }

    @Transactional(readOnly = true)
    public Invoice getById(Long id) {
        return invoiceRepository.findByIdWithClientQuoteAndLines(id)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));
    }

    @Transactional
    public Invoice createFromQuote(Long quoteId) {
        Quote lockedQuote = quoteRepository.findByIdForUpdate(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Presupuesto no encontrado"));

        if (lockedQuote.getStatus() != QuoteStatus.ACCEPTED) {
            throw new IllegalStateException("Solo se pueden facturar presupuestos aceptados");
        }

        return invoiceRepository.findByQuote_Id(quoteId)
                .orElseGet(() -> buildInvoiceFromAcceptedQuote(quoteId));
    }

    @Transactional
    public void changeStatus(Long id, InvoiceStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado de la factura es obligatorio");
        }
        if (status == InvoiceStatus.PAID) {
            throw new IllegalStateException("Para marcar una factura como cobrada, registra un cobro desde el módulo de Cobros.");
        }

        Invoice invoice = invoiceRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));
        BigDecimal paidAmount = paymentRepository.sumAmountByInvoiceId(id);
        paidAmount = paidAmount != null ? paidAmount : BigDecimal.ZERO;

        if (paidAmount.compareTo(invoice.getTotal()) >= 0) {
            throw new IllegalStateException("Una factura completamente cobrada no puede cambiar a un estado pendiente.");
        }
        if (paidAmount.signum() > 0 && (status == InvoiceStatus.DRAFT || status == InvoiceStatus.CANCELLED)) {
            throw new IllegalStateException("No puedes cambiar a ese estado una factura que ya tiene cobros registrados.");
        }

        invoice.changeStatus(status);
    }

    @Transactional
    public void delete(Long id) {
        Invoice invoice = getById(id);
        if (!invoice.isDeletable()) {
            throw new IllegalStateException("Solo se pueden eliminar facturas en borrador. Para facturas emitidas, cambia el estado a cancelada.");
        }
        if (paymentRepository.countByInvoice_Id(id) > 0) {
            throw new IllegalStateException("No se puede eliminar una factura con cobros registrados.");
        }
        if (invoice.getQuote() != null) {
            invoice.getQuote().changeStatus(QuoteStatus.SENT);
        }
        invoiceRepository.delete(invoice);
    }

    private Invoice buildInvoiceFromAcceptedQuote(Long quoteId) {
        Quote quote = quoteRepository.findByIdWithClientAndLines(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Presupuesto no encontrado"));
        if (quote.getStatus() != QuoteStatus.ACCEPTED) {
            throw new IllegalStateException("Solo se pueden facturar presupuestos aceptados");
        }

        LocalDate issueDate = LocalDate.now();
        Invoice invoice = new Invoice(
                documentNumberService.nextInvoiceNumber(issueDate),
                quote,
                quote.getClient(),
                issueDate,
                documentNumberService.defaultInvoiceDueDate(issueDate),
                quote.getNotes()
        );
        List<InvoiceLine> lines = quote.getLines().stream().map(this::copyLine).toList();
        invoice.replaceLines(lines);
        return invoiceRepository.save(invoice);
    }

    private InvoiceLine copyLine(QuoteLine line) {
        return new InvoiceLine(
                line.getProduct(),
                line.getDescription(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getVatRate()
        );
    }

    private String normalizeQuery(String query) {
        return query == null || query.trim().isBlank() ? null : query.trim();
    }
}
