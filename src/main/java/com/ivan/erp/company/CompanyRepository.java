package com.ivan.erp.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByTaxIdIgnoreCase(String taxId);

    Optional<Company> findFirstByEnabledTrueOrderByIdAsc();
}
