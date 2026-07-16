package com.ivan.erp.expense;

public enum ExpenseCategory {
    PURCHASES("Compras"),
    RENT("Alquiler"),
    UTILITIES("Suministros"),
    PROFESSIONAL_SERVICES("Servicios profesionales"),
    SALARIES("Personal"),
    TAXES("Impuestos"),
    TRANSPORT("Transporte"),
    MARKETING("Marketing"),
    SOFTWARE("Software"),
    OTHER("Otros");

    private final String label;

    ExpenseCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
