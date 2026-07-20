package com.ivan.erp.payment.web;

import com.ivan.erp.payment.Payment;
import com.ivan.erp.payment.PaymentMethod;
import com.ivan.erp.payment.service.PaymentService;
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
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<Payment> payments = paymentService.search(query, page);

        model.addAttribute("payments", payments);
        model.addAttribute("query", query);
        model.addAttribute("currentPage", page);

        return "payments/index";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String createForm(
            @RequestParam(required = false) Long invoiceId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        PaymentForm form = new PaymentForm();

        if (invoiceId != null) {
            try {
                PaymentInvoiceOption option = paymentService.getPayableInvoiceOption(invoiceId);
                form.setInvoiceId(invoiceId);
                form.setAmount(option.getOutstanding());
            } catch (EntityNotFoundException | IllegalStateException ex) {
                redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                return "redirect:/invoices";
            }
        }

        prepareFormModel(model, form);
        return "payments/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String create(
            @Valid @ModelAttribute("paymentForm") PaymentForm paymentForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareFormModel(model, paymentForm);
            return "payments/form";
        }

        try {
            paymentService.create(paymentForm);
            redirectAttributes.addFlashAttribute("successMessage", "Cobro registrado correctamente");
            return "redirect:/payments";
        } catch (EntityNotFoundException | IllegalStateException | IllegalArgumentException ex) {
            bindingResult.reject("payment.error", ex.getMessage());
            prepareFormModel(model, paymentForm);
            return "payments/form";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String delete(
            @PathVariable Long id,
            @RequestParam(required = false) Long returnInvoiceId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            paymentService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cobro eliminado correctamente");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cobro no encontrado");
        }

        if (returnInvoiceId != null) {
            return "redirect:/invoices/" + returnInvoiceId;
        }

        return "redirect:/payments";
    }

    private void prepareFormModel(Model model, PaymentForm paymentForm) {
        model.addAttribute("paymentForm", paymentForm);
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("invoiceOptions", paymentService.getPayableInvoiceOptions());
    }
}
