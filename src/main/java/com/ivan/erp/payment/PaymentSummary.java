package com.ivan.erp.payment;

import java.math.BigDecimal;

public class PaymentSummary {

    private final BigDecimal paidAmount;
    private final BigDecimal outstandingAmount;

    public PaymentSummary(BigDecimal paidAmount, BigDecimal outstandingAmount) {
        this.paidAmount = paidAmount;
        this.outstandingAmount = outstandingAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public boolean isFullyPaid() {
        return outstandingAmount.compareTo(BigDecimal.ZERO) <= 0;
    }
}
