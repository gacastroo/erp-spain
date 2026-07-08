package com.ivan.erp.product;

public enum ProductType {
    PRODUCT("Producto"),
    SERVICE("Servicio");

    private final String label;

    ProductType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
