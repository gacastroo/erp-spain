package com.ivan.erp.quote;

public enum QuoteStatus {
    DRAFT("Borrador"),
    SENT("Enviado"),
    ACCEPTED("Aceptado"),
    REJECTED("Rechazado");

    private final String label;

    QuoteStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
