package com.ivan.erp.company;

import com.ivan.erp.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "companies",
        indexes = {
                @Index(name = "idx_companies_tax_id", columnList = "tax_id", unique = true)
        }
)
public class Company extends BaseEntity {

    @Column(name = "legal_name", nullable = false, length = 180)
    private String legalName;

    @Column(name = "commercial_name", length = 180)
    private String commercialName;

    @Column(name = "tax_id", nullable = false, unique = true, length = 20)
    private String taxId;

    @Column(length = 180)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(name = "address_line", length = 255)
    private String addressLine;

    @Column(length = 120)
    private String city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 120)
    private String province;

    @Column(length = 80)
    private String country = "España";

    @Column(name = "invoice_series", nullable = false, length = 20)
    private String invoiceSeries = "FAC";

    @Column(name = "quote_series", nullable = false, length = 20)
    private String quoteSeries = "PRE";

    @Column(name = "bank_name", length = 120)
    private String bankName;

    @Column(name = "bank_iban", length = 34)
    private String bankIban;

    @Column(name = "logo_text", length = 80)
    private String logoText;

    @Column(name = "invoice_legal_text", length = 1200)
    private String invoiceLegalText;

    @Column(name = "default_payment_terms_days", nullable = false)
    private Integer defaultPaymentTermsDays = 30;

    @Column(nullable = false)
    private boolean enabled = true;

    protected Company() {
    }

    public Company(String legalName, String taxId) {
        this.legalName = normalizeText(legalName);
        this.taxId = normalizeTaxId(taxId);
        this.logoText = initialsFromName(legalName);
    }

    public void updateSettings(
            String legalName,
            String commercialName,
            String taxId,
            String email,
            String phone,
            String addressLine,
            String city,
            String postalCode,
            String province,
            String country,
            String invoiceSeries,
            String quoteSeries,
            String bankName,
            String bankIban,
            String logoText,
            String invoiceLegalText,
            Integer defaultPaymentTermsDays
    ) {
        this.legalName = normalizeText(legalName);
        this.commercialName = normalizeNullable(commercialName);
        this.taxId = normalizeTaxId(taxId);
        this.email = normalizeEmail(email);
        this.phone = normalizeNullable(phone);
        this.addressLine = normalizeNullable(addressLine);
        this.city = normalizeNullable(city);
        this.postalCode = normalizeNullable(postalCode);
        this.province = normalizeNullable(province);
        this.country = normalizeNullable(country) == null ? "España" : normalizeNullable(country);
        this.invoiceSeries = normalizeSeries(invoiceSeries, "FAC");
        this.quoteSeries = normalizeSeries(quoteSeries, "PRE");
        this.bankName = normalizeNullable(bankName);
        this.bankIban = normalizeIban(bankIban);
        this.logoText = normalizeNullable(logoText);
        this.invoiceLegalText = normalizeNullable(invoiceLegalText);
        this.defaultPaymentTermsDays = normalizePaymentTerms(defaultPaymentTermsDays);
    }

    private Integer normalizePaymentTerms(Integer value) {
        if (value == null || value < 0) {
            return 30;
        }
        return Math.min(value, 365);
    }

    private String normalizeSeries(String value, String fallback) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            return fallback;
        }
        return normalized.toUpperCase().replace(" ", "");
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeEmail(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    private String normalizeTaxId(String value) {
        return value == null ? null : value.trim().toUpperCase().replace(" ", "");
    }

    private String normalizeIban(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toUpperCase().replace(" ", "");
    }

    private String initialsFromName(String name) {
        if (name == null || name.isBlank()) {
            return "ERP";
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isBlank() && initials.length() < 3) {
                initials.append(part.charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }

    public String getLegalName() {
        return legalName;
    }

    public String getCommercialName() {
        return commercialName;
    }

    public String getTaxId() {
        return taxId;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getProvince() {
        return province;
    }

    public String getCountry() {
        return country;
    }

    public String getInvoiceSeries() {
        return invoiceSeries;
    }

    public String getQuoteSeries() {
        return quoteSeries;
    }

    public String getBankName() {
        return bankName;
    }

    public String getBankIban() {
        return bankIban;
    }

    public String getLogoText() {
        return logoText;
    }

    public String getInvoiceLegalText() {
        return invoiceLegalText;
    }

    public Integer getDefaultPaymentTermsDays() {
        return defaultPaymentTermsDays;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
