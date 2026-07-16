package com.ivan.erp.payment;

import com.ivan.erp.invoice.Invoice;
import com.ivan.erp.shared.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_invoice", columnList = "invoice_id"),
                @Index(name = "idx_payments_date", columnList = "payment_date"),
                @Index(name = "idx_payments_method", columnList = "method")
        }
)
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod method;

    @Column(length = 120)
    private String reference;

    @Column(length = 500)
    private String notes;

    protected Payment() {
    }

    public Payment(
            Invoice invoice,
            LocalDate paymentDate,
            BigDecimal amount,
            PaymentMethod method,
            String reference,
            String notes
    ) {
        this.invoice = invoice;
        this.paymentDate = paymentDate;
        this.amount = normalizeMoney(amount);
        this.method = method;
        this.reference = cleanNullable(reference);
        this.notes = cleanNullable(notes);
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

    public Invoice getInvoice() {
        return invoice;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public String getReference() {
        return reference;
    }

    public String getNotes() {
        return notes;
    }
}
