package com.ivan.erp.client;

import com.ivan.erp.shared.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(
        name = "clients",
        indexes = {
                @Index(name = "idx_clients_legal_name", columnList = "legal_name"),
                @Index(name = "idx_clients_tax_id", columnList = "tax_id", unique = true),
                @Index(name = "idx_clients_email", columnList = "email"),
                @Index(name = "idx_clients_enabled", columnList = "enabled")
        }
)
public class Client extends BaseEntity {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false, length = 40)
    private ClientType clientType = ClientType.COMPANY;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private boolean enabled = true;

    protected Client() {
    }

    public Client(String legalName, String taxId, ClientType clientType) {
        this.legalName = normalizeText(legalName);
        this.taxId = normalizeTaxId(taxId);
        this.clientType = clientType == null ? ClientType.COMPANY : clientType;
    }

    public void update(
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
            ClientType clientType,
            String notes
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
        this.clientType = clientType == null ? ClientType.COMPANY : clientType;
        this.notes = normalizeNullable(notes);
    }

    public void activate() {
        this.enabled = true;
    }

    public void deactivate() {
        this.enabled = false;
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
        if (value == null || value.trim().isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private String normalizeTaxId(String value) {
        return value == null ? null : value.trim().toUpperCase().replace(" ", "");
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

    public ClientType getClientType() {
        return clientType;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
