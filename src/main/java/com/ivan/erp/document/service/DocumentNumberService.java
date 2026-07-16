package com.ivan.erp.document.service;

import com.ivan.erp.company.Company;
import com.ivan.erp.company.service.CompanySettingsService;
import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.quote.QuoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DocumentNumberService {

    private final CompanySettingsService companySettingsService;
    private final InvoiceRepository invoiceRepository;
    private final QuoteRepository quoteRepository;

    public DocumentNumberService(
            CompanySettingsService companySettingsService,
            InvoiceRepository invoiceRepository,
            QuoteRepository quoteRepository
    ) {
        this.companySettingsService = companySettingsService;
        this.invoiceRepository = invoiceRepository;
        this.quoteRepository = quoteRepository;
    }

    public String nextInvoiceNumber(LocalDate issueDate) {
        Company company = companySettingsService.getActiveCompany();
        String prefix = buildPrefix(company.getInvoiceSeries(), issueDate);
        return invoiceRepository.findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(prefix)
                .map(lastInvoice -> prefix + nextSequence(lastInvoice.getInvoiceNumber(), prefix))
                .orElse(prefix + "000001");
    }

    public String nextQuoteNumber(LocalDate issueDate) {
        Company company = companySettingsService.getActiveCompany();
        String prefix = buildPrefix(company.getQuoteSeries(), issueDate);
        return quoteRepository.findTopByQuoteNumberStartingWithOrderByQuoteNumberDesc(prefix)
                .map(lastQuote -> prefix + nextSequence(lastQuote.getQuoteNumber(), prefix))
                .orElse(prefix + "000001");
    }

    public LocalDate defaultInvoiceDueDate(LocalDate issueDate) {
        LocalDate baseDate = issueDate != null ? issueDate : LocalDate.now();
        Integer days = companySettingsService.getActiveCompany().getDefaultPaymentTermsDays();
        return baseDate.plusDays(days != null ? days : 30);
    }

    private String buildPrefix(String series, LocalDate issueDate) {
        int year = issueDate != null ? issueDate.getYear() : LocalDate.now().getYear();
        String normalizedSeries = series == null || series.isBlank() ? "DOC" : series.trim().toUpperCase().replace(" ", "");
        return normalizedSeries + "-" + year + "-";
    }

    private String nextSequence(String lastDocumentNumber, String prefix) {
        try {
            String lastNumber = lastDocumentNumber.substring(prefix.length());
            int nextNumber = Integer.parseInt(lastNumber) + 1;
            return String.format("%06d", nextNumber);
        } catch (RuntimeException ex) {
            return "000001";
        }
    }
}
