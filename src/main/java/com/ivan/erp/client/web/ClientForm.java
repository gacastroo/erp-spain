package com.ivan.erp.client.web;

import com.ivan.erp.client.Client;
import com.ivan.erp.client.ClientType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ClientForm {

    @NotBlank(message = "El nombre fiscal es obligatorio")
    @Size(max = 180, message = "El nombre fiscal no puede superar 180 caracteres")
    private String legalName;

    @Size(max = 180, message = "El nombre comercial no puede superar 180 caracteres")
    private String commercialName;

    @NotBlank(message = "El NIF/CIF/NIE es obligatorio")
    @Size(max = 20, message = "El NIF/CIF/NIE no puede superar 20 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9\\- ]+$", message = "El NIF/CIF/NIE contiene caracteres no válidos")
    private String taxId;

    @Email(message = "El email no tiene un formato válido")
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

    @NotNull(message = "El tipo de cliente es obligatorio")
    private ClientType clientType = ClientType.COMPANY;

    @Size(max = 2000, message = "Las observaciones no pueden superar 2000 caracteres")
    private String notes;

    public static ClientForm from(Client client) {
        ClientForm form = new ClientForm();
        form.setLegalName(client.getLegalName());
        form.setCommercialName(client.getCommercialName());
        form.setTaxId(client.getTaxId());
        form.setEmail(client.getEmail());
        form.setPhone(client.getPhone());
        form.setAddressLine(client.getAddressLine());
        form.setCity(client.getCity());
        form.setPostalCode(client.getPostalCode());
        form.setProvince(client.getProvince());
        form.setCountry(client.getCountry());
        form.setClientType(client.getClientType());
        form.setNotes(client.getNotes());
        return form;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getCommercialName() {
        return commercialName;
    }

    public void setCommercialName(String commercialName) {
        this.commercialName = commercialName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
