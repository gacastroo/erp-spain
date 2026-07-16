package com.ivan.erp.invoice;

import com.ivan.erp.product.Product;
import com.ivan.erp.shared.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(
        name = "invoice_lines",
        indexes = {
                @Index(name = "idx_invoice_lines_invoice", columnList = "invoice_id"),
                @Index(name = "idx_invoice_lines_product", columnList = "product_id")
        }
)
public class InvoiceLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatRate;

    @Column(name = "line_subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineSubtotal = BigDecimal.ZERO;

    @Column(name = "line_vat", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineVat = BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected InvoiceLine() {
    }

    public InvoiceLine(Product product, String description, BigDecimal quantity, BigDecimal unitPrice, BigDecimal vatRate) {
        this.product = product;
        this.description = clean(description);
        this.quantity = normalizeMoney(quantity);
        this.unitPrice = normalizeMoney(unitPrice);
        this.vatRate = normalizeMoney(vatRate);
        recalculate();
    }

    public void attachToInvoice(Invoice invoice, int sortOrder) {
        this.invoice = invoice;
        this.sortOrder = sortOrder;
    }

    public void recalculate() {
        this.lineSubtotal = normalizeMoney(quantity.multiply(unitPrice));
        this.lineVat = normalizeMoney(lineSubtotal.multiply(vatRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        this.lineTotal = normalizeMoney(lineSubtotal.add(lineVat));
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public Product getProduct() {
        return product;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public BigDecimal getLineSubtotal() {
        return lineSubtotal;
    }

    public BigDecimal getLineVat() {
        return lineVat;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
