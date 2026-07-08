package com.ivan.erp.invoice.web;

import com.ivan.erp.invoice.Invoice;
import com.ivan.erp.invoice.InvoiceStatus;
import com.ivan.erp.invoice.service.InvoiceService;
import com.ivan.erp.quote.QuoteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final QuoteRepository quoteRepository;

    public InvoiceController(InvoiceService invoiceService, QuoteRepository quoteRepository) {
        this.invoiceService = invoiceService;
        this.quoteRepository = quoteRepository;
    }

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<Invoice> invoices = invoiceService.search(query, page);

        model.addAttribute("invoices", invoices);
        model.addAttribute("query", query);
        model.addAttribute("currentPage", page);

        return "invoices/index";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String createForm(Model model) {
        model.addAttribute("quotes", quoteRepository.findInvoiceableQuotes());
        return "invoices/new";
    }

    @PostMapping("/from-quote/{quoteId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String createFromQuote(
            @PathVariable Long quoteId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            invoiceService.createFromQuote(quoteId);
            redirectAttributes.addFlashAttribute("successMessage", "Factura creada correctamente");
            return "redirect:/invoices";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/invoices/new";
        }
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Invoice invoice = invoiceService.getById(id);
            model.addAttribute("invoice", invoice);
            model.addAttribute("statuses", InvoiceStatus.values());
            return "invoices/detail";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Factura no encontrada");
            return "redirect:/invoices";
        }
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam InvoiceStatus status,
            RedirectAttributes redirectAttributes
    ) {
        try {
            invoiceService.changeStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Estado de factura actualizado correctamente");
            return "redirect:/invoices/" + id;
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Factura no encontrada");
            return "redirect:/invoices";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            invoiceService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Factura eliminada correctamente");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Factura no encontrada");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/invoices/" + id;
        }

        return "redirect:/invoices";
    }
}
