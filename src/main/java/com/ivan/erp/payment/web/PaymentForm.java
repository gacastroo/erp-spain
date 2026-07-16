package com.ivan.erp.payment.web;

import com.ivan.erp.payment.PaymentMethod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentForm {

    @NotNull(message = "La factura es obligatoria")
    private Long invoiceId;

    @NotNull(message = "La fecha de cobro es obligatoria")
    private LocalDate paymentDate = LocalDate.now();

    @NotNull(message = "El importe es obligatorio")
    @DecimalMin(value = "0.01", message = "El importe debe ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "El importe no es válido")
    private BigDecimal amount;

    @NotNull(message = "El método de cobro es obligatorio")
    private PaymentMethod method = PaymentMethod.TRANSFER;

    @Size(max = 120, message = "La referencia no puede superar los 120 caracteres")
    private String reference;

    @Size(max = 500, message = "Las notas no pueden superar los 500 caracteres")
    private String notes;

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = clean(reference);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = clean(notes);
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }
}
