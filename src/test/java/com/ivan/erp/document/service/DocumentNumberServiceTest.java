package com.ivan.erp.document.service;

import com.ivan.erp.company.Company;
import com.ivan.erp.company.service.CompanySettingsService;
import com.ivan.erp.document.DocumentCounterRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentNumberServiceTest {

    private final CompanySettingsService companySettingsService = mock(CompanySettingsService.class);
    private final DocumentCounterRepository counterRepository = mock(DocumentCounterRepository.class);

    @Test
    void obtainsTheSequenceFromTheAtomicCounter() {
        Company company = company("fac");
        when(companySettingsService.getActiveCompany()).thenReturn(company);
        when(counterRepository.lastGeneratedValue()).thenReturn(42L);

        DocumentNumberService service = new DocumentNumberService(companySettingsService, counterRepository);

        assertThat(service.nextInvoiceNumber(LocalDate.of(2026, 7, 20)))
                .isEqualTo("FAC-2026-000042");
        var order = inOrder(counterRepository);
        order.verify(counterRepository).increment("INVOICE", "FAC", 2026);
        order.verify(counterRepository).lastGeneratedValue();
    }

    @Test
    void rejectsUnsafeSeriesCharacters() {
        when(companySettingsService.getActiveCompany()).thenReturn(company("FAC/../../"));

        DocumentNumberService service = new DocumentNumberService(companySettingsService, counterRepository);

        assertThatThrownBy(() -> service.nextInvoiceNumber(LocalDate.of(2026, 7, 20)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("serie documental");
        verify(counterRepository, org.mockito.Mockito.never())
                .increment(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt());
    }

    private Company company(String invoiceSeries) {
        Company company = new Company("Empresa", "B00000000");
        company.updateSettings(
                "Empresa", null, "B00000000", null, null, null, null, null,
                null, "España", invoiceSeries, "PRE", null, null, null, null, 30
        );
        return company;
    }
}
