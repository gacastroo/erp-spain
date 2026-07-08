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
    private String invoiceSeries = "A";

    @Column(name = "bank_iban", length = 34)
    private String bankIban;

    @Column(nullable = false)
    private boolean enabled = true;

    protected Company() {
    }

    public Company(String legalName, String taxId) {
        this.legalName = legalName;
        this.taxId = taxId.toUpperCase().trim();
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

    public String getInvoiceSeries() {
        return invoiceSeries;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
