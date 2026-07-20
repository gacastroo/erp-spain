package com.ivan.erp.expense.service;

import com.ivan.erp.expense.Expense;
import com.ivan.erp.expense.ExpenseRepository;
import com.ivan.erp.expense.web.ExpenseForm;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {

    private static final int PAGE_SIZE = 10;

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public Page<Expense> search(String query, int page) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "expenseDate").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        return expenseRepository.search(normalizeQuery(query), pageable);
    }

    @Transactional(readOnly = true)
    public Expense getById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Gasto no encontrado"));
    }

    @Transactional
    public Expense create(ExpenseForm form) {
        validateAmounts(form);
        Expense expense = new Expense(
                form.getExpenseDate(),
                form.getSupplierName(),
                form.getSupplierTaxId(),
                form.getInvoiceNumber(),
                form.getCategory(),
                form.getDescription(),
                form.getBaseAmount(),
                form.getVatRate(),
                form.isPaid(),
                form.getPaymentMethod(),
                form.getNotes()
        );

        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense update(Long id, ExpenseForm form) {
        validateAmounts(form);
        Expense expense = getById(id);
        expense.update(
                form.getExpenseDate(),
                form.getSupplierName(),
                form.getSupplierTaxId(),
                form.getInvoiceNumber(),
                form.getCategory(),
                form.getDescription(),
                form.getBaseAmount(),
                form.getVatRate(),
                form.isPaid(),
                form.getPaymentMethod(),
                form.getNotes()
        );
        return expense;
    }

    @Transactional
    public void delete(Long id) {
        Expense expense = getById(id);
        expenseRepository.delete(expense);
    }

    private void validateAmounts(ExpenseForm form) {
        if (form.getBaseAmount() == null
                || form.getBaseAmount().signum() <= 0
                || form.getBaseAmount().scale() > 2
                || form.getBaseAmount().precision() - form.getBaseAmount().scale() > 10) {
            throw new IllegalArgumentException("La base imponible debe ser positiva y tener como máximo 10 enteros y 2 decimales");
        }
        if (form.getVatRate() == null
                || form.getVatRate().signum() < 0
                || form.getVatRate().compareTo(new java.math.BigDecimal("100.00")) > 0
                || form.getVatRate().scale() > 2
                || form.getVatRate().precision() - form.getVatRate().scale() > 3) {
            throw new IllegalArgumentException("El IVA debe estar entre 0 y 100 y tener como máximo 2 decimales");
        }
    }

    private String normalizeQuery(String query) {
        if (query == null || query.trim().isBlank()) {
            return null;
        }
        return query.trim();
    }
}
