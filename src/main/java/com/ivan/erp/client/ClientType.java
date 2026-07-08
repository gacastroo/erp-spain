package com.ivan.erp.client;

public enum ClientType {
    COMPANY("Empresa"),
    SELF_EMPLOYED("Autónomo"),
    INDIVIDUAL("Particular"),
    PUBLIC_ENTITY("Administración pública");

    private final String displayName;

    ClientType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
