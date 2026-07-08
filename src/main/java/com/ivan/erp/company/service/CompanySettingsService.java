package com.ivan.erp.company.service;

import com.ivan.erp.company.Company;
import com.ivan.erp.company.CompanyRepository;
import com.ivan.erp.company.web.CompanySettingsForm;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanySettingsService {

    private final CompanyRepository companyRepository;

    public CompanySettingsService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public Company getActiveCompany() {
        return companyRepository.findFirstByEnabledTrueOrderByIdAsc()
                .orElseThrow(() -> new EntityNotFoundException("Empresa no configurada"));
    }

    @Transactional(readOnly = true)
    public CompanySettingsForm getSettingsForm() {
        return CompanySettingsForm.fromCompany(getActiveCompany());
    }

    @Transactional
    public Company update(CompanySettingsForm form) {
        Company company = getActiveCompany();
        company.updateSettings(
                form.getLegalName(),
                form.getCommercialName(),
                form.getTaxId(),
                form.getEmail(),
                form.getPhone(),
                form.getAddressLine(),
                form.getCity(),
                form.getPostalCode(),
                form.getProvince(),
                form.getCountry(),
                form.getInvoiceSeries(),
                form.getQuoteSeries(),
                form.getBankName(),
                form.getBankIban(),
                form.getLogoText(),
                form.getInvoiceLegalText(),
                form.getDefaultPaymentTermsDays()
        );
        return company;
    }
}
