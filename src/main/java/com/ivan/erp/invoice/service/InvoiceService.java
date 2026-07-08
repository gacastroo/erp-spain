package com.ivan.erp.invoice.service;

import com.ivan.erp.invoice.*;
import com.ivan.erp.quote.Quote;
import com.ivan.erp.quote.QuoteLine;
import com.ivan.erp.quote.QuoteRepository;
import com.ivan.erp.quote.QuoteStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {

    private static final int PAGE_SIZE = 10;

    private final InvoiceRepository invoiceRepository;
    private final QuoteRepository quoteRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, QuoteRepository quoteRepository) {
        this.invoiceRepository = invoiceRepository;
        this.quoteRepository = quoteRepository;
    }

    @Transactional(readOnly = true)
    public Page<Invoice> search(String query, int page) {
        String normalizedQuery = normalizeQuery(query);

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "issueDate").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        return invoiceRepository.search(normalizedQuery, pageable);
    }

    @Transactional(readOnly = true)
    public Invoice getById(Long id) {
        return invoiceRepository.findByIdWithClientQuoteAndLines(id)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada"));
    }

    @Transactional
    public Invoice createFromQuote(Long quoteId) {
        return invoiceRepository.findByQuote_Id(quoteId)
                .map(existingInvoice -> {
                    existingInvoice.getQuote().changeStatus(QuoteStatus.ACCEPTED);
                    return existingInvoice;
                })
                .orElseGet(() -> buildInvoiceFromQuote(quoteId));
    }

    @Transactional
    public void changeStatus(Long id, InvoiceStatus status) {
        Invoice invoice = getById(id);
        invoice.changeStatus(status);

        if (invoice.getQuote() != null) {
            invoice.getQuote().changeStatus(QuoteStatus.ACCEPTED);
        }
    }

    @Transactional
    public void delete(Long id) {
        Invoice invoice = getById(id);

        if (!invoice.isDeletable()) {
            throw new IllegalStateException("Solo se pueden eliminar facturas en borrador. Para facturas emitidas, cambia el estado a cancelada.");
        }

        if (invoice.getQuote() != null) {
            invoice.getQuote().changeStatus(QuoteStatus.SENT);
        }

        invoiceRepository.delete(invoice);
    }

    private Invoice buildInvoiceFromQuote(Long quoteId) {
        Quote quote = quoteRepository.findByIdWithClientAndLines(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Presupuesto no encontrado"));

        LocalDate issueDate = LocalDate.now();
        quote.changeStatus(QuoteStatus.ACCEPTED);

        Invoice invoice = new Invoice(
                generateInvoiceNumber(issueDate),
                quote,
                quote.getClient(),
                issueDate,
                issueDate.plusDays(30),
                quote.getNotes()
        );

        List<InvoiceLine> lines = quote.getLines()
                .stream()
                .map(this::copyLine)
                .toList();

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

    private String generateInvoiceNumber(LocalDate issueDate) {
        int year = issueDate != null ? issueDate.getYear() : LocalDate.now().getYear();
        String prefix = "FAC-" + year + "-";

        return invoiceRepository.findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(prefix)
                .map(lastInvoice -> {
                    String lastNumber = lastInvoice.getInvoiceNumber().substring(prefix.length());
                    int nextNumber = Integer.parseInt(lastNumber) + 1;
                    return prefix + String.format("%06d", nextNumber);
                })
                .orElse(prefix + "000001");
    }

    private String normalizeQuery(String query) {
        if (query == null || query.trim().isBlank()) {
            return null;
        }
        return query.trim();
    }
}
