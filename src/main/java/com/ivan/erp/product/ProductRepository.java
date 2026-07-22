package com.ivan.erp.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByEnabledTrueOrderByNameAsc();

    Optional<Product> findByIdAndEnabledTrue(Long id);

    @Query("""
            SELECT p
            FROM Product p
            WHERE
                :query IS NULL
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(COALESCE(p.sku, '')) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<Product> search(@Param("query") String query, Pageable pageable);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    @Query("""
            SELECT COUNT(l)
            FROM InvoiceLine l
            WHERE l.product.id = :productId
            """)
    long countInvoiceLinesByProductId(@Param("productId") Long productId);

    @Query("""
            SELECT COUNT(l)
            FROM QuoteLine l
            WHERE l.product.id = :productId
            """)
    long countQuoteLinesByProductId(@Param("productId") Long productId);
}
