package com.ivan.erp.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByTaxIdIgnoreCase(String taxId);

    Optional<Client> findByIdAndEnabledTrue(Long id);

    boolean existsByTaxIdIgnoreCase(String taxId);

    boolean existsByTaxIdIgnoreCaseAndIdNot(String taxId, Long id);

    long countByEnabledTrue();

    List<Client> findByEnabledTrueOrderByLegalNameAsc();

    @Query("""
            select c
            from Client c
            where (:q is null or :q = ''
                or lower(c.legalName) like lower(concat('%', :q, '%'))
                or lower(c.commercialName) like lower(concat('%', :q, '%'))
                or lower(c.taxId) like lower(concat('%', :q, '%'))
                or lower(c.email) like lower(concat('%', :q, '%')))
            """)
    Page<Client> search(@Param("q") String query, Pageable pageable);
}
