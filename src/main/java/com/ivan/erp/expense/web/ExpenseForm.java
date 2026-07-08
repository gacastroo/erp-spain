package com.ivan.erp.expense.web;

import com.ivan.erp.expense.Expense;
import com.ivan.erp.expense.ExpenseCategory;
import com.ivan.erp.payment.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseForm {

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate expenseDate = LocalDate.now();

    @NotBlank(message = "El proveedor o concepto es obligatorio")
    @Size(max = 180, message = "No puede superar 180 caracteres")
    private String supplierName;

    @Size(max = 20, message = "No puede superar 20 caracteres")
    private String supplierTaxId;

    @Size(max = 80, message = "No puede superar 80 caracteres")
    private String invoiceNumber;

    @NotNull(message = "La categoría es obligatoria")
    private ExpenseCategory category = ExpenseCategory.OTHER;

    @Size(max = 500, message = "No puede superar 500 caracteres")
    private String description;

    @NotNull(message = "La base imponible es obligatoria")
    @DecimalMin(value = "0.01", message = "La base debe ser mayor que 0")
    private BigDecimal baseAmount = BigDecimal.ZERO;

    @NotNull(message = "El IVA es obligatorio")
    @DecimalMin(value = "0.00", message = "El IVA no puede ser negativo")
    private BigDecimal vatRate = BigDecimal.valueOf(21);

    private boolean paid = true;

    @NotNull(message = "El método de pago es obligatorio")
    private PaymentMethod paymentMethod = PaymentMethod.TRANSFER;

    @Size(max = 500, message = "No puede superar 500 caracteres")
    private String notes;

    public static ExpenseForm fromExpense(Expense expense) {
        ExpenseForm form = new ExpenseForm();
        form.setExpenseDate(expense.getExpenseDate());
        form.setSupplierName(expense.getSupplierName());
        form.setSupplierTaxId(expense.getSupplierTaxId());
        form.setInvoiceNumber(expense.getInvoiceNumber());
        form.setCategory(expense.getCategory());
        form.setDescription(expense.getDescription());
        form.setBaseAmount(expense.getBaseAmount());
        form.setVatRate(expense.getVatRate());
        form.setPaid(expense.isPaid());
        form.setPaymentMethod(expense.getPaymentMethod());
        form.setNotes(expense.getNotes());
        return form;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierTaxId() {
        return supplierTaxId;
    }

    public void setSupplierTaxId(String supplierTaxId) {
        this.supplierTaxId = supplierTaxId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public void setVatRate(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
