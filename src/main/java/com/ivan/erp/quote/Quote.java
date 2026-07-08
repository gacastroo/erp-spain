package com.ivan.erp.quote;

import com.ivan.erp.client.Client;
import com.ivan.erp.shared.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "quotes",
        indexes = {
                @Index(name = "idx_quotes_number", columnList = "quote_number"),
                @Index(name = "idx_quotes_client", columnList = "client_id"),
                @Index(name = "idx_quotes_status", columnList = "status"),
                @Index(name = "idx_quotes_issue_date", columnList = "issue_date")
        }
)
public class Quote extends BaseEntity {

    @Column(name = "quote_number", nullable = false, unique = true, length = 40)
    private String quoteNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuoteStatus status = QuoteStatus.DRAFT;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "vat_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal vatTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<QuoteLine> lines = new ArrayList<>();

    protected Quote() {
    }

    public Quote(String quoteNumber, Client client, LocalDate issueDate, LocalDate validUntil, String notes) {
        this.quoteNumber = quoteNumber;
        this.client = client;
        this.issueDate = issueDate;
        this.validUntil = validUntil;
        this.notes = cleanNullable(notes);
        this.status = QuoteStatus.DRAFT;
    }

    public void updateHeader(Client client, LocalDate issueDate, LocalDate validUntil, String notes) {
        this.client = client;
        this.issueDate = issueDate;
        this.validUntil = validUntil;
        this.notes = cleanNullable(notes);
    }

    public void replaceLines(List<QuoteLine> newLines) {
        this.lines.clear();

        int index = 1;
        for (QuoteLine line : newLines) {
            line.attachToQuote(this, index++);
            this.lines.add(line);
        }

        recalculateTotals();
    }

    public void changeStatus(QuoteStatus status) {
        this.status = status;
    }

    public void recalculateTotals() {
        BigDecimal newSubtotal = BigDecimal.ZERO;
        BigDecimal newVatTotal = BigDecimal.ZERO;
        BigDecimal newTotal = BigDecimal.ZERO;

        for (QuoteLine line : lines) {
            line.recalculate();

            newSubtotal = newSubtotal.add(line.getLineSubtotal());
            newVatTotal = newVatTotal.add(line.getLineVat());
            newTotal = newTotal.add(line.getLineTotal());
        }

        this.subtotal = normalizeMoney(newSubtotal);
        this.vatTotal = normalizeMoney(newVatTotal);
        this.total = normalizeMoney(newTotal);
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String cleanNullable(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        return value.trim();
    }

    public String getQuoteNumber() {
        return quoteNumber;
    }

    public Client getClient() {
        return client;
    }

    public QuoteStatus getStatus() {
        return status;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public String getNotes() {
        return notes;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getVatTotal() {
        return vatTotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<QuoteLine> getLines() {
        return lines;
    }
}
