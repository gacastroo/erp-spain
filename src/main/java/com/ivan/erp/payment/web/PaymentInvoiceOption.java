package com.ivan.erp.payment.web;

import java.math.BigDecimal;

public class PaymentInvoiceOption {

    private final Long id;
    private final String invoiceNumber;
    private final String clientName;
    private final BigDecimal total;
    private final BigDecimal paid;
    private final BigDecimal outstanding;

    public PaymentInvoiceOption(
            Long id,
            String invoiceNumber,
            String clientName,
            BigDecimal total,
            BigDecimal paid,
            BigDecimal outstanding
    ) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.clientName = clientName;
        this.total = total;
        this.paid = paid;
        this.outstanding = outstanding;
    }

    public Long getId() {
        return id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getClientName() {
        return clientName;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public BigDecimal getOutstanding() {
        return outstanding;
    }

    public String getLabel() {
        return invoiceNumber + " · " + clientName;
    }
}
