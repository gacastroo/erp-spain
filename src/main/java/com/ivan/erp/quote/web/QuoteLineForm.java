package com.ivan.erp.quote.web;

import com.ivan.erp.quote.QuoteLine;
import com.ivan.erp.quote.web.validation.ValidQuoteLine;

import java.math.BigDecimal;

@ValidQuoteLine
public class QuoteLineForm {

    private Long productId;
    private String description;
    private BigDecimal quantity = BigDecimal.ONE;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal vatRate = new BigDecimal("21.00");

    public static QuoteLineForm fromLine(QuoteLine line) {
        QuoteLineForm form = new QuoteLineForm();
        if (line.getProduct() != null) {
            form.setProductId(line.getProduct().getId());
        }
        form.setDescription(line.getDescription());
        form.setQuantity(line.getQuantity());
        form.setUnitPrice(line.getUnitPrice());
        form.setVatRate(line.getVatRate());
        return form;
    }

    public boolean isEmpty() {
        return (description == null || description.isBlank())
                && productId == null
                && (quantity == null || BigDecimal.ONE.compareTo(quantity) == 0)
                && (unitPrice == null || BigDecimal.ZERO.compareTo(unitPrice) == 0)
                && (vatRate == null || new BigDecimal("21.00").compareTo(vatRate) == 0);
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = clean(description); }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getVatRate() { return vatRate; }
    public void setVatRate(BigDecimal vatRate) { this.vatRate = vatRate; }
    private String clean(String value) { return value == null ? null : value.trim(); }
}
