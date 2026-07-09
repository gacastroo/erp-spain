package com.ivan.erp.invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @EntityGraph(attributePaths = {"client"})
    @Query(
            value = """
                    SELECT i
                    FROM Invoice i
                    JOIN i.client c
                    WHERE
                        :query IS NULL
                        OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(c.legalName) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(COALESCE(c.taxId, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    """,
            countQuery = """
                    SELECT COUNT(i)
                    FROM Invoice i
                    JOIN i.client c
                    WHERE
                        :query IS NULL
                        OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(c.legalName) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(COALESCE(c.taxId, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    """
    )
    Page<Invoice> search(@Param("query") String query, Pageable pageable);

    @Query("""
            SELECT DISTINCT i
            FROM Invoice i
            JOIN FETCH i.client
            LEFT JOIN FETCH i.quote
            LEFT JOIN FETCH i.lines l
            LEFT JOIN FETCH l.product
            WHERE i.id = :id
            """)
    Optional<Invoice> findByIdWithClientQuoteAndLines(@Param("id") Long id);

    @EntityGraph(attributePaths = {"client", "quote"})
    Optional<Invoice> findByQuote_Id(Long quoteId);

    boolean existsByQuote_Id(Long quoteId);

    @Query("""
            SELECT i.quote.id
            FROM Invoice i
            WHERE i.quote IS NOT NULL
              AND i.quote.id IN :quoteIds
            """)
    Set<Long> findInvoicedQuoteIds(@Param("quoteIds") Collection<Long> quoteIds);

    Optional<Invoice> findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(String prefix);

    @EntityGraph(attributePaths = {"client", "quote"})
    List<Invoice> findByStatusNotInOrderByIssueDateDescIdDesc(Collection<InvoiceStatus> statuses);

    long countByStatus(InvoiceStatus status);

    long countByStatusIn(Collection<InvoiceStatus> statuses);

    @EntityGraph(attributePaths = {"client"})
    List<Invoice> findTop3ByOrderByIssueDateDescIdDesc();

    @Query("""
            SELECT COUNT(i)
            FROM Invoice i
            WHERE i.dueDate < :today
              AND i.status NOT IN :excludedStatuses
            """)
    long countOverdue(@Param("today") LocalDate today, @Param("excludedStatuses") Collection<InvoiceStatus> excludedStatuses);

    @Query("""
            SELECT COALESCE(SUM(i.total), 0)
            FROM Invoice i
            WHERE i.status = :status
              AND i.issueDate BETWEEN :start AND :end
            """)
    BigDecimal sumTotalByStatusAndIssueDateBetween(
            @Param("status") InvoiceStatus status,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @EntityGraph(attributePaths = {"client"})
    List<Invoice> findByIssueDateBetweenAndStatusNotInOrderByIssueDateDescIdDesc(
            LocalDate start,
            LocalDate end,
            Collection<InvoiceStatus> excludedStatuses
    );

    @Query("""
            SELECT DISTINCT i
            FROM Invoice i
            JOIN FETCH i.client
            LEFT JOIN FETCH i.lines l
            WHERE i.issueDate BETWEEN :start AND :end
              AND i.status NOT IN :excludedStatuses
            ORDER BY i.issueDate DESC, i.id DESC
            """)
    List<Invoice> findFiscalInvoicesWithLines(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("excludedStatuses") Collection<InvoiceStatus> excludedStatuses
    );

    @Query("""
            SELECT COALESCE(SUM(i.total), 0)
            FROM Invoice i
            WHERE i.issueDate BETWEEN :start AND :end
              AND i.status NOT IN :excludedStatuses
            """)
    BigDecimal sumTotalByIssueDateBetweenAndStatusNotIn(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("excludedStatuses") Collection<InvoiceStatus> excludedStatuses
    );

    @Query("""
            SELECT COALESCE(SUM(i.vatTotal), 0)
            FROM Invoice i
            WHERE i.issueDate BETWEEN :start AND :end
              AND i.status NOT IN :excludedStatuses
            """)
    BigDecimal sumVatTotalByIssueDateBetweenAndStatusNotIn(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("excludedStatuses") Collection<InvoiceStatus> excludedStatuses
    );

    @Query("""
            SELECT c.legalName, COUNT(i), COALESCE(SUM(i.total), 0)
            FROM Invoice i
            JOIN i.client c
            WHERE i.issueDate BETWEEN :start AND :end
              AND i.status NOT IN :excludedStatuses
            GROUP BY c.id, c.legalName
            ORDER BY COALESCE(SUM(i.total), 0) DESC
            """)
    List<Object[]> salesByClient(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("excludedStatuses") Collection<InvoiceStatus> excludedStatuses
    );

    @Query("""
            SELECT l.description, COALESCE(SUM(l.quantity), 0), COALESCE(SUM(l.lineTotal), 0)
            FROM Invoice i
            JOIN i.lines l
            WHERE i.issueDate BETWEEN :start AND :end
              AND i.status NOT IN :excludedStatuses
            GROUP BY l.description
            ORDER BY COALESCE(SUM(l.lineTotal), 0) DESC
            """)
    List<Object[]> salesByProduct(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("excludedStatuses") Collection<InvoiceStatus> excludedStatuses
    );
}
