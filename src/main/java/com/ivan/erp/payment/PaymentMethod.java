package com.ivan.erp.payment;

public enum PaymentMethod {
    TRANSFER("Transferencia"),
    CARD("Tarjeta"),
    CASH("Efectivo"),
    BIZUM("Bizum"),
    DIRECT_DEBIT("Domiciliación"),
    OTHER("Otro");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
