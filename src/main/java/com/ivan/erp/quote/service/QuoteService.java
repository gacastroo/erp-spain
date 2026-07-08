package com.ivan.erp.quote.service;

import com.ivan.erp.client.Client;
import com.ivan.erp.client.ClientRepository;
import com.ivan.erp.product.Product;
import com.ivan.erp.product.ProductRepository;
import com.ivan.erp.quote.*;
import com.ivan.erp.quote.web.QuoteForm;
import com.ivan.erp.quote.web.QuoteLineForm;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class QuoteService {

    private static final int PAGE_SIZE = 10;

    private final QuoteRepository quoteRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    public QuoteService(
            QuoteRepository quoteRepository,
            ClientRepository clientRepository,
            ProductRepository productRepository
    ) {
        this.quoteRepository = quoteRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<Quote> search(String query, int page) {
        String normalizedQuery = normalizeQuery(query);

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "issueDate").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        return quoteRepository.search(normalizedQuery, pageable);
    }

    @Transactional(readOnly = true)
    public Quote getById(Long id) {
        return quoteRepository.findByIdWithClientAndLines(id)
                .orElseThrow(() -> new EntityNotFoundException("Presupuesto no encontrado"));
    }

    @Transactional
    public Quote create(QuoteForm form) {
        Client client = getClient(form.getClientId());

        Quote quote = new Quote(
                generateQuoteNumber(form.getIssueDate()),
                client,
                form.getIssueDate(),
                form.getValidUntil(),
                form.getNotes()
        );

        quote.replaceLines(buildLines(form));

        return quoteRepository.save(quote);
    }

    @Transactional
    public Quote update(Long id, QuoteForm form) {
        Quote quote = getById(id);
        Client client = getClient(form.getClientId());

        quote.updateHeader(client, form.getIssueDate(), form.getValidUntil(), form.getNotes());
        quote.replaceLines(buildLines(form));

        return quote;
    }

    @Transactional
    public void changeStatus(Long id, QuoteStatus status) {
        Quote quote = getById(id);
        quote.changeStatus(status);
    }

    @Transactional
    public void delete(Long id) {
        Quote quote = getById(id);
        quoteRepository.delete(quote);
    }

    private List<QuoteLine> buildLines(QuoteForm form) {
        return form.getLines()
                .stream()
                .filter(line -> !line.isEmpty())
                .map(this::buildLine)
                .toList();
    }

    private QuoteLine buildLine(QuoteLineForm form) {
        Product product = null;

        if (form.getProductId() != null) {
            product = productRepository.findById(form.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        }

        return new QuoteLine(
                product,
                form.getDescription(),
                form.getQuantity(),
                form.getUnitPrice(),
                form.getVatRate()
        );
    }

    private Client getClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
    }

    private String generateQuoteNumber(LocalDate issueDate) {
        int year = issueDate != null ? issueDate.getYear() : LocalDate.now().getYear();
        String prefix = "PRE-" + year + "-";

        return quoteRepository.findTopByQuoteNumberStartingWithOrderByQuoteNumberDesc(prefix)
                .map(lastQuote -> {
                    String lastNumber = lastQuote.getQuoteNumber().substring(prefix.length());
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
