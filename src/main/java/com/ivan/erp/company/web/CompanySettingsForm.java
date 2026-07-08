package com.ivan.erp.company.web;

import com.ivan.erp.company.Company;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CompanySettingsForm {

    @NotBlank(message = "El nombre fiscal es obligatorio")
    @Size(max = 180, message = "El nombre fiscal no puede superar 180 caracteres")
    private String legalName;

    @Size(max = 180, message = "El nombre comercial no puede superar 180 caracteres")
    private String commercialName;

    @NotBlank(message = "El NIF/CIF es obligatorio")
    @Size(max = 20, message = "El NIF/CIF no puede superar 20 caracteres")
    private String taxId;

    @Email(message = "Introduce un email válido")
    @Size(max = 180, message = "El email no puede superar 180 caracteres")
    private String email;

    @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
    private String phone;

    @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
    private String addressLine;

    @Size(max = 120, message = "La ciudad no puede superar 120 caracteres")
    private String city;

    @Size(max = 20, message = "El código postal no puede superar 20 caracteres")
    private String postalCode;

    @Size(max = 120, message = "La provincia no puede superar 120 caracteres")
    private String province;

    @Size(max = 80, message = "El país no puede superar 80 caracteres")
    private String country = "España";

    @NotBlank(message = "La serie de facturas es obligatoria")
    @Size(max = 20, message = "La serie de facturas no puede superar 20 caracteres")
    private String invoiceSeries = "FAC";

    @NotBlank(message = "La serie de presupuestos es obligatoria")
    @Size(max = 20, message = "La serie de presupuestos no puede superar 20 caracteres")
    private String quoteSeries = "PRE";

    @Size(max = 120, message = "El banco no puede superar 120 caracteres")
    private String bankName;

    @Size(max = 34, message = "El IBAN no puede superar 34 caracteres")
    private String bankIban;

    @Size(max = 80, message = "El texto de logo no puede superar 80 caracteres")
    private String logoText;

    @Size(max = 1200, message = "El texto legal no puede superar 1200 caracteres")
    private String invoiceLegalText;

    @Min(value = 0, message = "Los días de vencimiento no pueden ser negativos")
    @Max(value = 365, message = "Los días de vencimiento no pueden superar 365")
    private Integer defaultPaymentTermsDays = 30;

    public static CompanySettingsForm fromCompany(Company company) {
        CompanySettingsForm form = new CompanySettingsForm();
        form.legalName = company.getLegalName();
        form.commercialName = company.getCommercialName();
        form.taxId = company.getTaxId();
        form.email = company.getEmail();
        form.phone = company.getPhone();
        form.addressLine = company.getAddressLine();
        form.city = company.getCity();
        form.postalCode = company.getPostalCode();
        form.province = company.getProvince();
        form.country = company.getCountry();
        form.invoiceSeries = company.getInvoiceSeries();
        form.quoteSeries = company.getQuoteSeries();
        form.bankName = company.getBankName();
        form.bankIban = company.getBankIban();
        form.logoText = company.getLogoText();
        form.invoiceLegalText = company.getInvoiceLegalText();
        form.defaultPaymentTermsDays = company.getDefaultPaymentTermsDays();
        return form;
    }

    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public String getCommercialName() { return commercialName; }
    public void setCommercialName(String commercialName) { this.commercialName = commercialName; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getInvoiceSeries() { return invoiceSeries; }
    public void setInvoiceSeries(String invoiceSeries) { this.invoiceSeries = invoiceSeries; }
    public String getQuoteSeries() { return quoteSeries; }
    public void setQuoteSeries(String quoteSeries) { this.quoteSeries = quoteSeries; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankIban() { return bankIban; }
    public void setBankIban(String bankIban) { this.bankIban = bankIban; }
    public String getLogoText() { return logoText; }
    public void setLogoText(String logoText) { this.logoText = logoText; }
    public String getInvoiceLegalText() { return invoiceLegalText; }
    public void setInvoiceLegalText(String invoiceLegalText) { this.invoiceLegalText = invoiceLegalText; }
    public Integer getDefaultPaymentTermsDays() { return defaultPaymentTermsDays; }
    public void setDefaultPaymentTermsDays(Integer defaultPaymentTermsDays) { this.defaultPaymentTermsDays = defaultPaymentTermsDays; }
}
