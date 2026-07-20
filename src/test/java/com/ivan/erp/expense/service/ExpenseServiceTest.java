package com.ivan.erp.expense.service;

import com.ivan.erp.expense.ExpenseCategory;
import com.ivan.erp.expense.ExpenseRepository;
import com.ivan.erp.expense.web.ExpenseForm;
import com.ivan.erp.payment.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ExpenseServiceTest {

    private final ExpenseRepository repository = mock(ExpenseRepository.class);
    private final ExpenseService service = new ExpenseService(repository);

    @Test
    void rejectsVatAboveOneHundredEvenWhenCalledOutsideMvcValidation() {
        ExpenseForm form = validForm();
        form.setVatRate(new BigDecimal("100.01"));

        assertThatThrownBy(() -> service.create(form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entre 0 y 100");
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsMoreThanTwoDecimalPlaces() {
        ExpenseForm form = validForm();
        form.setBaseAmount(new BigDecimal("10.001"));

        assertThatThrownBy(() -> service.create(form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2 decimales");
    }

    private ExpenseForm validForm() {
        ExpenseForm form = new ExpenseForm();
        form.setExpenseDate(LocalDate.now());
        form.setSupplierName("Proveedor");
        form.setCategory(ExpenseCategory.OTHER);
        form.setBaseAmount(new BigDecimal("10.00"));
        form.setVatRate(new BigDecimal("21.00"));
        form.setPaymentMethod(PaymentMethod.TRANSFER);
        return form;
    }
}
