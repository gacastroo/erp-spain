package com.ivan.erp.expense;

import com.ivan.erp.payment.PaymentMethod;
import com.ivan.erp.shared.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(
        name = "expenses",
        indexes = {
                @Index(name = "idx_expenses_date", columnList = "expense_date"),
                @Index(name = "idx_expenses_supplier", columnList = "supplier_name"),
                @Index(name = "idx_expenses_category", columnList = "category"),
                @Index(name = "idx_expenses_paid", columnList = "paid")
        }
)
public class Expense extends BaseEntity {

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "supplier_name", nullable = false, length = 180)
    private String supplierName;

    @Column(name = "supplier_tax_id", length = 20)
    private String supplierTaxId;

    @Column(name = "invoice_number", length = 80)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ExpenseCategory category;

    @Column(length = 500)
    private String description;

    @Column(name = "base_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseAmount = BigDecimal.ZERO;

    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatRate = BigDecimal.ZERO;

    @Column(name = "vat_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal vatAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean paid = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod = PaymentMethod.TRANSFER;

    @Column(length = 500)
    private String notes;

    protected Expense() {
    }

    public Expense(
            LocalDate expenseDate,
            String supplierName,
            String supplierTaxId,
            String invoiceNumber,
            ExpenseCategory category,
            String description,
            BigDecimal baseAmount,
            BigDecimal vatRate,
            boolean paid,
            PaymentMethod paymentMethod,
            String notes
    ) {
        update(
                expenseDate,
                supplierName,
                supplierTaxId,
                invoiceNumber,
                category,
                description,
                baseAmount,
                vatRate,
                paid,
                paymentMethod,
                notes
        );
    }

    public void update(
            LocalDate expenseDate,
            String supplierName,
            String supplierTaxId,
            String invoiceNumber,
            ExpenseCategory category,
            String description,
            BigDecimal baseAmount,
            BigDecimal vatRate,
            boolean paid,
            PaymentMethod paymentMethod,
            String notes
    ) {
        this.expenseDate = expenseDate;
        this.supplierName = supplierName != null ? supplierName.trim() : null;
        this.supplierTaxId = cleanNullable(supplierTaxId);
        this.invoiceNumber = cleanNullable(invoiceNumber);
        this.category = category;
        this.description = cleanNullable(description);
        this.baseAmount = normalizeMoney(baseAmount);
        this.vatRate = normalizeVat(vatRate);
        this.paid = paid;
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.TRANSFER;
        this.notes = cleanNullable(notes);
        recalculateTotals();
    }

    public void recalculateTotals() {
        BigDecimal vat = baseAmount
                .multiply(vatRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        this.vatAmount = normalizeMoney(vat);
        this.total = normalizeMoney(this.baseAmount.add(this.vatAmount));
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeVat(BigDecimal value) {
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

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getSupplierTaxId() {
        return supplierTaxId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public boolean isPaid() {
        return paid;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getNotes() {
        return notes;
    }
}
