package com.ivan.erp.document.service;

import com.ivan.erp.company.Company;
import com.ivan.erp.company.service.CompanySettingsService;
import com.ivan.erp.document.DocumentCounterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

@Service
public class DocumentNumberService {

    private static final String INVOICE = "INVOICE";
    private static final String QUOTE = "QUOTE";

    private final CompanySettingsService companySettingsService;
    private final DocumentCounterRepository documentCounterRepository;

    public DocumentNumberService(
            CompanySettingsService companySettingsService,
            DocumentCounterRepository documentCounterRepository
    ) {
        this.companySettingsService = companySettingsService;
        this.documentCounterRepository = documentCounterRepository;
    }

    @Transactional
    public String nextInvoiceNumber(LocalDate issueDate) {
        Company company = companySettingsService.getActiveCompany();
        return nextNumber(INVOICE, company.getInvoiceSeries(), issueDate);
    }

    @Transactional
    public String nextQuoteNumber(LocalDate issueDate) {
        Company company = companySettingsService.getActiveCompany();
        return nextNumber(QUOTE, company.getQuoteSeries(), issueDate);
    }

    public LocalDate defaultInvoiceDueDate(LocalDate issueDate) {
        LocalDate baseDate = issueDate != null ? issueDate : LocalDate.now();
        Integer days = companySettingsService.getActiveCompany().getDefaultPaymentTermsDays();
        return baseDate.plusDays(days != null ? days : 30);
    }

    private String nextNumber(String documentType, String configuredSeries, LocalDate documentDate) {
        LocalDate effectiveDate = documentDate != null ? documentDate : LocalDate.now();
        String series = normalizeSeries(configuredSeries);
        int year = effectiveDate.getYear();

        documentCounterRepository.increment(documentType, series, year);
        long sequence = documentCounterRepository.lastGeneratedValue();

        if (sequence < 1) {
            throw new IllegalStateException("The document counter returned an invalid sequence");
        }

        return series + "-" + year + "-" + String.format(Locale.ROOT, "%06d", sequence);
    }

    private String normalizeSeries(String series) {
        String normalized = series == null ? "" : series.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
        if (normalized.isBlank()) {
            return "DOC";
        }
        if (!normalized.matches("[A-Z0-9_-]{1,30}")) {
            throw new IllegalStateException("La serie documental contiene caracteres no permitidos");
        }
        return normalized;
    }
}
