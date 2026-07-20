package com.ivan.erp.document;

import com.ivan.erp.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "document_counters",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_document_counters_type_series_year",
                columnNames = {"document_type", "series", "document_year"}
        ),
        indexes = @Index(
                name = "idx_document_counters_lookup",
                columnList = "document_type,series,document_year",
                unique = true
        )
)
public class DocumentCounter extends BaseEntity {

    @Column(name = "document_type", nullable = false, length = 20)
    private String documentType;

    @Column(nullable = false, length = 30)
    private String series;

    @Column(name = "document_year", nullable = false)
    private int documentYear;

    @Column(name = "current_value", nullable = false)
    private long currentValue;

    protected DocumentCounter() {
    }
}
