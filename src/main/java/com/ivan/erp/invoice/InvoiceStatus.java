package com.ivan.erp.invoice;

public enum InvoiceStatus {
    DRAFT("Borrador"),
    ISSUED("Emitida"),
    SENT("Enviada"),
    PAID("Cobrada"),
    OVERDUE("Vencida"),
    CANCELLED("Cancelada");

    private final String label;

    InvoiceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
