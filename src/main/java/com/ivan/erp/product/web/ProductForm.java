package com.ivan.erp.product.web;

import com.ivan.erp.product.Product;
import com.ivan.erp.product.ProductType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProductForm {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 180, message = "El nombre no puede superar los 180 caracteres")
    private String name;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String description;

    @Size(max = 80, message = "La referencia no puede superar los 80 caracteres")
    private String sku;

    @NotNull(message = "El tipo es obligatorio")
    private ProductType productType = ProductType.PRODUCT;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener como máximo 10 enteros y 2 decimales")
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @NotNull(message = "El IVA es obligatorio")
    @DecimalMin(value = "0.00", message = "El IVA no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El IVA no puede superar el 100%")
    @Digits(integer = 3, fraction = 2, message = "El IVA debe tener como máximo 3 enteros y 2 decimales")
    private BigDecimal vatRate = new BigDecimal("21.00");

    public static ProductForm fromProduct(Product product) {
        ProductForm form = new ProductForm();
        form.setName(product.getName());
        form.setDescription(product.getDescription());
        form.setSku(product.getSku());
        form.setProductType(product.getProductType());
        form.setUnitPrice(product.getUnitPrice());
        form.setVatRate(product.getVatRate());
        return form;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = clean(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = clean(description);
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = clean(sku);
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
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
