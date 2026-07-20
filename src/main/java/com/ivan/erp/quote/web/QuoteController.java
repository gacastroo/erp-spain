package com.ivan.erp.quote.web;

import com.ivan.erp.client.ClientRepository;
import com.ivan.erp.product.ProductRepository;
import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.document.service.DocumentPdfService;
import com.ivan.erp.quote.Quote;
import com.ivan.erp.quote.QuoteStatus;
import com.ivan.erp.quote.service.QuoteService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/quotes")
public class QuoteController {

    private final QuoteService quoteService;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;
    private final DocumentPdfService documentPdfService;

    public QuoteController(
            QuoteService quoteService,
            ClientRepository clientRepository,
            ProductRepository productRepository,
            InvoiceRepository invoiceRepository,
            DocumentPdfService documentPdfService
    ) {
        this.quoteService = quoteService;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.invoiceRepository = invoiceRepository;
        this.documentPdfService = documentPdfService;
    }

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<Quote> quotes = quoteService.search(query, page);
        List<Long> quoteIds = quotes.getContent().stream().map(Quote::getId).toList();
        Set<Long> invoicedQuoteIds = quoteIds.isEmpty()
                ? Set.of()
                : invoiceRepository.findInvoicedQuoteIds(quoteIds);

        model.addAttribute("quotes", quotes);
        model.addAttribute("invoicedQuoteIds", invoicedQuoteIds);
        model.addAttribute("query", query);
        model.addAttribute("currentPage", page);

        return "quotes/index";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String createForm(Model model) {
        prepareFormModel(model, new QuoteForm(), "Nuevo presupuesto", "/quotes");
        return "quotes/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String create(
            @Valid @ModelAttribute("quoteForm") QuoteForm quoteForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validateLines(quoteForm, bindingResult);
        validateDateRange(quoteForm, bindingResult);

        if (bindingResult.hasErrors()) {
            prepareFormModel(model, quoteForm, "Nuevo presupuesto", "/quotes");
            return "quotes/form";
        }

        try {
            quoteService.create(quoteForm);
            redirectAttributes.addFlashAttribute("successMessage", "Presupuesto creado correctamente");
            return "redirect:/quotes";
        } catch (EntityNotFoundException | IllegalStateException ex) {
            bindingResult.reject("quote.error", ex.getMessage());
            prepareFormModel(model, quoteForm, "Nuevo presupuesto", "/quotes");
            return "quotes/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Quote quote = quoteService.getById(id);
            model.addAttribute("quote", quote);
            model.addAttribute("invoice", invoiceRepository.findByQuote_Id(id).orElse(null));
            model.addAttribute("statuses", QuoteStatus.values());
            return "quotes/detail";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Presupuesto no encontrado");
            return "redirect:/quotes";
        }
    }


    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Quote quote = quoteService.getById(id);
        byte[] pdf = documentPdfService.quotePdf(quote);
        String filename = quote.getQuoteNumber().replace("/", "-") + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String editForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (invoiceRepository.existsByQuote_Id(id)) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "No se puede editar este presupuesto porque ya tiene una factura asociada."
                );
                return "redirect:/quotes/" + id;
            }

            Quote quote = quoteService.getById(id);
            prepareFormModel(model, QuoteForm.fromQuote(quote), "Editar presupuesto", "/quotes/" + id);
            model.addAttribute("quoteId", id);
            return "quotes/form";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Presupuesto no encontrado");
            return "redirect:/quotes";
        }
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("quoteForm") QuoteForm quoteForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validateLines(quoteForm, bindingResult);
        validateDateRange(quoteForm, bindingResult);

        if (bindingResult.hasErrors()) {
            prepareFormModel(model, quoteForm, "Editar presupuesto", "/quotes/" + id);
            model.addAttribute("quoteId", id);
            return "quotes/form";
        }

        try {
            quoteService.update(id, quoteForm);
            redirectAttributes.addFlashAttribute("successMessage", "Presupuesto actualizado correctamente");
            return "redirect:/quotes";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/quotes";
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/quotes/" + id;
        }
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam QuoteStatus status,
            RedirectAttributes redirectAttributes
    ) {
        try {
            quoteService.changeStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Estado actualizado correctamente");
            return "redirect:/quotes/" + id;
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Presupuesto no encontrado");
            return "redirect:/quotes";
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/quotes/" + id;
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            quoteService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Presupuesto eliminado correctamente");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Presupuesto no encontrado");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/quotes";
    }

    private void prepareFormModel(Model model, QuoteForm quoteForm, String pageTitle, String formAction) {
        model.addAttribute("quoteForm", quoteForm);
        model.addAttribute("clients", clientRepository.findByEnabledTrueOrderByLegalNameAsc());
        model.addAttribute("products", productRepository.findByEnabledTrueOrderByNameAsc());
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("formAction", formAction);
    }


    private void validateDateRange(QuoteForm quoteForm, BindingResult bindingResult) {
        if (quoteForm.getIssueDate() == null || quoteForm.getValidUntil() == null) {
            return;
        }

        if (quoteForm.getIssueDate().isAfter(quoteForm.getValidUntil())) {
            bindingResult.rejectValue(
                    "validUntil",
                    "validUntil.beforeIssueDate",
                    "La fecha de validez no puede ser anterior a la fecha de emisión"
            );
        }
    }

    private void validateLines(QuoteForm quoteForm, BindingResult bindingResult) {
        long validLines = quoteForm.getLines()
                .stream()
                .filter(line -> !line.isEmpty())
                .count();

        if (validLines == 0) {
            bindingResult.reject("lines.empty", "Debes añadir al menos una línea al presupuesto");
        }
    }
}
