package com.ivan.erp.quote;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    @Query("""
            SELECT q
            FROM Quote q
            JOIN FETCH q.client
            WHERE q.id = :id
            """)
    Optional<Quote> findByIdWithClient(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT q
            FROM Quote q
            JOIN FETCH q.client
            LEFT JOIN FETCH q.lines l
            LEFT JOIN FETCH l.product
            WHERE q.id = :id
            """)
    Optional<Quote> findByIdWithClientAndLines(@Param("id") Long id);

    @Query(
            value = """
                    SELECT q
                    FROM Quote q
                    JOIN FETCH q.client c
                    WHERE
                        :query IS NULL
                        OR LOWER(q.quoteNumber) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(c.legalName) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(COALESCE(c.taxId, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    """,
            countQuery = """
                    SELECT COUNT(q)
                    FROM Quote q
                    JOIN q.client c
                    WHERE
                        :query IS NULL
                        OR LOWER(q.quoteNumber) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(c.legalName) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(COALESCE(c.taxId, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    """
    )
    Page<Quote> search(@Param("query") String query, Pageable pageable);

    Optional<Quote> findTopByQuoteNumberStartingWithOrderByQuoteNumberDesc(String prefix);

    @Query("""
            SELECT q
            FROM Quote q
            JOIN FETCH q.client
            WHERE NOT EXISTS (
                SELECT i.id
                FROM Invoice i
                WHERE i.quote = q
            )
            ORDER BY q.issueDate DESC, q.id DESC
            """)
    List<Quote> findInvoiceableQuotes();

}
