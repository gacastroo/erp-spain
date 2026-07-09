package com.ivan.erp.expense;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query(
            value = """
                    SELECT e
                    FROM Expense e
                    WHERE
                        :query IS NULL
                        OR LOWER(e.supplierName) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(COALESCE(e.invoiceNumber, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(COALESCE(e.description, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    """,
            countQuery = """
                    SELECT COUNT(e)
                    FROM Expense e
                    WHERE
                        :query IS NULL
                        OR LOWER(e.supplierName) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(COALESCE(e.invoiceNumber, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(COALESCE(e.description, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    """
    )
    Page<Expense> search(@Param("query") String query, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(e.total), 0)
            FROM Expense e
            WHERE e.expenseDate BETWEEN :start AND :end
            """)
    BigDecimal sumTotalByExpenseDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("""
            SELECT COALESCE(SUM(e.vatAmount), 0)
            FROM Expense e
            WHERE e.expenseDate BETWEEN :start AND :end
            """)
    BigDecimal sumVatAmountByExpenseDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    long countByPaidFalse();

    List<Expense> findTop3ByOrderByExpenseDateDescIdDesc();

    List<Expense> findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(LocalDate start, LocalDate end);
}
