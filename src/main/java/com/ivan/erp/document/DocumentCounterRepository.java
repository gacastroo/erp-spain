package com.ivan.erp.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentCounterRepository extends JpaRepository<DocumentCounter, Long> {

    /**
     * Atomically increments the counter and stores the generated value in the
     * current MySQL connection through LAST_INSERT_ID(expr). The service reads
     * it in the same transaction, so concurrent requests cannot receive the
     * same sequence.
     */
    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO document_counters (
                document_type, series, document_year, current_value, created_at, updated_at
            ) VALUES (
                :documentType, :series, :documentYear, LAST_INSERT_ID(1), NOW(6), NOW(6)
            )
            ON DUPLICATE KEY UPDATE
                current_value = LAST_INSERT_ID(document_counters.current_value + 1),
                updated_at = NOW(6)
            """, nativeQuery = true)
    void increment(
            @Param("documentType") String documentType,
            @Param("series") String series,
            @Param("documentYear") int documentYear
    );

    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    long lastGeneratedValue();
}
