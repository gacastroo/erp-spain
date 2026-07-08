package com.ivan.erp.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {"invoice", "invoice.client"})
    @Query(
            value = """
                    SELECT p
                    FROM Payment p
                    JOIN p.invoice i
                    JOIN i.client c
                    WHERE
                        :query IS NULL
                        OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(c.legalName) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(COALESCE(p.reference, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM Payment p
                    JOIN p.invoice i
                    JOIN i.client c
                    WHERE
                        :query IS NULL
                        OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(c.legalName) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(COALESCE(p.reference, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    """
    )
    Page<Payment> search(@Param("query") String query, Pageable pageable);

    @EntityGraph(attributePaths = {"invoice", "invoice.client"})
    Optional<Payment> findById(Long id);

    @EntityGraph(attributePaths = {"invoice", "invoice.client"})
    List<Payment> findByInvoice_IdOrderByPaymentDateDescIdDesc(Long invoiceId);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.invoice.id = :invoiceId
            """)
    BigDecimal sumAmountByInvoiceId(@Param("invoiceId") Long invoiceId);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.paymentDate BETWEEN :start AND :end
            """)
    BigDecimal sumAmountByPaymentDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @EntityGraph(attributePaths = {"invoice", "invoice.client"})
    List<Payment> findByPaymentDateBetweenOrderByPaymentDateDescIdDesc(LocalDate start, LocalDate end);

    long countByInvoice_Id(Long invoiceId);

    @EntityGraph(attributePaths = {"invoice", "invoice.client"})
    List<Payment> findTop5ByOrderByPaymentDateDescIdDesc();
}
