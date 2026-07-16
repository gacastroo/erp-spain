package com.ivan.erp.expense.web;

import com.ivan.erp.expense.Expense;
import com.ivan.erp.expense.ExpenseCategory;
import com.ivan.erp.expense.service.ExpenseService;
import com.ivan.erp.payment.PaymentMethod;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<Expense> expenses = expenseService.search(query, page);
        model.addAttribute("expenses", expenses);
        model.addAttribute("query", query);
        model.addAttribute("currentPage", page);
        return "expenses/index";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String createForm(Model model) {
        prepareFormModel(model, new ExpenseForm(), "Nuevo gasto", "/expenses");
        return "expenses/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String create(
            @Valid @ModelAttribute("expenseForm") ExpenseForm expenseForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareFormModel(model, expenseForm, "Nuevo gasto", "/expenses");
            return "expenses/form";
        }

        expenseService.create(expenseForm);
        redirectAttributes.addFlashAttribute("successMessage", "Gasto creado correctamente");
        return "redirect:/expenses";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String editForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Expense expense = expenseService.getById(id);
            prepareFormModel(model, ExpenseForm.fromExpense(expense), "Editar gasto", "/expenses/" + id);
            model.addAttribute("expenseId", id);
            return "expenses/form";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gasto no encontrado");
            return "redirect:/expenses";
        }
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("expenseForm") ExpenseForm expenseForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareFormModel(model, expenseForm, "Editar gasto", "/expenses/" + id);
            model.addAttribute("expenseId", id);
            return "expenses/form";
        }

        try {
            expenseService.update(id, expenseForm);
            redirectAttributes.addFlashAttribute("successMessage", "Gasto actualizado correctamente");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gasto no encontrado");
        }

        return "redirect:/expenses";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            expenseService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Gasto eliminado correctamente");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gasto no encontrado");
        }

        return "redirect:/expenses";
    }

    private void prepareFormModel(Model model, ExpenseForm expenseForm, String pageTitle, String formAction) {
        model.addAttribute("expenseForm", expenseForm);
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("formAction", formAction);
    }
}
