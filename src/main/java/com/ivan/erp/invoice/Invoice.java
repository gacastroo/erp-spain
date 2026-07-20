package com.ivan.erp.invoice;

import com.ivan.erp.client.Client;
import com.ivan.erp.quote.Quote;
import com.ivan.erp.shared.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "invoices",
        indexes = {
                @Index(name = "idx_invoices_number", columnList = "invoice_number"),
                @Index(name = "idx_invoices_client", columnList = "client_id"),
                @Index(name = "idx_invoices_status", columnList = "status"),
                @Index(name = "idx_invoices_issue_date", columnList = "issue_date"),
                @Index(name = "idx_invoices_due_date", columnList = "due_date"),
                @Index(name = "idx_invoices_quote", columnList = "quote_id")
        }
)
public class Invoice extends BaseEntity {

    @Column(name = "invoice_number", nullable = false, unique = true, length = 40)
    private String invoiceNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", unique = true)
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_at")
    private LocalDate paidAt;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "vat_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal vatTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<InvoiceLine> lines = new ArrayList<>();

    protected Invoice() {
    }

    public Invoice(String invoiceNumber, Quote quote, Client client, LocalDate issueDate, LocalDate dueDate, String notes) {
        this.invoiceNumber = invoiceNumber;
        this.quote = quote;
        this.client = client;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.notes = cleanNullable(notes);
        this.status = InvoiceStatus.DRAFT;
    }

    public void replaceLines(List<InvoiceLine> newLines) {
        this.lines.clear();

        int index = 1;
        for (InvoiceLine line : newLines) {
            line.attachToInvoice(this, index++);
            this.lines.add(line);
        }

        recalculateTotals();
    }

    public void changeStatus(InvoiceStatus status) {
        this.status = status;

        if (status != InvoiceStatus.PAID) {
            this.paidAt = null;
        }
    }

    public void markPaid(LocalDate paymentDate) {
        this.status = InvoiceStatus.PAID;
        this.paidAt = paymentDate != null ? paymentDate : LocalDate.now();
    }

    public void markPendingAfterPaymentChange(LocalDate today) {
        this.paidAt = null;

        if (this.status == InvoiceStatus.CANCELLED || this.status == InvoiceStatus.DRAFT) {
            return;
        }

        if (this.dueDate != null && this.dueDate.isBefore(today)) {
            this.status = InvoiceStatus.OVERDUE;
            return;
        }

        // A payment does not prove that an ISSUED invoice was sent. Preserve the
        // commercial state for partial payments. When a previously PAID invoice
        // becomes pending after deleting a payment, SENT is the safest fallback.
        if (this.status == InvoiceStatus.PAID) {
            this.status = InvoiceStatus.SENT;
        }
    }

    public boolean isDeletable() {
        return status == InvoiceStatus.DRAFT;
    }

    public boolean canRegisterPayment() {
        return status == InvoiceStatus.ISSUED
                || status == InvoiceStatus.SENT
                || status == InvoiceStatus.OVERDUE;
    }

    public void recalculateTotals() {
        BigDecimal newSubtotal = BigDecimal.ZERO;
        BigDecimal newVatTotal = BigDecimal.ZERO;
        BigDecimal newTotal = BigDecimal.ZERO;

        for (InvoiceLine line : lines) {
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

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public Quote getQuote() {
        return quote;
    }

    public Client getClient() {
        return client;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getPaidAt() {
        return paidAt;
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

    public List<InvoiceLine> getLines() {
        return lines;
    }
}
