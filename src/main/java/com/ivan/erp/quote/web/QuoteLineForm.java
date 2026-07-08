package com.ivan.erp.quote.web;

import com.ivan.erp.quote.QuoteLine;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class QuoteLineForm {

    private Long productId;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String description;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "La cantidad no es válida")
    private BigDecimal quantity = BigDecimal.ONE;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El precio no es válido")
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @NotNull(message = "El IVA es obligatorio")
    @DecimalMin(value = "0.00", message = "El IVA no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El IVA no puede superar el 100%")
    @Digits(integer = 3, fraction = 2, message = "El IVA no es válido")
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
        return (description == null || description.trim().isBlank())
                && productId == null
                && (unitPrice == null || BigDecimal.ZERO.compareTo(unitPrice) == 0);
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = clean(description);
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public void setVatRate(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }
}
