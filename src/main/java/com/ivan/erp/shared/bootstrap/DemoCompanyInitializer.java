package com.ivan.erp.shared.bootstrap;

import com.ivan.erp.company.Company;
import com.ivan.erp.company.CompanyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Local demo-only company bootstrap. It is never active unless the dev or demo
 * Spring profile is selected explicitly.
 */
@Component
@Profile({"dev", "demo"})
public class DemoCompanyInitializer implements CommandLineRunner {

    private final CompanyRepository companyRepository;

    public DemoCompanyInitializer(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        companyRepository.findByTaxIdIgnoreCase("B00000000")
                .orElseGet(() -> companyRepository.save(
                        new Company("Empresa Demo S.L.", "B00000000")
                ));
    }
}
