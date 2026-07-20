package com.ivan.erp.product;

import com.ivan.erp.shared.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_products_name", columnList = "name"),
                @Index(name = "idx_products_sku", columnList = "sku"),
                @Index(name = "idx_products_enabled", columnList = "enabled"),
                @Index(name = "idx_products_type", columnList = "product_type")
        }
)
public class Product extends BaseEntity {

    @Column(nullable = false, length = 180)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 80, unique = true)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 30)
    private ProductType productType;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatRate;

    @Column(nullable = false)
    private boolean enabled = true;

    protected Product() {
    }

    public Product(
            String name,
            String description,
            String sku,
            ProductType productType,
            BigDecimal unitPrice,
            BigDecimal vatRate
    ) {
        this.name = clean(name);
        this.description = cleanNullable(description);
        this.sku = cleanNullableUpper(sku);
        this.productType = productType;
        this.unitPrice = normalizeDecimal(unitPrice);
        this.vatRate = normalizeDecimal(vatRate);
        this.enabled = true;
    }

    public void update(
            String name,
            String description,
            String sku,
            ProductType productType,
            BigDecimal unitPrice,
            BigDecimal vatRate
    ) {
        this.name = clean(name);
        this.description = cleanNullable(description);
        this.sku = cleanNullableUpper(sku);
        this.productType = productType;
        this.unitPrice = normalizeDecimal(unitPrice);
        this.vatRate = normalizeDecimal(vatRate);
    }

    public void activate() {
        this.enabled = true;
    }

    public void deactivate() {
        this.enabled = false;
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private String cleanNullable(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String cleanNullableUpper(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        return value.trim().toUpperCase();
    }

    private BigDecimal normalizeDecimal(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSku() {
        return sku;
    }

    public ProductType getProductType() {
        return productType;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
