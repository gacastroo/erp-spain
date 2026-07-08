package com.ivan.erp.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByTaxIdIgnoreCase(String taxId);

    boolean existsByTaxIdIgnoreCase(String taxId);

    boolean existsByTaxIdIgnoreCaseAndIdNot(String taxId, Long id);

    long countByEnabledTrue();

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
